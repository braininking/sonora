package com.example.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val genre: String = "",
    val trackNumber: Int = 0,
    val year: Int = 0,
    val durationMs: Long = 0L,
    val contentUri: Uri,
    val filePath: String = "",
    val folderName: String = "",
    val folderPath: String = "",
    val dateAdded: Long = 0L,
    val dateModified: Long = 0L,
    val sizeBytes: Long = 0L,
    val isFavorite: Boolean = false
) {
    val durationFormatted: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val hours = minutes / 60
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes % 60, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }
}
