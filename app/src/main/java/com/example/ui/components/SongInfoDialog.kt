package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Song

@Composable
fun SongInfoDialog(
    song: Song,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AlbumArtImage(
                    albumId = song.albumId,
                    size = 44.dp,
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Informações do Áudio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoField(label = "Título", value = song.title)
                InfoField(label = "Artista", value = song.artist)
                InfoField(label = "Álbum", value = song.album)
                InfoField(label = "Gênero", value = song.genre.ifBlank { "Não informado" })
                InfoField(label = "Duração", value = song.durationFormatted)
                if (song.year > 0) {
                    InfoField(label = "Ano", value = song.year.toString())
                }
                if (song.trackNumber > 0) {
                    InfoField(label = "Faixa", value = song.trackNumber.toString())
                }
                if (song.sizeBytes > 0) {
                    val sizeMb = song.sizeBytes.toFloat() / (1024 * 1024)
                    InfoField(label = "Tamanho", value = String.format("%.2f MB", sizeMb))
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                InfoField(
                    label = "Pasta",
                    value = song.folderName
                )
                if (song.filePath.isNotBlank()) {
                    InfoField(
                        label = "Caminho do Arquivo",
                        value = song.filePath
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_info_ok")
            ) {
                Text("Fechar")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun InfoField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
