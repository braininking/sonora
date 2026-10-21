package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongMenuBottomSheet(
    song: Song,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onToggleFavorite: () -> Unit,
    onGoToArtist: () -> Unit,
    onGoToAlbum: () -> Unit,
    onGoToFolder: () -> Unit,
    onViewInfo: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    sheetState: SheetState
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Song Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AlbumArtImage(
                    albumId = song.albumId,
                    size = 52.dp,
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${song.artist} • ${song.album}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            // Actions
            MenuItemRow(
                icon = Icons.Default.PlayArrow,
                title = "Reproduzir",
                testTag = "menu_action_play",
                onClick = { onDismiss(); onPlay() }
            )
            MenuItemRow(
                icon = Icons.Default.QueueMusic,
                title = "Adicionar à fila",
                testTag = "menu_action_queue",
                onClick = { onDismiss(); onAddToQueue() }
            )
            MenuItemRow(
                icon = Icons.Default.PlaylistAdd,
                title = "Adicionar à playlist",
                testTag = "menu_action_playlist",
                onClick = { onDismiss(); onAddToPlaylist() }
            )
            MenuItemRow(
                icon = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                title = if (song.isFavorite) "Remover dos favoritos" else "Favoritar",
                tint = if (song.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                testTag = "menu_action_favorite",
                onClick = { onDismiss(); onToggleFavorite() }
            )
            MenuItemRow(
                icon = Icons.Default.Person,
                title = "Ir para artista (${song.artist})",
                testTag = "menu_action_artist",
                onClick = { onDismiss(); onGoToArtist() }
            )
            MenuItemRow(
                icon = Icons.Default.Album,
                title = "Ir para álbum (${song.album})",
                testTag = "menu_action_album",
                onClick = { onDismiss(); onGoToAlbum() }
            )
            MenuItemRow(
                icon = Icons.Default.Folder,
                title = "Ir para pasta (${song.folderName})",
                testTag = "menu_action_folder",
                onClick = { onDismiss(); onGoToFolder() }
            )
            MenuItemRow(
                icon = Icons.Default.Info,
                title = "Ver informações",
                testTag = "menu_action_info",
                onClick = { onDismiss(); onViewInfo() }
            )
            MenuItemRow(
                icon = Icons.Default.Share,
                title = "Compartilhar arquivo",
                testTag = "menu_action_share",
                onClick = { onDismiss(); onShare() }
            )
            MenuItemRow(
                icon = Icons.Default.Delete,
                title = "Excluir arquivo",
                tint = MaterialTheme.colorScheme.error,
                testTag = "menu_action_delete",
                onClick = { onDismiss(); onDelete() }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MenuItemRow(
    icon: ImageVector,
    title: String,
    testTag: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
