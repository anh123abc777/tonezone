package com.example.tonezone.network

data class MixPlaylist(
    val id: String = "",
    val name: String = "Mix for you",
    val type: String = "type",
    val tracks: List<Int> = listOf()
)
