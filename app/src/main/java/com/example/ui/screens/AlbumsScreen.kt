package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.Album
import com.example.model.Song
import com.example.ui.components.AlbumArtImage
import com.example.ui.components.SongItem

@Composable
fun AlbumsScreen(
    albums: List<Album>,
    selectedAlbum: Album?,
    albumSongs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onSelectAlbum: (Album?) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onPlayAllAlbum: (List<Song>) -> Unit,
    onShuffleAllAlbum: (List<Song>) -> Unit,
    onAddAlbumToQueue: (List<Song>) -> Unit,
    onMenuSong: (Song) -> Unit
) {
    if (selectedAlbum != null) {
        AlbumDetailView(
            album = selectedAlbum,
            songs = albumSongs,
            currentSong = currentSong,
            isPlaying = isPlaying,
            onBack = { onSelectAlbum(null) },
            onSongClick = { song -> onSongClick(song, albumSongs) },
            onPlayAll = { onPlayAllAlbum(albumSongs) },
            onShuffleAll = { onShuffleAllAlbum(albumSongs) },
            onAddToQueue = { onAddAlbumToQueue(albumSongs) },
            onMenuSong = onMenuSong
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("albums_screen")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Álbuns",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${albums.size} álbum(ns)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (albums.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Nenhum álbum encontrado",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(albums, key = { it.id }) { album ->
                        AlbumGridItem(
                            album = album,
                            onClick = { onSelectAlbum(album) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumGridItem(
    album: Album,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("album_item_${album.id}")
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            AlbumArtImage(
                albumId = album.id,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = album.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = album.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${album.songCount} música(s)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AlbumDetailView(
    album: Album,
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onAddToQueue: () -> Unit,
    onMenuSong: (Song) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("album_detail_view")
    ) {
        // Top Back Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("btn_back_album")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar"
                )
            }
            Text(
                text = "Álbum",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Album Hero Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumArtImage(
                albumId = album.id,
                size = 100.dp,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = album.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (album.year > 0) {
                    Text(
                        text = "Ano: ${album.year} • ${songs.size} faixas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "${songs.size} faixas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Action Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onPlayAll,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tocar", fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onShuffleAll,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Aleatório", fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onAddToQueue,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.QueueMusic, contentDescription = "Fila", modifier = Modifier.size(18.dp))
            }
        }

        // Tracks in track order
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
                    onMenuClick = { onMenuSong(song) },
                    trackNumber = song.trackNumber
                )
            }
        }
    }
}
