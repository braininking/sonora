package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.db.entities.FavoriteEntity
import com.example.data.db.entities.PlaybackHistoryEntity
import com.example.data.db.entities.PlaylistCount
import com.example.data.db.entities.PlaylistEntity
import com.example.data.db.entities.PlaylistSongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT songId FROM favorites")
    fun getAllFavoriteSongIds(): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    fun isFavorite(songId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun deleteFavorite(songId: Long)
}

@Dao
interface PlaybackHistoryDao {
    @Query("SELECT * FROM playback_history ORDER BY playedAt DESC LIMIT 100")
    fun getRecentHistory(): Flow<List<PlaybackHistoryEntity>>

    @Query("SELECT * FROM playback_history ORDER BY playCount DESC LIMIT :limit")
    fun getTopPlayed(limit: Int = 10): Flow<List<PlaybackHistoryEntity>>

    @Query("SELECT * FROM playback_history WHERE songId = :songId LIMIT 1")
    suspend fun getHistoryEntryForSong(songId: Long): PlaybackHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHistory(entry: PlaybackHistoryEntity)

    @Transaction
    suspend fun recordPlayback(songId: Long, title: String, artist: String, album: String) {
        val existing = getHistoryEntryForSong(songId)
        if (existing != null) {
            insertOrUpdateHistory(
                existing.copy(
                    songTitle = title,
                    artist = artist,
                    album = album,
                    playedAt = System.currentTimeMillis(),
                    playCount = existing.playCount + 1
                )
            )
        } else {
            insertOrUpdateHistory(
                PlaybackHistoryEntity(
                    songId = songId,
                    songTitle = title,
                    artist = artist,
                    album = album,
                    playedAt = System.currentTimeMillis(),
                    playCount = 1
                )
            )
        }
    }

    @Query("DELETE FROM playback_history")
    suspend fun clearHistory()
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    fun getPlaylistById(id: Long): Flow<PlaylistEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :name, updatedAt = :timestamp WHERE id = :id")
    suspend fun renamePlaylist(id: Long, name: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun deletePlaylistSongs(playlistId: Long)

    @Transaction
    suspend fun deletePlaylistWithSongs(playlistId: Long) {
        deletePlaylistSongs(playlistId)
        deletePlaylist(playlistId)
    }

    @Query("SELECT songId FROM playlist_songs WHERE playlistId = :playlistId ORDER BY position ASC, addedAt ASC")
    fun getSongIdsForPlaylist(playlistId: Long): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    fun getPlaylistSongCount(playlistId: Long): Flow<Int>

    @Query("SELECT playlistId, COUNT(songId) as count FROM playlist_songs GROUP BY playlistId")
    fun getPlaylistSongCounts(): Flow<List<PlaylistCount>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylistSong(playlistSong: PlaylistSongEntity)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("UPDATE playlist_songs SET position = :newPosition WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun updateSongPosition(playlistId: Long, songId: Long, newPosition: Int)

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun getNextPosition(playlistId: Long): Int

    @Transaction
    suspend fun addSong(playlistId: Long, songId: Long) {
        val nextPos = getNextPosition(playlistId)
        insertPlaylistSong(
            PlaylistSongEntity(
                playlistId = playlistId,
                songId = songId,
                position = nextPos,
                addedAt = System.currentTimeMillis()
            )
        )
    }
}
