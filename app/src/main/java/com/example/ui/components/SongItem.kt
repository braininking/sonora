package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.Song

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongItem(
    song: Song,
    isPlaying: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    trackNumber: Int? = null
) {
    val backgroundColor = if (isCurrent) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onMenuClick
            )
            .testTag("song_item_${song.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track number or Album Art
            if (trackNumber != null && trackNumber > 0) {
                Text(
                    text = String.format("%02d", trackNumber),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(32.dp)
                )
            } else {
                AlbumArtImage(
                    albumId = song.albumId,
                    size = 48.dp,
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Song Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCurrent && isPlaying) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Tocando",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = song.durationFormatted,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (song.isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorita",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(start = 2.dp)
                        )
                    }
                }
            }

            // 3-dots action menu
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("menu_song_${song.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Mais opções",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
