package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.db.dao.FavoriteDao
import com.example.data.db.dao.PlaybackHistoryDao
import com.example.data.db.dao.PlaylistDao
import com.example.data.db.entities.FavoriteEntity
import com.example.data.db.entities.PlaybackHistoryEntity
import com.example.data.db.entities.PlaylistEntity
import com.example.data.db.entities.PlaylistSongEntity

@Database(
    entities = [
        FavoriteEntity::class,
        PlaybackHistoryEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SonoraDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playbackHistoryDao(): PlaybackHistoryDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: SonoraDatabase? = null

        fun getInstance(context: Context): SonoraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SonoraDatabase::class.java,
                    "sonora_music.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
