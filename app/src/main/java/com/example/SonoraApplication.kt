package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.data.db.SonoraDatabase
import com.example.data.mediastore.MediaStoreScanner
import com.example.data.repository.MusicRepository
import com.example.playback.MusicPlayerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SonoraApplication : Application(), ImageLoaderFactory {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var database: SonoraDatabase
        private set

    lateinit var scanner: MediaStoreScanner
        private set

    lateinit var repository: MusicRepository
        private set

    lateinit var playerController: MusicPlayerController
        private set

    override fun onCreate() {
        super.onCreate()

        database = SonoraDatabase.getInstance(this)
        scanner = MediaStoreScanner(this)
        repository = MusicRepository(this, scanner, database, applicationScope)
        playerController = MusicPlayerController(this, applicationScope) { playedSong ->
            applicationScope.launch {
                repository.recordPlayback(playedSong)
            }
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("sonora_art_cache"))
                    .maxSizePercent(0.05)
                    .build()
            }
            .crossfade(150)
            .respectCacheHeaders(false)
            .allowHardware(true)
            .build()
    }
}
