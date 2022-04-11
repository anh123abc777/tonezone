package com.example.tonezone.network

data class GroupPlaylist(
    val title: String,
    val playlists: List<Playlist>?= listOf(),
    val albums: List<Album>? = listOf()
)
