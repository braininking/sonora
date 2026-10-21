package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.db.entities.PlaybackHistoryEntity
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Folder
import com.example.model.Song
import com.example.ui.ThemeSetting
import com.example.ui.components.AlbumArtImage
import com.example.ui.components.SongItem

@Composable
fun HomeScreen(
    allSongs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    folders: List<Folder>,
    favorites: List<Song>,
    history: List<PlaybackHistoryEntity>,
    isScanning: Boolean,
    currentSong: Song?,
    isPlaying: Boolean,
    themeSetting: ThemeSetting,
    onSongClick: (Song, List<Song>) -> Unit,
    onShuffleAll: () -> Unit,
    onPlayAll: () -> Unit,
    onMenuSong: (Song) -> Unit,
    onSearchClick: () -> Unit,
    onThemeToggle: () -> Unit,
    onViewFavorites: () -> Unit,
    onViewHistory: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        // App Bar & Quick Actions
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
                        text = "Sonora",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Música Offline",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier.testTag("home_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onThemeToggle,
                        modifier = Modifier.testTag("home_theme_btn")
                    ) {
                        Icon(
                            imageVector = if (themeSetting == ThemeSetting.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Alternar Tema",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Sonora Hero Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.sonora_music_hero),
                        contentDescription = "Sonora Hero Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Som Puro, Sem Conexão",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (allSongs.isNotEmpty()) "${allSongs.size} faixas prontas para reprodução" else "Escaneando armazenamento...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Library Stats Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Biblioteca Local",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isScanning) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Indexando…",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(icon = Icons.Default.MusicNote, value = "${allSongs.size}", label = "Músicas")
                        StatItem(icon = Icons.Default.Album, value = "${albums.size}", label = "Álbuns")
                        StatItem(icon = Icons.Default.Person, value = "${artists.size}", label = "Artistas")
                        StatItem(icon = Icons.Default.Folder, value = "${folders.size}", label = "Pastas")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Play & Shuffle Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onPlayAll,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_play_all"),
                            shape = RoundedCornerShape(12.dp),
                            enabled = allSongs.isNotEmpty()
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reproduzir", fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onShuffleAll,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_shuffle_all"),
                            shape = RoundedCornerShape(12.dp),
                            enabled = allSongs.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Shuffle, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Aleatório", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Favorites Horizontal Shelf
        if (favorites.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Favoritos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Ver todos (${favorites.size})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(onClick = onViewFavorites)
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favorites.take(10), key = { it.id }) { song ->
                        FavoriteCard(
                            song = song,
                            onClick = { onSongClick(song, favorites) }
                        )
                    }
                }
            }
        }

        // Recently Played Section
        if (history.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reproduzidas Recentemente",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Histórico",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(onClick = onViewHistory)
                    )
                }
            }

            val historySongs = history.mapNotNull { entry ->
                allSongs.find { it.id == entry.songId }
            }.distinctBy { it.id }.take(5)

            items(historySongs, key = { "home_history_${it.id}" }) { song ->
                SongItem(
                    song = song,
                    isPlaying = isPlaying,
                    isCurrent = song.id == currentSong?.id,
                    onClick = { onSongClick(song, historySongs) },
                    onMenuClick = { onMenuSong(song) }
                )
            }
        }

        // Recent Additions
        if (allSongs.isNotEmpty()) {
            item {
                Text(
                    text = "Músicas Recentes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }

            val recentSongs = allSongs.sortedByDescending { it.dateAdded }.take(8)
            items(recentSongs, key = { "home_recent_${it.id}" }) { song ->
                SongItem(
                    song = song,
                    isPlaying = isPlaying,
                    isCurrent = song.id == currentSong?.id,
                    onClick = { onSongClick(song, recentSongs) },
                    onMenuClick = { onMenuSong(song) }
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FavoriteCard(
    song: Song,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        AlbumArtImage(
            albumId = song.albumId,
            size = 102.dp,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
