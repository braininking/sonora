package com.example.ui

import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SonoraApplication
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Folder
import com.example.model.Genre
import com.example.model.Song
import com.example.model.SortOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class NavigationTab {
    HOME,
    SONGS,
    FOLDERS,
    ALBUMS,
    ARTISTS,
    PLAYLISTS
}

enum class ThemeSetting {
    SYSTEM,
    LIGHT,
    DARK
}

data class SearchResult(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val folders: List<Folder> = emptyList()
)

class SonoraViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SonoraApplication
    val repository = app.repository
    val playerController = app.playerController

    // Navigation & UI state
    private val _currentTab = MutableStateFlow(NavigationTab.HOME)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _themeSetting = MutableStateFlow(ThemeSetting.SYSTEM)
    val themeSetting: StateFlow<ThemeSetting> = _themeSetting.asStateFlow()

    private val _currentSortOrder = MutableStateFlow(SortOrder.NAME_ASC)
    val currentSortOrder: StateFlow<SortOrder> = _currentSortOrder.asStateFlow()

    // Drill-down selections
    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum: StateFlow<Album?> = _selectedAlbum.asStateFlow()

    private val _selectedArtist = MutableStateFlow<Artist?>(null)
    val selectedArtist: StateFlow<Artist?> = _selectedArtist.asStateFlow()

    private val _selectedFolder = MutableStateFlow<Folder?>(null)
    val selectedFolder: StateFlow<Folder?> = _selectedFolder.asStateFlow()

    private val _selectedPlaylistId = MutableStateFlow<Long?>(null)
    val selectedPlaylistId: StateFlow<Long?> = _selectedPlaylistId.asStateFlow()

    private val _selectedPlaylistName = MutableStateFlow<String>("")
    val selectedPlaylistName: StateFlow<String> = _selectedPlaylistName.asStateFlow()

    // Full Player Screen & Bottom Sheets visibility
    private val _isFullPlayerVisible = MutableStateFlow(false)
    val isFullPlayerVisible: StateFlow<Boolean> = _isFullPlayerVisible.asStateFlow()

    private val _isQueueSheetVisible = MutableStateFlow(false)
    val isQueueSheetVisible: StateFlow<Boolean> = _isQueueSheetVisible.asStateFlow()

    private val _menuSong = MutableStateFlow<Song?>(null)
    val menuSong: StateFlow<Song?> = _menuSong.asStateFlow()

    private val _infoSong = MutableStateFlow<Song?>(null)
    val infoSong: StateFlow<Song?> = _infoSong.asStateFlow()

    private val _deleteSongCandidate = MutableStateFlow<Song?>(null)
    val deleteSongCandidate: StateFlow<Song?> = _deleteSongCandidate.asStateFlow()

    private val _addToPlaylistSong = MutableStateFlow<Song?>(null)
    val addToPlaylistSong: StateFlow<Song?> = _addToPlaylistSong.asStateFlow()

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    // Raw data flows from repository
    val allSongsRaw = repository.allSongs
    val isScanning = repository.isScanning
    val playlists = repository.playlists
    val favorites = repository.favoriteSongs
    val playbackHistory = repository.playbackHistory
    val playlistSongCounts = repository.playlistSongCounts

    // Sorted all songs
    val allSongs: StateFlow<List<Song>> = combine(allSongsRaw, _currentSortOrder) { songs, order ->
        withContext(Dispatchers.Default) {
            order.sortSongs(songs)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<Album>> = repository.albums
    val artists: StateFlow<List<Artist>> = repository.artists
    val folders: StateFlow<List<Folder>> = repository.folders

    fun getSongsForPlaylist(playlistId: Long) = repository.getSongsForPlaylist(playlistId)

    // Search results (evaluated on Dispatchers.Default using precomputed repository caches)
    val searchResult: StateFlow<SearchResult> = combine(allSongsRaw, _searchQuery) { songs, query ->
        withContext(Dispatchers.Default) {
            val trimmed = query.trim()
            if (trimmed.isEmpty()) {
                SearchResult()
            } else {
                val q = trimmed.lowercase()
                val matchedSongs = songs.filter {
                    it.title.lowercase().contains(q) ||
                            it.artist.lowercase().contains(q) ||
                            it.album.lowercase().contains(q) ||
                            it.folderName.lowercase().contains(q) ||
                            it.genre.lowercase().contains(q)
                }
                val matchedAlbums = repository.albums.value.filter {
                    it.title.lowercase().contains(q) || it.artist.lowercase().contains(q)
                }
                val matchedArtists = repository.artists.value.filter {
                    it.name.lowercase().contains(q)
                }
                val matchedFolders = repository.folders.value.filter {
                    it.name.lowercase().contains(q) || it.path.lowercase().contains(q)
                }
                SearchResult(
                    songs = matchedSongs,
                    albums = matchedAlbums,
                    artists = matchedArtists,
                    folders = matchedFolders
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResult())

    fun setTab(tab: NavigationTab) {
        _currentTab.value = tab
        // Clear drill-down selections when user taps on primary tabs
        _selectedAlbum.value = null
        _selectedArtist.value = null
        _selectedFolder.value = null
        _selectedPlaylistId.value = null
        _isSearchActive.value = false
        _isFullPlayerVisible.value = false
    }

    fun setThemeSetting(setting: ThemeSetting) {
        _themeSetting.value = setting
    }

    fun setSortOrder(order: SortOrder) {
        _currentSortOrder.value = order
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
        }
    }

    fun selectAlbum(album: Album?) {
        _selectedAlbum.value = album
    }

    fun selectArtist(artist: Artist?) {
        _selectedArtist.value = artist
    }

    fun selectFolder(folder: Folder?) {
        _selectedFolder.value = folder
    }

    fun selectPlaylist(id: Long?, name: String = "") {
        _selectedPlaylistId.value = id
        _selectedPlaylistName.value = name
    }

    fun setFullPlayerVisible(visible: Boolean) {
        _isFullPlayerVisible.value = visible
    }

    fun setQueueSheetVisible(visible: Boolean) {
        _isQueueSheetVisible.value = visible
    }

    fun setMenuSong(song: Song?) {
        _menuSong.value = song
    }

    fun setInfoSong(song: Song?) {
        _infoSong.value = song
    }

    fun setDeleteSongCandidate(song: Song?) {
        _deleteSongCandidate.value = song
    }

    fun setAddToPlaylistSong(song: Song?) {
        _addToPlaylistSong.value = song
    }

    // Playback commands
    fun playSong(song: Song, queueList: List<Song>) {
        playerController.playSong(song, queueList)
    }

    fun playQueue(queueList: List<Song>, startIndex: Int = 0) {
        playerController.playQueue(queueList, startIndex)
    }

    fun shuffleAll(songs: List<Song>) {
        if (songs.isNotEmpty()) {
            val shuffled = songs.shuffled()
            playerController.playQueue(shuffled, 0)
        }
    }

    fun togglePlayPause() = playerController.togglePlayPause()
    fun seekTo(positionMs: Long) = playerController.seekTo(positionMs)
    fun seekForward10s() = playerController.seekForward10s()
    fun seekBack10s() = playerController.seekBack10s()
    fun skipNext() = playerController.skipNext()
    fun skipPrevious() = playerController.skipPrevious()
    fun toggleShuffle() = playerController.toggleShuffle()
    fun toggleRepeat() = playerController.toggleRepeat()

    fun addToQueue(song: Song) = playerController.addToQueue(song)
    fun addListToQueue(songs: List<Song>) = playerController.addListToQueue(songs)
    fun removeFromQueue(index: Int) = playerController.removeFromQueue(index)
    fun moveQueueItem(fromIndex: Int, toIndex: Int) = playerController.moveQueueItem(fromIndex, toIndex)
    fun clearQueue() = playerController.clearQueue()

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song.id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun renamePlaylist(id: Long, newName: String) {
        viewModelScope.launch {
            repository.renamePlaylist(id, newName)
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(id)
            if (_selectedPlaylistId.value == id) {
                _selectedPlaylistId.value = null
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun deleteSongFile(song: Song, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val resolver: ContentResolver = app.contentResolver
                val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.id)
                resolver.delete(uri, null, null)
                repository.rescan()
                onDeleted()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _deleteSongCandidate.value = null
            }
        }
    }

    fun shareSong(song: Song) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, song.contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Compartilhar áudio").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            app.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun rescan() {
        viewModelScope.launch {
            repository.rescan()
        }
    }
}
