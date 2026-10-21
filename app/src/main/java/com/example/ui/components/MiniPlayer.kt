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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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

@Composable
fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    progressProvider: () -> Float,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onExpand)
            .testTag("mini_player")
    ) {
        Column {
            // High-performance LinearProgressIndicator reading progressProvider directly in draw phase
            LinearProgressIndicator(
                progress = progressProvider,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art thumbnail with memory caching and downsampling
                AlbumArtImage(
                    albumId = song.albumId,
                    size = 46.dp,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Song Title & Artist
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Play / Pause Button
                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("mini_player_play_pause")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Skip Next Button
                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("mini_player_next")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Próxima música",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) = MiniPlayer(
    song = song,
    isPlaying = isPlaying,
    progressProvider = {
        if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    },
    onTogglePlayPause = onTogglePlayPause,
    onSkipNext = onSkipNext,
    onExpand = onExpand,
    modifier = modifier
)
