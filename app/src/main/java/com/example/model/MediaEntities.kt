package com.example.model

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val songCount: Int,
    val year: Int = 0
)

data class Artist(
    val name: String,
    val songCount: Int,
    val albumCount: Int = 0
)

data class Folder(
    val name: String,
    val path: String,
    val songCount: Int
)

data class Genre(
    val name: String,
    val songCount: Int
)
