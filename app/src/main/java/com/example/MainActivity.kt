package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.db.entities.PlaylistEntity
import com.example.ui.NavigationTab
import com.example.ui.SonoraViewModel
import com.example.ui.ThemeSetting
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.FullPlayerScreen
import com.example.ui.components.MiniPlayer
import com.example.ui.components.SonoraBottomNavigation
import com.example.ui.components.QueueBottomSheet
import com.example.ui.components.RenamePlaylistDialog
import com.example.ui.components.SongInfoDialog
import com.example.ui.components.SongMenuBottomSheet
import com.example.ui.screens.AlbumsScreen
import com.example.ui.screens.ArtistsScreen
import com.example.ui.screens.FoldersScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SongsScreen
import com.example.ui.theme.SonoraTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SonoraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeSetting by viewModel.themeSetting.collectAsState()

            SonoraTheme(themeSetting = themeSetting) {
                MainContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(viewModel: SonoraViewModel) {
    val context = LocalContext.current

    // Permission state
    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val granted = permissionsMap[audioPermission] == true
        hasAudioPermission = granted
        if (granted) {
            viewModel.rescan()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasAudioPermission) {
            val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.POST_NOTIFICATIONS)
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            permissionLauncher.launch(perms)
        }
    }

    // Observing ViewModel state
    val currentTab by viewModel.currentTab.collectAsState()
    val themeSetting by viewModel.themeSetting.collectAsState()
    val currentSortOrder by viewModel.currentSortOrder.collectAsState()

    val allSongs by viewModel.allSongs.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val playlists by viewModel.playlists.collectAsState(initial = emptyList())
    val playlistSongCounts by viewModel.playlistSongCounts.collectAsState(initial = emptyMap())
    val favorites by viewModel.favorites.collectAsState(initial = emptyList())
    val history by viewModel.playbackHistory.collectAsState(initial = emptyList())
    val isScanning by viewModel.isScanning.collectAsState()

    // UI overlays & Dialogs
    val isFullPlayerVisible by viewModel.isFullPlayerVisible.collectAsState()

    // Player state
    val currentSong by viewModel.playerController.currentSong.collectAsState()
    val isPlaying by viewModel.playerController.isPlaying.collectAsState()
    val currentPositionMs by viewModel.playerController.currentPositionMs.collectAsState()
    val durationMs by viewModel.playerController.durationMs.collectAsState()
    val isShuffleEnabled by viewModel.playerController.isShuffleEnabled.collectAsState()
    val repeatMode by viewModel.playerController.repeatMode.collectAsState()
    val queue by viewModel.playerController.queue.collectAsState()
    val currentQueueIndex by viewModel.playerController.currentQueueIndex.collectAsState()

    // Drill down states
    val selectedAlbum by viewModel.selectedAlbum.collectAsState()
    val selectedArtist by viewModel.selectedArtist.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val selectedPlaylistId by viewModel.selectedPlaylistId.collectAsState()
    val selectedPlaylistName by viewModel.selectedPlaylistName.collectAsState()

    // Dialogs & Sheets
    val isQueueSheetVisible by viewModel.isQueueSheetVisible.collectAsState()
    val menuSong by viewModel.menuSong.collectAsState()
    val infoSong by viewModel.infoSong.collectAsState()
    val deleteCandidate by viewModel.deleteSongCandidate.collectAsState()
    val addToPlaylistSong by viewModel.addToPlaylistSong.collectAsState()

    // Search state
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResult by viewModel.searchResult.collectAsState()

    // Playlist dialogs
    var isCreatePlaylistOpen by remember { mutableStateOf(false) }
    var playlistToRename by remember { mutableStateOf<PlaylistEntity?>(null) }
    var playlistToDelete by remember { mutableStateOf<PlaylistEntity?>(null) }

    // Bottom sheet states
    val queueSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val menuSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Handle back presses
    BackHandler(enabled = isFullPlayerVisible || isSearchActive || selectedAlbum != null || selectedArtist != null || selectedFolder != null || selectedPlaylistId != null) {
        when {
            isFullPlayerVisible -> viewModel.setFullPlayerVisible(false)
            isSearchActive -> viewModel.setSearchActive(false)
            selectedAlbum != null -> viewModel.selectAlbum(null)
            selectedArtist != null -> viewModel.selectArtist(null)
            selectedFolder != null -> viewModel.selectFolder(null)
            selectedPlaylistId != null -> viewModel.selectPlaylist(null)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // MiniPlayer above bottom navigation
                if (currentSong != null && !isFullPlayerVisible) {
                    MiniPlayer(
                        song = currentSong!!,
                        isPlaying = isPlaying,
                        currentPositionMs = currentPositionMs,
                        durationMs = durationMs,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onSkipNext = { viewModel.skipNext() },
                        onExpand = { viewModel.setFullPlayerVisible(true) }
                    )
                }

                // Sonora custom responsive bottom navigation (un-truncated, fluid, fully accessible)
                SonoraBottomNavigation(
                    currentTab = currentTab,
                    isSearchActive = isSearchActive,
                    onTabSelected = { tab -> viewModel.setTab(tab) },
                    modifier = Modifier.testTag("bottom_navigation")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!hasAudioPermission) {
                // Permission Request Banner
                PermissionRationaleView(
                    onRequestPermission = {
                        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            arrayOf(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                        permissionLauncher.launch(perms)
                    }
                )
            } else if (isSearchActive) {
                // Search Screen
                SearchScreen(
                    query = searchQuery,
                    searchResult = searchResult,
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    onCloseSearch = { viewModel.setSearchActive(false) },
                    onSongClick = { song -> viewModel.playSong(song, searchResult.songs) },
                    onAlbumClick = { album ->
                        viewModel.selectAlbum(album)
                        viewModel.setSearchActive(false)
                        viewModel.setTab(NavigationTab.ALBUMS)
                    },
                    onArtistClick = { artist ->
                        viewModel.selectArtist(artist)
                        viewModel.setSearchActive(false)
                        viewModel.setTab(NavigationTab.ARTISTS)
                    },
                    onFolderClick = { folder ->
                        viewModel.selectFolder(folder)
                        viewModel.setSearchActive(false)
                        viewModel.setTab(NavigationTab.FOLDERS)
                    },
                    onMenuSong = { viewModel.setMenuSong(it) }
                )
            } else {
                // Active Tab Screen
                when (currentTab) {
                    NavigationTab.HOME -> {
                        HomeScreen(
                            allSongs = allSongs,
                            albums = albums,
                            artists = artists,
                            folders = folders,
                            favorites = favorites,
                            history = history,
                            isScanning = isScanning,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            themeSetting = themeSetting,
                            onSongClick = { song, list -> viewModel.playSong(song, list) },
                            onShuffleAll = { viewModel.shuffleAll(allSongs) },
                            onPlayAll = { viewModel.playQueue(allSongs, 0) },
                            onMenuSong = { viewModel.setMenuSong(it) },
                            onSearchClick = { viewModel.setSearchActive(true) },
                            onThemeToggle = {
                                viewModel.setThemeSetting(
                                    if (themeSetting == ThemeSetting.DARK) ThemeSetting.LIGHT else ThemeSetting.DARK
                                )
                            },
                            onViewFavorites = {
                                viewModel.setTab(NavigationTab.PLAYLISTS)
                                viewModel.selectPlaylist(com.example.ui.screens.PLAYLIST_ID_FAVORITES, "Favoritos")
                            },
                            onViewHistory = {
                                viewModel.setTab(NavigationTab.PLAYLISTS)
                                viewModel.selectPlaylist(com.example.ui.screens.PLAYLIST_ID_HISTORY, "Histórico")
                            }
                        )
                    }
                    NavigationTab.SONGS -> {
                        SongsScreen(
                            songs = allSongs,
                            currentSortOrder = currentSortOrder,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            onSortOrderChange = { viewModel.setSortOrder(it) },
                            onSongClick = { song -> viewModel.playSong(song, allSongs) },
                            onPlayAll = { viewModel.playQueue(allSongs, 0) },
                            onShuffleAll = { viewModel.shuffleAll(allSongs) },
                            onMenuSong = { viewModel.setMenuSong(it) },
                            onSearchClick = { viewModel.setSearchActive(true) }
                        )
                    }
                    NavigationTab.FOLDERS -> {
                        val folderSongs = remember(allSongs, selectedFolder?.path) {
                            selectedFolder?.let { folder ->
                                // Folder names are not unique; use the full path to avoid
                                // mixing songs from different folders with the same name.
                                allSongs.filter { it.folderPath == folder.path }
                            }.orEmpty()
                        }

                        FoldersScreen(
                            folders = folders,
                            selectedFolder = selectedFolder,
                            folderSongs = folderSongs,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            onSelectFolder = { viewModel.selectFolder(it) },
                            onSongClick = { song, list -> viewModel.playSong(song, list) },
                            onPlayAllFolder = { list -> viewModel.playQueue(list, 0) },
                            onShuffleAllFolder = { list -> viewModel.shuffleAll(list) },
                            onMenuSong = { viewModel.setMenuSong(it) }
                        )
                    }
                    NavigationTab.ALBUMS -> {
                        val albumSongs = if (selectedAlbum != null) {
                            allSongs.filter { it.albumId == selectedAlbum!!.id }
                                .sortedWith { a, b ->
                                    if (a.trackNumber > 0 && b.trackNumber > 0) a.trackNumber.compareTo(b.trackNumber)
                                    else a.title.compareTo(b.title)
                                }
                        } else emptyList()

                        AlbumsScreen(
                            albums = albums,
                            selectedAlbum = selectedAlbum,
                            albumSongs = albumSongs,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            onSelectAlbum = { viewModel.selectAlbum(it) },
                            onSongClick = { song, list -> viewModel.playSong(song, list) },
                            onPlayAllAlbum = { list -> viewModel.playQueue(list, 0) },
                            onShuffleAllAlbum = { list -> viewModel.shuffleAll(list) },
                            onAddAlbumToQueue = { list -> viewModel.addListToQueue(list) },
                            onMenuSong = { viewModel.setMenuSong(it) }
                        )
                    }
                    NavigationTab.ARTISTS -> {
                        val artistSongs = if (selectedArtist != null) {
                            allSongs.filter { it.artist.equals(selectedArtist!!.name, ignoreCase = true) }
                        } else emptyList()

                        ArtistsScreen(
                            artists = artists,
                            selectedArtist = selectedArtist,
                            artistSongs = artistSongs,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            onSelectArtist = { viewModel.selectArtist(it) },
                            onSelectAlbum = { album ->
                                viewModel.selectAlbum(album)
                                viewModel.setTab(NavigationTab.ALBUMS)
                            },
                            onSongClick = { song, list -> viewModel.playSong(song, list) },
                            onPlayAllArtist = { list -> viewModel.playQueue(list, 0) },
                            onShuffleAllArtist = { list -> viewModel.shuffleAll(list) },
                            onMenuSong = { viewModel.setMenuSong(it) }
                        )
                    }
                    NavigationTab.PLAYLISTS -> {
                        val customPlaylistSongs by remember(selectedPlaylistId) {
                            if (selectedPlaylistId != null && selectedPlaylistId!! > 0) {
                                viewModel.getSongsForPlaylist(selectedPlaylistId!!)
                            } else {
                                kotlinx.coroutines.flow.flowOf(emptyList<com.example.model.Song>())
                            }
                        }.collectAsState(initial = emptyList())

                        PlaylistsScreen(
                            playlists = playlists,
                            selectedPlaylistId = selectedPlaylistId,
                            selectedPlaylistName = selectedPlaylistName,
                            playlistSongs = customPlaylistSongs,
                            playlistSongCounts = playlistSongCounts,
                            allSongs = allSongs,
                            favorites = favorites,
                            history = history,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            onSelectPlaylist = { id, name -> viewModel.selectPlaylist(id, name) },
                            onCreatePlaylist = { isCreatePlaylistOpen = true },
                            onRenamePlaylist = { playlistToRename = it },
                            onDeletePlaylist = { playlistToDelete = it },
                            onClearHistory = { viewModel.clearHistory() },
                            onSongClick = { song, list -> viewModel.playSong(song, list) },
                            onPlayAll = { list -> viewModel.playQueue(list, 0) },
                            onShuffleAll = { list -> viewModel.shuffleAll(list) },
                            onMenuSong = { viewModel.setMenuSong(it) },
                            onRemoveSongFromPlaylist = { playlistId, songId ->
                                viewModel.removeSongFromPlaylist(playlistId, songId)
                            }
                        )
                    }
                }
            }

            // Full Player Screen Overlay
            AnimatedVisibility(
                visible = isFullPlayerVisible && currentSong != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                currentSong?.let { song ->
                    FullPlayerScreen(
                        song = song,
                        isPlaying = isPlaying,
                        currentPositionMs = currentPositionMs,
                        durationMs = durationMs,
                        isShuffleEnabled = isShuffleEnabled,
                        repeatMode = repeatMode,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onSeekTo = { viewModel.seekTo(it) },
                        onSeekForward10s = { viewModel.seekForward10s() },
                        onSeekBack10s = { viewModel.seekBack10s() },
                        onSkipNext = { viewModel.skipNext() },
                        onSkipPrevious = { viewModel.skipPrevious() },
                        onToggleShuffle = { viewModel.toggleShuffle() },
                        onToggleRepeat = { viewModel.toggleRepeat() },
                        onToggleFavorite = { viewModel.toggleFavorite(song) },
                        onOpenQueue = { viewModel.setQueueSheetVisible(true) },
                        onOpenInfo = { viewModel.setInfoSong(song) },
                        onDismiss = { viewModel.setFullPlayerVisible(false) }
                    )
                }
            }
        }
    }

    // Queue Bottom Sheet
    if (isQueueSheetVisible) {
        QueueBottomSheet(
            queue = queue,
            currentIndex = currentQueueIndex,
            isPlaying = isPlaying,
            onSongClick = { idx -> viewModel.playQueue(queue, idx) },
            onRemoveItem = { idx -> viewModel.removeFromQueue(idx) },
            onMoveItem = { from, to -> viewModel.moveQueueItem(from, to) },
            onClearQueue = { viewModel.clearQueue() },
            onDismiss = { viewModel.setQueueSheetVisible(false) },
            sheetState = queueSheetState
        )
    }

    // Song 3-Dots Menu Bottom Sheet
    menuSong?.let { song ->
        SongMenuBottomSheet(
            song = song,
            onDismiss = { viewModel.setMenuSong(null) },
            onPlay = { viewModel.playSong(song, allSongs) },
            onAddToQueue = { viewModel.addToQueue(song) },
            onAddToPlaylist = { viewModel.setAddToPlaylistSong(song) },
            onToggleFavorite = { viewModel.toggleFavorite(song) },
            onGoToArtist = {
                val artist = artists.find { it.name.equals(song.artist, ignoreCase = true) }
                if (artist != null) {
                    viewModel.selectArtist(artist)
                    viewModel.setTab(NavigationTab.ARTISTS)
                }
            },
            onGoToAlbum = {
                val album = albums.find { it.id == song.albumId }
                if (album != null) {
                    viewModel.selectAlbum(album)
                    viewModel.setTab(NavigationTab.ALBUMS)
                }
            },
            onGoToFolder = {
                val folder = folders.find { it.name == song.folderName }
                if (folder != null) {
                    viewModel.selectFolder(folder)
                    viewModel.setTab(NavigationTab.FOLDERS)
                }
            },
            onViewInfo = { viewModel.setInfoSong(song) },
            onShare = { viewModel.shareSong(song) },
            onDelete = { viewModel.setDeleteSongCandidate(song) },
            sheetState = menuSheetState
        )
    }

    // Metadata Info Dialog
    infoSong?.let { song ->
        SongInfoDialog(
            song = song,
            onDismiss = { viewModel.setInfoSong(null) }
        )
    }

    // Add to Playlist Selection Dialog
    addToPlaylistSong?.let { song ->
        AddToPlaylistDialog(
            song = song,
            playlists = playlists,
            onDismiss = { viewModel.setAddToPlaylistSong(null) },
            onSelectPlaylist = { playlistId ->
                viewModel.addSongToPlaylist(playlistId, song.id)
                viewModel.setAddToPlaylistSong(null)
            },
            onCreateNewPlaylist = {
                viewModel.setAddToPlaylistSong(null)
                isCreatePlaylistOpen = true
            }
        )
    }

    // Create Playlist Dialog
    if (isCreatePlaylistOpen) {
        CreatePlaylistDialog(
            onDismiss = { isCreatePlaylistOpen = false },
            onConfirm = { name ->
                viewModel.createPlaylist(name)
                isCreatePlaylistOpen = false
            }
        )
    }

    // Rename Playlist Dialog
    playlistToRename?.let { playlist ->
        RenamePlaylistDialog(
            initialName = playlist.name,
            onDismiss = { playlistToRename = null },
            onConfirm = { newName ->
                viewModel.renamePlaylist(playlist.id, newName)
                playlistToRename = null
            }
        )
    }

    // Delete Playlist Confirmation Dialog
    playlistToDelete?.let { playlist ->
        ConfirmDeleteDialog(
            title = "Excluir Playlist",
            message = "Tem certeza de que deseja excluir a playlist \"${playlist.name}\"? Suas músicas locais não serão apagadas.",
            onConfirm = {
                viewModel.deletePlaylist(playlist.id)
                playlistToDelete = null
            },
            onDismiss = { playlistToDelete = null }
        )
    }

    // Delete Song Confirmation Dialog
    deleteCandidate?.let { song ->
        ConfirmDeleteDialog(
            title = "Excluir Arquivo",
            message = "Tem certeza de que deseja apagar o arquivo \"${song.title}\" do armazenamento do dispositivo? Esta ação não pode ser desfeita.",
            onConfirm = {
                viewModel.deleteSongFile(song)
            },
            onDismiss = { viewModel.setDeleteSongCandidate(null) }
        )
    }
}

@Composable
private fun PermissionRationaleView(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Acesso aos Arquivos de Áudio",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "O Sonora é um player de música offline. Para localizar e reproduzir suas músicas locais, precisamos da sua permissão para ler os arquivos de áudio do dispositivo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onRequestPermission,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_request_permission")
                ) {
                    Text(
                        text = "Conceder Permissão",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
