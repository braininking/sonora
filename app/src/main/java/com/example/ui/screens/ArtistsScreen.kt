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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Song
import com.example.ui.components.AlbumArtImage
import com.example.ui.components.SongItem

@Composable
fun ArtistsScreen(
    artists: List<Artist>,
    selectedArtist: Artist?,
    artistSongs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onSelectArtist: (Artist?) -> Unit,
    onSelectAlbum: (Album) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onPlayAllArtist: (List<Song>) -> Unit,
    onShuffleAllArtist: (List<Song>) -> Unit,
    onMenuSong: (Song) -> Unit
) {
    if (selectedArtist != null) {
        ArtistDetailView(
            artist = selectedArtist,
            songs = artistSongs,
            currentSong = currentSong,
            isPlaying = isPlaying,
            onBack = { onSelectArtist(null) },
            onSelectAlbum = onSelectAlbum,
            onSongClick = { song -> onSongClick(song, artistSongs) },
            onPlayAll = { onPlayAllArtist(artistSongs) },
            onShuffleAll = { onShuffleAllArtist(artistSongs) },
            onMenuSong = onMenuSong
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("artists_screen")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Artistas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${artists.size} artista(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (artists.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Nenhum artista encontrado",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    items(artists, key = { it.name }) { artist ->
                        ArtistItemRow(
                            artist = artist,
                            onClick = { onSelectArtist(artist) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistItemRow(
    artist: Artist,
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
            .testTag("artist_item_${artist.name}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${artist.songCount} música(s) • ${artist.albumCount} álbum(ns)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ArtistDetailView(
    artist: Artist,
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onSelectAlbum: (Album) -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onMenuSong: (Song) -> Unit
) {
    val albums = remember(songs) {
        songs.groupBy { it.albumId to it.album }
            .map { (key, albumSongs) ->
                Album(
                    id = key.first,
                    title = key.second,
                    artist = artist.name,
                    songCount = albumSongs.size,
                    year = albumSongs.map { it.year }.maxOrNull() ?: 0
                )
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("artist_detail_view")
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
                modifier = Modifier.testTag("btn_back_artist")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar"
                )
            }
            Text(
                text = "Artista",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Artist Hero
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${songs.size} música(s) • ${albums.size} álbum(ns)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Action Buttons
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
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tocar todas", fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onShuffleAll,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Aleatório", fontWeight = FontWeight.SemiBold)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Albums Section
            if (albums.isNotEmpty()) {
                item {
                    Text(
                        text = "Álbuns do Artista",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(albums, key = { it.id }) { album ->
                            Column(
                                modifier = Modifier
                                    .width(110.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSelectAlbum(album) }
                                    .padding(4.dp)
                            ) {
                                AlbumArtImage(
                                    albumId = album.id,
                                    size = 102.dp,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = album.title,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${album.songCount} música(s)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Songs Section
            item {
                Text(
                    text = "Todas as Músicas do Artista",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

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
