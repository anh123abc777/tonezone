package com.example.tonezone.network

data class UserPlaylists(
    val href: String,
    val items: List<Playlist>,
    val next: String?,
    val total: Int,
    val previous: String?
)
