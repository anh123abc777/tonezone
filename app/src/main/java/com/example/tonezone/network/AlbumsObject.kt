package com.example.tonezone.network

data class AlbumsObject(
    val albums: Albums
)

data class Albums(
    val items: List<Album> = listOf()
)

data class SavedAlbums(
    val items: List<SavedAlbum> = listOf()
)

data class SavedAlbum(
    val album: Album,
    val added_at: String,
)

data class Album(
    val id: String?="",
    val album_type: String?="",
    val images: List<Image>?= listOf(),
    val name: String?="",
    val uri: String?="",
    val artists: List<Artist>? = listOf(),
    val release_date: String? = "",
    val type: String?="",
    val album_group: String?=""
)
