package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.db.entities.PlaybackHistoryEntity
import com.example.data.db.entities.PlaylistEntity
import com.example.model.Song
import com.example.ui.components.SongItem

const val PLAYLIST_ID_FAVORITES = -1L
const val PLAYLIST_ID_HISTORY = -2L
const val PLAYLIST_ID_MOST_PLAYED = -3L
const val PLAYLIST_ID_RECENTLY_ADDED = -4L

@Composable
fun PlaylistsScreen(
    playlists: List<PlaylistEntity>,
    selectedPlaylistId: Long?,
    selectedPlaylistName: String,
    playlistSongs: List<Song>,
    playlistSongCounts: Map<Long, Int>,
    allSongs: List<Song>,
    favorites: List<Song>,
    history: List<PlaybackHistoryEntity>,
    currentSong: Song?,
    isPlaying: Boolean,
    onSelectPlaylist: (Long?, String) -> Unit,
    onCreatePlaylist: () -> Unit,
    onRenamePlaylist: (PlaylistEntity) -> Unit,
    onDeletePlaylist: (PlaylistEntity) -> Unit,
    onClearHistory: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onPlayAll: (List<Song>) -> Unit,
    onShuffleAll: (List<Song>) -> Unit,
    onMenuSong: (Song) -> Unit,
    onRemoveSongFromPlaylist: (Long, Long) -> Unit
) {
    if (selectedPlaylistId != null) {
        // Determine the songs for this playlist
        val songsToDisplay = when (selectedPlaylistId) {
            PLAYLIST_ID_FAVORITES -> favorites
            PLAYLIST_ID_HISTORY -> {
                history.mapNotNull { entry -> allSongs.find { it.id == entry.songId } }.distinctBy { it.id }
            }
            PLAYLIST_ID_MOST_PLAYED -> {
                val playCounts = history.groupBy { it.songId }.mapValues { it.value.size }
                allSongs.filter { (playCounts[it.id] ?: 0) > 0 }
                    .sortedByDescending { playCounts[it.id] ?: 0 }
            }
            PLAYLIST_ID_RECENTLY_ADDED -> {
                allSongs.sortedByDescending { it.dateAdded }.take(50)
            }
            else -> {
                playlistSongs
            }
        }

        PlaylistDetailView(
            playlistId = selectedPlaylistId,
            name = selectedPlaylistName,
            songs = songsToDisplay,
            currentSong = currentSong,
            isPlaying = isPlaying,
            onBack = { onSelectPlaylist(null, "") },
            onClearHistory = onClearHistory,
            onSongClick = { song -> onSongClick(song, songsToDisplay) },
            onPlayAll = { onPlayAll(songsToDisplay) },
            onShuffleAll = { onShuffleAll(songsToDisplay) },
            onMenuSong = onMenuSong
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("playlists_screen"),
            contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Playlists",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Coleções automáticas e personalizadas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onCreatePlaylist,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_create_playlist")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Criar")
                    }
                }
            }

            // System smart playlists
            item {
                Text(
                    text = "Playlists Automáticas",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                SystemPlaylistRow(
                    title = "Músicas Favoritas",
                    subtitle = "${favorites.size} música(s)",
                    icon = Icons.Default.Favorite,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    onClick = { onSelectPlaylist(PLAYLIST_ID_FAVORITES, "Favoritos") }
                )

                SystemPlaylistRow(
                    title = "Histórico de Reprodução",
                    subtitle = "${history.size} registro(s)",
                    icon = Icons.Default.History,
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = { onSelectPlaylist(PLAYLIST_ID_HISTORY, "Histórico") }
                )

                SystemPlaylistRow(
                    title = "Mais Tocadas",
                    subtitle = "Baseado em suas reproduções",
                    icon = Icons.Default.TrendingUp,
                    iconTint = Color(0xFF10B981),
                    onClick = { onSelectPlaylist(PLAYLIST_ID_MOST_PLAYED, "Mais Tocadas") }
                )

                SystemPlaylistRow(
                    title = "Adicionadas Recentemente",
                    subtitle = "Últimos arquivos adicionados",
                    icon = Icons.Default.NewReleases,
                    iconTint = Color(0xFFF59E0B),
                    onClick = { onSelectPlaylist(PLAYLIST_ID_RECENTLY_ADDED, "Adicionadas Recentemente") }
                )
            }

            // User custom playlists
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Minhas Playlists (${playlists.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            if (playlists.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Nenhuma playlist personalizada criada ainda",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(playlists, key = { it.id }) { playlist ->
                    val count = playlistSongCounts[playlist.id] ?: 0
                    UserPlaylistRow(
                        playlist = playlist,
                        songCount = count,
                        onClick = { onSelectPlaylist(playlist.id, playlist.name) },
                        onRename = { onRenamePlaylist(playlist) },
                        onDelete = { onDeletePlaylist(playlist) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemPlaylistRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = iconTint.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UserPlaylistRow(
    playlist: PlaylistEntity,
    songCount: Int,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("playlist_item_${playlist.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlaylistPlay,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$songCount música(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("playlist_menu_${playlist.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opções"
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Renomear") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Excluir", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistDetailView(
    playlistId: Long,
    name: String,
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onClearHistory: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onMenuSong: (Song) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("playlist_detail_view")
    ) {
        // Back Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("btn_back_playlist")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar"
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${songs.size} música(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (playlistId == PLAYLIST_ID_HISTORY && songs.isNotEmpty()) {
                TextButton(
                    onClick = onClearHistory,
                    modifier = Modifier.testTag("btn_clear_history")
                ) {
                    Text("Limpar", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        // Action Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onPlayAll,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                enabled = songs.isNotEmpty()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tocar todas", fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onShuffleAll,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                enabled = songs.isNotEmpty()
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Aleatório", fontWeight = FontWeight.SemiBold)
            }
        }

        if (songs.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Nenhuma música nesta playlist",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(songs, key = { it.id }) { song ->
                    SongItem(
                        song = song,
                        isPlaying = isPlaying,
                        isCurrent = song.id == currentSong?.id,
                        onClick = { onSongClick(song) },
                        onMenuClick = { onMenuSong(song) }
                    )
                }
            }
        }
    }
}
