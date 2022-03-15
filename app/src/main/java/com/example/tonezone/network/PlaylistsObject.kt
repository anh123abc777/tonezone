package com.example.tonezone.network

data class PlaylistsObject(
    val playlists: Playlists
)

data class Playlists(
    val items: List<Playlist>?= listOf(),
)
