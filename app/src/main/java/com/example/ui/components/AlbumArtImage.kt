package com.example.ui.components

import android.content.ContentUris
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale

@Composable
fun AlbumArtImage(
    albumId: Long,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val sizePx = remember(size, density) {
        with(density) { size.roundToPx() }
    }

    val artworkUri = remember(albumId) {
        if (albumId > 0) {
            ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"),
                albumId
            )
        } else null
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        // Immediate, zero-cost vector placeholder displayed instantly
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.size(size * 0.5f)
        )

        if (artworkUri != null) {
            AsyncImage(
                model = remember(artworkUri, sizePx) {
                    ImageRequest.Builder(context)
                        .data(artworkUri)
                        .memoryCacheKey("art_$albumId")
                        .size(sizePx, sizePx)
                        .scale(Scale.FILL)
                        .precision(Precision.INEXACT)
                        .crossfade(150)
                        .build()
                },
                contentDescription = "Capa do álbum",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
