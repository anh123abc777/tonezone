package com.example.tonezone.network


data class Playlist(
    var id : String?="",
    var description: String?="",
    var images: List<Image>?= listOf(Image(null,null,null)),
    var name: String? = "",
    var owner: Owner? = Owner(),
    var public: Boolean? = false,
    var type: String? = "playlist",
    var tracks: List<TrackInPlaylist>? = listOf(),
    var deltailTracks : List<Track>?= listOf()
)
