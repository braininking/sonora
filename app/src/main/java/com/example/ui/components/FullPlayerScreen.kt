package com.example.ui.components

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.example.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreen(
    song: Song,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isShuffleEnabled: Boolean,
    repeatMode: Int,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekForward10s: () -> Unit,
    onSeekBack10s: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenInfo: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val maxVolume = remember { audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15 }
    var currentVolume by remember {
        mutableFloatStateOf(
            (audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 8).toFloat()
        )
    }

    var isUserScrubbing by remember { mutableStateOf(false) }
    var scrubPositionMs by remember { mutableFloatStateOf(0f) }

    val activePositionMs = if (isUserScrubbing) {
        scrubPositionMs.toLong()
    } else {
        currentPositionMs
    }

    val sliderValue = if (durationMs > 0) {
        (activePositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Header
            TopAppBar(
                title = {
                    Text(
                        text = "Tocando agora",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("full_player_collapse")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Fechar player"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenInfo,
                        modifier = Modifier.testTag("full_player_info")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Informações da música"
                        )
                    }
                    IconButton(
                        onClick = onOpenQueue,
                        modifier = Modifier.testTag("full_player_queue")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = "Fila de reprodução"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Large Album Artwork
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp),
                contentAlignment = Alignment.Center
            ) {
                AlbumArtImage(
                    albumId = song.albumId,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shadow(16.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Song Info & Favorite
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${song.artist} • ${song.album}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("full_player_favorite")
                ) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (song.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Slider
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Slider(
                    value = sliderValue,
                    onValueChange = { fraction ->
                        isUserScrubbing = true
                        scrubPositionMs = fraction * durationMs
                    },
                    onValueChangeFinished = {
                        isUserScrubbing = false
                        onSeekTo(scrubPositionMs.toLong())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("full_player_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(activePositionMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTime(durationMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Playback Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("full_player_shuffle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Aleatório",
                        tint = if (isShuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }

                // Rewind 10 seconds
                IconButton(
                    onClick = onSeekBack10s,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("full_player_rewind10")
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Voltar 10 segundos",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Previous
                IconButton(
                    onClick = onSkipPrevious,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("full_player_prev")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Música anterior",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Main Play/Pause Button
                FilledIconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(68.dp)
                        .shadow(8.dp, CircleShape)
                        .testTag("full_player_play_pause"),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Next
                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("full_player_next")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Próxima música",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Forward 10 seconds
                IconButton(
                    onClick = onSeekForward10s,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("full_player_forward10")
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "Avançar 10 segundos",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Repeat Mode Button
                IconButton(
                    onClick = onToggleRepeat,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("full_player_repeat")
                ) {
                    val repeatIcon = if (repeatMode == Player.REPEAT_MODE_ONE) {
                        Icons.Default.RepeatOne
                    } else {
                        Icons.Default.Repeat
                    }
                    val isRepeatActive = repeatMode != Player.REPEAT_MODE_OFF
                    Icon(
                        imageVector = repeatIcon,
                        contentDescription = "Repetir",
                        tint = if (isRepeatActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Volume Control Row
            if (audioManager != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeDown,
                        contentDescription = "Volume Baixo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = currentVolume,
                        onValueChange = { vol ->
                            currentVolume = vol
                            try {
                                audioManager.setStreamVolume(
                                    AudioManager.STREAM_MUSIC,
                                    vol.toInt(),
                                    0
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        valueRange = 0f..maxVolume.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.secondary,
                            activeTrackColor = MaterialTheme.colorScheme.secondary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Volume Alto",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hours = minutes / 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes % 60, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
