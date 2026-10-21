package com.example.model

import com.example.util.NaturalOrderComparator

enum class SortOrder(val label: String) {
    NAME_ASC("Nome A-Z"),
    NAME_DESC("Nome Z-A"),
    ARTIST_ASC("Artista A-Z"),
    ARTIST_DESC("Artista Z-A"),
    ALBUM_ASC("Álbum A-Z"),
    RECENT("Mais recentes"),
    OLDEST("Mais antigas"),
    DURATION("Duração");

    fun sortSongs(songs: List<Song>): List<Song> {
        return when (this) {
            NAME_ASC -> songs.sortedWith { a, b -> NaturalOrderComparator.compare(a.title, b.title) }
            NAME_DESC -> songs.sortedWith { a, b -> NaturalOrderComparator.compare(b.title, a.title) }
            ARTIST_ASC -> songs.sortedWith { a, b -> NaturalOrderComparator.compare(a.artist, b.artist) }
            ARTIST_DESC -> songs.sortedWith { a, b -> NaturalOrderComparator.compare(b.artist, a.artist) }
            ALBUM_ASC -> songs.sortedWith { a, b -> NaturalOrderComparator.compare(a.album, b.album) }
            RECENT -> songs.sortedByDescending { it.dateAdded }
            OLDEST -> songs.sortedBy { it.dateAdded }
            DURATION -> songs.sortedByDescending { it.durationMs }
        }
    }
}
