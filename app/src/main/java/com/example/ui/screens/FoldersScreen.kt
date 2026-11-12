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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.Folder
import com.example.model.Song
import com.example.util.NaturalOrderComparator
import com.example.ui.components.SongItem

@Composable
fun FoldersScreen(
    folders: List<Folder>,
    selectedFolder: Folder?,
    folderSongs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onSelectFolder: (Folder?) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onPlayAllFolder: (List<Song>) -> Unit,
    onShuffleAllFolder: (List<Song>) -> Unit,
    onMenuSong: (Song) -> Unit
) {
    // Keep the parent folder-list scroll position while the detail screen is shown.
    // The LazyColumn leaves composition during drill-down, but this state holder stays alive.
    val foldersListState = rememberLazyListState()

    if (selectedFolder != null) {
        // Folder Detail View
        FolderDetailView(
            folder = selectedFolder,
            songs = folderSongs,
            currentSong = currentSong,
            isPlaying = isPlaying,
            onBack = { onSelectFolder(null) },
            onSongClick = { song -> onSongClick(song, folderSongs) },
            onPlayAll = { onPlayAllFolder(folderSongs) },
            onShuffleAll = { onShuffleAllFolder(folderSongs) },
            onMenuSong = onMenuSong
        )
    } else {
        // Folders List View
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("folders_screen")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pastas do Dispositivo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${folders.size} pasta(s) com arquivos de áudio",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (folders.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Nenhuma pasta encontrada",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = foldersListState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    items(folders, key = { it.path }) { folder ->
                        FolderItemRow(
                            folder = folder,
                            onClick = { onSelectFolder(folder) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderItemRow(
    folder: Folder,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("folder_item_${folder.name}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${folder.songCount} música(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (folder.path.isNotBlank()) {
                    Text(
                        text = folder.path,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun FolderDetailView(
    folder: Folder,
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onMenuSong: (Song) -> Unit
) {
    var isAlphabeticalSort by remember { mutableStateOf(true) }

    val sortedSongs = remember(songs, isAlphabeticalSort) {
        if (isAlphabeticalSort) {
            songs.sortedWith { a, b -> NaturalOrderComparator.compare(a.title, b.title) }
        } else {
            songs.sortedByDescending { it.dateAdded }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("folder_detail_view")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("btn_back_folder")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar"
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
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

            IconButton(
                onClick = { isAlphabeticalSort = !isAlphabeticalSort }
            ) {
                Icon(
                    imageVector = Icons.Default.SortByAlpha,
                    contentDescription = "Ordenar",
                    tint = if (isAlphabeticalSort) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Folder Path Banner
        if (folder.path.isNotBlank()) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "📁 ${folder.path}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Action Buttons
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
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tocar todas", fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onShuffleAll,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                enabled = songs.isNotEmpty()
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Aleatório", fontWeight = FontWeight.SemiBold)
            }
        }

        // Songs
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            items(sortedSongs, key = { it.id }) { song ->
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
