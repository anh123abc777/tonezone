package com.example.tonezone.network

data class SearchedItem(
    val playlists: Playlists?= Playlists(),
    val artists: Artists?= Artists(),
    val tracks: Tracks?=Tracks()
)
