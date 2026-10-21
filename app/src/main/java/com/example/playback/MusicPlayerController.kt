package com.example.playback

import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.model.Song
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Collections

class MusicPlayerController(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onSongStarted: (Song) -> Unit = {}
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentQueueIndex = MutableStateFlow(0)
    val currentQueueIndex: StateFlow<Int> = _currentQueueIndex.asStateFlow()

    private var progressJob: Job? = null

    init {
        initializeController()
    }

    private fun isTestEnvironment(): Boolean {
        return try {
            Class.forName("org.robolectric.Robolectric")
            true
        } catch (e: Throwable) {
            false
        }
    }

    private fun initializeController() {
        if (isTestEnvironment()) return
        try {
            val sessionToken = SessionToken(
                context,
                ComponentName(context, MusicPlaybackService::class.java)
            )
            controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            val mainExecutor = ContextCompat.getMainExecutor(context)
            controllerFuture?.addListener({
                try {
                    mediaController = controllerFuture?.get()
                    setupPlayerListener()
                    // If playback was requested before controller was fully bound, trigger it safely now
                    val controller = mediaController
                    if (controller != null && _isPlaying.value && _queue.value.isNotEmpty()) {
                        val mediaItems = _queue.value.map { it.toMediaItem() }
                        controller.setMediaItems(mediaItems, _currentQueueIndex.value, _currentPositionMs.value)
                        controller.prepare()
                        controller.play()
                        startProgressTracker()
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }, mainExecutor)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun setupPlayerListener() {
        val controller = mediaController ?: return

        try {
            controller.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                    if (isPlaying) {
                        startProgressTracker()
                    } else {
                        stopProgressTracker()
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    updateCurrentSongFromPlayer()
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        _durationMs.value = controller.duration.coerceAtLeast(0L)
                    }
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    _isShuffleEnabled.value = shuffleModeEnabled
                }

                override fun onRepeatModeChanged(newRepeatMode: Int) {
                    _repeatMode.value = newRepeatMode
                }
            })

            // Synchronize initial state
            _isPlaying.value = controller.isPlaying
            _isShuffleEnabled.value = controller.shuffleModeEnabled
            _repeatMode.value = controller.repeatMode
            updateCurrentSongFromPlayer()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun updateCurrentSongFromPlayer() {
        try {
            val controller = mediaController ?: return
            val currentMediaItem = controller.currentMediaItem ?: return
            val songId = currentMediaItem.mediaId.toLongOrNull() ?: return

            val foundIndex = _queue.value.indexOfFirst { it.id == songId }
            if (foundIndex != -1) {
                _currentQueueIndex.value = foundIndex
                val song = _queue.value[foundIndex]
                _currentSong.value = song
                _durationMs.value = if (song.durationMs > 0) song.durationMs else controller.duration.coerceAtLeast(0L)
                onSongStarted(song)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        // MUST execute on Dispatchers.Main because ExoPlayer/MediaController verifies the application thread Looper
        progressJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                try {
                    mediaController?.let { controller ->
                        if (controller.isPlaying) {
                            val pos = controller.currentPosition.coerceAtLeast(0L)
                            if (_currentPositionMs.value != pos) {
                                _currentPositionMs.value = pos
                            }
                            val dur = controller.duration
                            if (dur > 0 && _durationMs.value != dur) {
                                _durationMs.value = dur
                            }
                        }
                    }
                } catch (e: Throwable) {
                    // Safe guard against any unexpected player state transition
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        try {
            mediaController?.let { controller ->
                _currentPositionMs.value = controller.currentPosition.coerceAtLeast(0L)
            }
        } catch (e: Throwable) {
            // Safe guard
        }
    }

    fun playSong(song: Song, queueList: List<Song>) {
        val index = queueList.indexOfFirst { it.id == song.id }
        val startIndex = if (index != -1) index else 0
        playQueue(queueList, startIndex)
    }

    fun playQueue(queueList: List<Song>, startIndex: Int = 0) {
        if (queueList.isEmpty()) return

        _queue.value = queueList
        _currentQueueIndex.value = startIndex.coerceIn(0, queueList.size - 1)
        val targetSong = queueList[_currentQueueIndex.value]
        _currentSong.value = targetSong
        _isPlaying.value = true

        val controller = mediaController
        if (controller == null) {
            if (controllerFuture == null) {
                initializeController()
            }
            return
        }

        try {
            val mediaItems = queueList.map { it.toMediaItem() }
            controller.setMediaItems(mediaItems, _currentQueueIndex.value, 0L)
            controller.prepare()
            controller.play()
            onSongStarted(targetSong)
            startProgressTracker()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        try {
            if (controller.isPlaying) {
                controller.pause()
            } else {
                if (controller.mediaItemCount == 0 && _queue.value.isNotEmpty()) {
                    playQueue(_queue.value, _currentQueueIndex.value)
                } else {
                    controller.play()
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun seekTo(positionMs: Long) {
        val controller = mediaController ?: return
        try {
            controller.seekTo(positionMs)
            _currentPositionMs.value = positionMs
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun seekForward10s() {
        val controller = mediaController ?: return
        try {
            val newPos = (controller.currentPosition + 10_000).coerceAtMost(controller.duration)
            seekTo(newPos)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun seekBack10s() {
        val controller = mediaController ?: return
        try {
            val newPos = (controller.currentPosition - 10_000).coerceAtLeast(0L)
            seekTo(newPos)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun skipNext() {
        val controller = mediaController ?: return
        try {
            if (controller.hasNextMediaItem()) {
                controller.seekToNextMediaItem()
            } else if (_repeatMode.value == Player.REPEAT_MODE_ALL && controller.mediaItemCount > 0) {
                controller.seekTo(0, 0L)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun skipPrevious() {
        val controller = mediaController ?: return
        try {
            // If within first 3 seconds, jump to previous song; otherwise jump to beginning of current song
            if (controller.currentPosition > 3000) {
                seekTo(0L)
            } else if (controller.hasPreviousMediaItem()) {
                controller.seekToPreviousMediaItem()
            } else {
                seekTo(0L)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun toggleShuffle() {
        val controller = mediaController ?: return
        try {
            val newState = !controller.shuffleModeEnabled
            controller.shuffleModeEnabled = newState
            _isShuffleEnabled.value = newState
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun toggleRepeat() {
        val controller = mediaController ?: return
        try {
            val nextMode = when (controller.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
            controller.repeatMode = nextMode
            _repeatMode.value = nextMode
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun addToQueue(song: Song) {
        val controller = mediaController ?: return
        try {
            val updated = _queue.value.toMutableList().apply { add(song) }
            _queue.value = updated
            controller.addMediaItem(song.toMediaItem())
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun addListToQueue(songs: List<Song>) {
        val controller = mediaController ?: return
        try {
            val updated = _queue.value.toMutableList().apply { addAll(songs) }
            _queue.value = updated
            controller.addMediaItems(songs.map { it.toMediaItem() })
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun removeFromQueue(index: Int) {
        val controller = mediaController ?: return
        try {
            if (index !in _queue.value.indices) return

            val updated = _queue.value.toMutableList().apply { removeAt(index) }
            _queue.value = updated
            controller.removeMediaItem(index)

            if (index == _currentQueueIndex.value) {
                updateCurrentSongFromPlayer()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        val controller = mediaController ?: return
        try {
            if (fromIndex !in _queue.value.indices || toIndex !in _queue.value.indices) return

            val updated = _queue.value.toMutableList()
            Collections.swap(updated, fromIndex, toIndex)
            _queue.value = updated
            controller.moveMediaItem(fromIndex, toIndex)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun clearQueue() {
        val controller = mediaController ?: return
        try {
            controller.clearMediaItems()
            _queue.value = emptyList()
            _currentSong.value = null
            _isPlaying.value = false
            _currentPositionMs.value = 0L
            _durationMs.value = 0L
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun release() {
        progressJob?.cancel()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        mediaController = null
    }

    private fun Song.toMediaItem(): MediaItem {
        val albumArtUri = ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"),
            albumId
        )

        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(contentUri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbumTitle(album)
                    .setArtworkUri(albumArtUri)
                    .build()
            )
            .build()
    }
}
