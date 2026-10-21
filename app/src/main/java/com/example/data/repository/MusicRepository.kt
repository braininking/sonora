package com.example.data.repository

import android.content.Context
import com.example.data.db.SonoraDatabase
import com.example.data.db.entities.FavoriteEntity
import com.example.data.db.entities.PlaybackHistoryEntity
import com.example.data.db.entities.PlaylistEntity
import com.example.data.mediastore.MediaStoreScanner
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Folder
import com.example.model.Genre
import com.example.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicRepository(
    private val context: Context,
    private val scanner: MediaStoreScanner,
    private val database: SonoraDatabase,
    private val scope: CoroutineScope
) {
    private val _rawSongs = MutableStateFlow<List<Song>>(emptyList())
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val favoriteDao = database.favoriteDao()
    private val historyDao = database.playbackHistoryDao()
    private val playlistDao = database.playlistDao()

    val favoriteSongIds: Flow<List<Long>> = favoriteDao.getAllFavoriteSongIds()

    // All songs combined with favorites status
    val allSongs: StateFlow<List<Song>> = combine(_rawSongs, favoriteSongIds) { songs, favIds ->
        val favSet = favIds.toSet()
        songs.map { song ->
            val isFav = favSet.contains(song.id)
            if (song.isFavorite != isFav) {
                song.copy(isFavorite = isFav)
            } else {
                song
            }
        }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val favoriteSongs: Flow<List<Song>> = allSongs.map { songs ->
        songs.filter { it.isFavorite }
    }

    val playlists: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
    val playbackHistory: Flow<List<PlaybackHistoryEntity>> = historyDao.getRecentHistory()
    val playlistSongCounts: Flow<Map<Long, Int>> = playlistDao.getPlaylistSongCounts().map { list ->
        list.associate { it.playlistId to it.count }
    }

    // Precomputed and cached in background thread (Dispatchers.Default)
    val albums: StateFlow<List<Album>> = _rawSongs.map { songs ->
        withContext(Dispatchers.Default) {
            scanner.extractAlbums(songs)
        }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val artists: StateFlow<List<Artist>> = _rawSongs.map { songs ->
        withContext(Dispatchers.Default) {
            scanner.extractArtists(songs)
        }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val folders: StateFlow<List<Folder>> = _rawSongs.map { songs ->
        withContext(Dispatchers.Default) {
            scanner.extractFolders(songs)
        }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val genres: StateFlow<List<Genre>> = _rawSongs.map { songs ->
        withContext(Dispatchers.Default) {
            scanner.extractGenres(songs)
        }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    init {
        // Observe media changes automatically with debounce to prevent duplicate rescans
        scope.launch {
            scanner.observeAudioChanges()
                .debounce(1500L)
                .collect {
                    if (!_isScanning.value) {
                        rescan()
                    }
                }
        }
    }

    suspend fun rescan() = withContext(Dispatchers.IO) {
        if (_isScanning.value) return@withContext
        _isScanning.value = true
        try {
            val scanned = scanner.scanSongs()
            _rawSongs.value = scanned
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isScanning.value = false
        }
    }

    fun getAlbums(): List<Album> = albums.value
    fun getArtists(): List<Artist> = artists.value
    fun getFolders(): List<Folder> = folders.value
    fun getGenres(): List<Genre> = genres.value

    fun getSongsForAlbum(albumId: Long): List<Song> {
        return allSongs.value.filter { it.albumId == albumId }
            .sortedBy { if (it.trackNumber > 0) it.trackNumber else Int.MAX_VALUE }
    }

    fun getSongsForArtist(artistName: String): List<Song> {
        return allSongs.value.filter { it.artist.equals(artistName, ignoreCase = true) }
    }

    fun getSongsForFolder(folderPath: String): List<Song> {
        return allSongs.value.filter { it.folderPath == folderPath }
    }

    suspend fun toggleFavorite(songId: Long) = withContext(Dispatchers.IO) {
        val favIds = favoriteDao.getAllFavoriteSongIds().first()
        if (favIds.contains(songId)) {
            favoriteDao.deleteFavorite(songId)
        } else {
            favoriteDao.insertFavorite(FavoriteEntity(songId = songId))
        }
    }

    suspend fun recordPlayback(song: Song) = withContext(Dispatchers.IO) {
        historyDao.recordPlayback(
            songId = song.id,
            title = song.title,
            artist = song.artist,
            album = song.album
        )
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        historyDao.clearHistory()
    }

    // Playlist Operations
    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylist(PlaylistEntity(name = name))
    }

    suspend fun renamePlaylist(id: Long, name: String) = withContext(Dispatchers.IO) {
        playlistDao.renamePlaylist(id, name)
    }

    suspend fun deletePlaylist(id: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylistWithSongs(id)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        playlistDao.addSong(playlistId, songId)
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> {
        return combine(allSongs, playlistDao.getSongIdsForPlaylist(playlistId)) { songs, songIds ->
            val songMap = songs.associateBy { it.id }
            songIds.mapNotNull { songMap[it] }
        }
    }
}
