package com.example.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val songId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: Long,
    val songTitle: String,
    val artist: String,
    val album: String,
    val playedAt: Long = System.currentTimeMillis(),
    val playCount: Int = 1
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"]
)
data class PlaylistSongEntity(
    val playlistId: Long,
    val songId: Long,
    val position: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)

data class PlaylistCount(
    val playlistId: Long,
    val count: Int
)
