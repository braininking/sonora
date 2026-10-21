package com.example.data.mediastore

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Folder
import com.example.model.Genre
import com.example.model.Song
import com.example.util.NaturalOrderComparator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.File

class MediaStoreScanner(private val context: Context) {

    private val supportedExtensions = setOf(
        "mp3", "m4a", "aac", "flac", "ogg", "opus", "wav", "aiff", "ape", "wma"
    )

    /**
     * Observes changes in MediaStore audio collection. Emits a unit trigger when changes occur.
     */
    fun observeAudioChanges(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                trySend(Unit)
            }
        }

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        context.contentResolver.registerContentObserver(uri, true, observer)

        // Initial emission
        trySend(Unit)

        awaitClose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }

    /**
     * Scans all audio files on the device using MediaStore.
     */
    suspend fun scanSongs(): List<Song> = withContext(Dispatchers.IO) {
        val songList = mutableListOf<Song>()
        val seenIds = mutableSetOf<Long>()

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projection.add(MediaStore.Audio.Media.RELATIVE_PATH)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            projection.add(MediaStore.Audio.Media.GENRE)
        }

        // Selection: Minimum duration 3 seconds, ignore corrupted or ringtone files
        val selection = "${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf("3000")
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        try {
            val cursor: Cursor? = context.contentResolver.query(
                collection,
                projection.toTypedArray(),
                selection,
                selectionArgs,
                sortOrder
            )

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistCol = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumCol = it.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = it.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val durationCol = it.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val dataCol = it.getColumnIndex(MediaStore.Audio.Media.DATA)
                val dateAddedCol = it.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                val dateModCol = it.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)
                val sizeCol = it.getColumnIndex(MediaStore.Audio.Media.SIZE)
                val yearCol = it.getColumnIndex(MediaStore.Audio.Media.YEAR)
                val trackCol = it.getColumnIndex(MediaStore.Audio.Media.TRACK)

                val relPathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    it.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
                } else -1

                val genreCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    it.getColumnIndex(MediaStore.Audio.Media.GENRE)
                } else -1

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    if (seenIds.contains(id)) continue
                    seenIds.add(id)

                    val filePath = if (dataCol != -1) it.getString(dataCol) ?: "" else ""

                    // Validate file extension if path is available
                    if (filePath.isNotBlank()) {
                        val ext = filePath.substringAfterLast('.', "").lowercase()
                        if (ext.isNotEmpty() && !supportedExtensions.contains(ext)) {
                            continue
                        }
                    }

                    val rawTitle = if (titleCol != -1) it.getString(titleCol) else null
                    val rawArtist = if (artistCol != -1) it.getString(artistCol) else null
                    val rawAlbum = if (albumCol != -1) it.getString(albumCol) else null
                    val albumId = if (albumIdCol != -1) it.getLong(albumIdCol) else -1L
                    val duration = if (durationCol != -1) it.getLong(durationCol) else 0L
                    val dateAdded = if (dateAddedCol != -1) it.getLong(dateAddedCol) else 0L
                    val dateModified = if (dateModCol != -1) it.getLong(dateModCol) else 0L
                    val size = if (sizeCol != -1) it.getLong(sizeCol) else 0L
                    val year = if (yearCol != -1) it.getInt(yearCol) else 0
                    val track = if (trackCol != -1) it.getInt(trackCol) else 0

                    val rawGenre = if (genreCol != -1) it.getString(genreCol) else null
                    val relPath = if (relPathCol != -1) it.getString(relPathCol) else null

                    // Compute Folder Name and Folder Path
                    val (folderName, folderPath) = resolveFolderInfo(filePath, relPath)

                    // Title fallback: If title is empty or unknown, use file name without extension
                    val title = if (!rawTitle.isNullOrBlank() && rawTitle != "<unknown>") {
                        rawTitle.trim()
                    } else if (filePath.isNotBlank()) {
                        File(filePath).nameWithoutExtension
                    } else {
                        "Áudio $id"
                    }

                    val artist = if (!rawArtist.isNullOrBlank() && rawArtist != "<unknown>") {
                        rawArtist.trim()
                    } else {
                        "Artista desconhecido"
                    }

                    val album = if (!rawAlbum.isNullOrBlank() && rawAlbum != "<unknown>") {
                        rawAlbum.trim()
                    } else {
                        "Álbum desconhecido"
                    }

                    val genre = if (!rawGenre.isNullOrBlank() && rawGenre != "<unknown>") {
                        rawGenre.trim()
                    } else {
                        "Geral"
                    }

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    songList.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            albumId = albumId,
                            genre = genre,
                            trackNumber = track,
                            year = year,
                            durationMs = duration,
                            contentUri = contentUri,
                            filePath = filePath,
                            folderName = folderName,
                            folderPath = folderPath,
                            dateAdded = dateAdded,
                            dateModified = dateModified,
                            sizeBytes = size
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Natural sort by title as default
        songList.sortedWith { a, b -> NaturalOrderComparator.compare(a.title, b.title) }
    }

    private fun resolveFolderInfo(filePath: String, relativePath: String?): Pair<String, String> {
        if (filePath.isNotBlank()) {
            val file = File(filePath)
            val parentFile = file.parentFile
            if (parentFile != null) {
                val folderName = parentFile.name
                val folderPath = parentFile.absolutePath
                return Pair(if (folderName.isBlank()) "Armazenamento" else folderName, folderPath)
            }
        }

        if (!relativePath.isNullOrBlank()) {
            val cleaned = relativePath.trim('/').replace('\\', '/')
            val segments = cleaned.split('/')
            val folderName = segments.lastOrNull()?.takeIf { it.isNotBlank() } ?: "Músicas"
            return Pair(folderName, cleaned)
        }

        return Pair("Músicas", "/storage/emulated/0/Music")
    }

    /**
     * Groups songs into Album objects with song counts and natural sort.
     */
    fun extractAlbums(songs: List<Song>): List<Album> {
        return songs.groupBy { it.albumId to it.album }
            .map { (key, albumSongs) ->
                val (albumId, albumTitle) = key
                val artist = albumSongs.firstOrNull()?.artist ?: "Vários Artistas"
                val year = albumSongs.map { it.year }.filter { it > 0 }.maxOrNull() ?: 0
                Album(
                    id = albumId,
                    title = albumTitle,
                    artist = artist,
                    songCount = albumSongs.size,
                    year = year
                )
            }
            .sortedWith { a, b -> NaturalOrderComparator.compare(a.title, b.title) }
    }

    /**
     * Groups songs into Artist objects.
     */
    fun extractArtists(songs: List<Song>): List<Artist> {
        return songs.groupBy { it.artist }
            .map { (artistName, artistSongs) ->
                val albumCount = artistSongs.map { it.album }.distinct().size
                Artist(
                    name = artistName,
                    songCount = artistSongs.size,
                    albumCount = albumCount
                )
            }
            .sortedWith { a, b -> NaturalOrderComparator.compare(a.name, b.name) }
    }

    /**
     * Groups songs into Folder objects.
     */
    fun extractFolders(songs: List<Song>): List<Folder> {
        return songs.groupBy { it.folderPath to it.folderName }
            .map { (key, folderSongs) ->
                val (path, name) = key
                Folder(
                    name = name,
                    path = path,
                    songCount = folderSongs.size
                )
            }
            .sortedWith { a, b -> NaturalOrderComparator.compare(a.name, b.name) }
    }

    /**
     * Groups songs into Genre objects.
     */
    fun extractGenres(songs: List<Song>): List<Genre> {
        return songs.groupBy { it.genre }
            .map { (genreName, genreSongs) ->
                Genre(
                    name = genreName,
                    songCount = genreSongs.size
                )
            }
            .sortedWith { a, b -> NaturalOrderComparator.compare(a.name, b.name) }
    }
}
