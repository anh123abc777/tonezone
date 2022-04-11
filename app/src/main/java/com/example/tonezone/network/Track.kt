package com.example.tonezone.network

data class Tracks(
    var items: List<Track>?= listOf()
)

data class SavedTracks(
    val items: List<SavedTrack>? = listOf()
)

data class SavedTrack(
    val track: Track,
    val added_at: String
)

data class Track (
    val artists : List<Artist>? = listOf(),
    val duration_ms : Long? = 0L,
    val id : String = "",
    val name : String="",
    val preview_url : String? = "",
    val type : String = "",
    val uri : String = "",
    val album: Album?=Album()
)

data class TrackInPlaylist(
    val id: String?="",
    val added_at: String?="",
)