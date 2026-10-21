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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Folder
import com.example.model.Song
import com.example.ui.SearchResult
import com.example.ui.components.AlbumArtImage
import com.example.ui.components.SongItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    query: String,
    searchResult: SearchResult,
    currentSong: Song?,
    isPlaying: Boolean,
    onQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onFolderClick: (Folder) -> Unit,
    onMenuSong: (Song) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("search_screen")
    ) {
        // Search Bar Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCloseSearch,
                modifier = Modifier.testTag("btn_close_search")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Fechar busca"
                )
            }

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Músicas, artistas, álbuns, pastas...") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpar")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_search_query")
            )
        }

        if (query.isBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Digite para buscar na sua biblioteca",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val hasResults = searchResult.songs.isNotEmpty() ||
                    searchResult.albums.isNotEmpty() ||
                    searchResult.artists.isNotEmpty() ||
                    searchResult.folders.isNotEmpty()

            if (!hasResults) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Nenhum resultado para \"$query\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    // Artists matches
                    if (searchResult.artists.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Artistas (${searchResult.artists.size})")
                        }
                        items(searchResult.artists, key = { "search_art_${it.name}" }) { artist ->
                            SearchArtistRow(artist = artist, onClick = { onArtistClick(artist) })
                        }
                    }

                    // Albums matches
                    if (searchResult.albums.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Álbuns (${searchResult.albums.size})")
                        }
                        items(searchResult.albums, key = { "search_alb_${it.id}" }) { album ->
                            SearchAlbumRow(album = album, onClick = { onAlbumClick(album) })
                        }
                    }

                    // Folders matches
                    if (searchResult.folders.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Pastas (${searchResult.folders.size})")
                        }
                        items(searchResult.folders, key = { "search_fld_${it.path}" }) { folder ->
                            SearchFolderRow(folder = folder, onClick = { onFolderClick(folder) })
                        }
                    }

                    // Songs matches
                    if (searchResult.songs.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Músicas (${searchResult.songs.size})")
                        }
                        items(searchResult.songs, key = { "search_sng_${it.id}" }) { song ->
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
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
private fun SearchArtistRow(artist: Artist, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${artist.songCount} música(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SearchAlbumRow(album: Album, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumArtImage(
                albumId = album.id,
                size = 40.dp,
                shape = RoundedCornerShape(6.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${album.artist} • ${album.songCount} faixa(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SearchFolderRow(folder: Folder, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${folder.songCount} música(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
