package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueBottomSheet(
    queue: List<Song>,
    currentIndex: Int,
    isPlaying: Boolean,
    onSongClick: (Int) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onMoveItem: (Int, Int) -> Unit,
    onClearQueue: () -> Unit,
    onDismiss: () -> Unit,
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
                .fillMaxHeight(0.75f)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Fila de Reprodução",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${queue.size} música(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    if (queue.isNotEmpty()) {
                        TextButton(
                            onClick = onClearQueue,
                            modifier = Modifier.testTag("clear_queue_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ClearAll,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Limpar")
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_queue_sheet")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                    }
                }
            }

            if (queue.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "A fila está vazia",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(
                        items = queue,
                        key = { idx, song -> "${song.id}_$idx" }
                    ) { index, song ->
                        val isCurrent = index == currentIndex
                        Surface(
                            color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSongClick(index) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Index or playing icon
                                if (isCurrent) {
                                    Icon(
                                        imageVector = Icons.Default.Equalizer,
                                        contentDescription = "Tocando",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .padding(end = 6.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.width(24.dp)
                                    )
                                }

                                AlbumArtImage(
                                    albumId = song.albumId,
                                    size = 40.dp,
                                    shape = RoundedCornerShape(6.dp)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = song.artist,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Reorder up
                                IconButton(
                                    onClick = { if (index > 0) onMoveItem(index, index - 1) },
                                    enabled = index > 0,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "Mover para cima",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Reorder down
                                IconButton(
                                    onClick = { if (index < queue.size - 1) onMoveItem(index, index + 1) },
                                    enabled = index < queue.size - 1,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Mover para baixo",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Remove
                                IconButton(
                                    onClick = { onRemoveItem(index) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remover da fila",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
