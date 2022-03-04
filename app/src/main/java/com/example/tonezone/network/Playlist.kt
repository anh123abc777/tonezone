package com.example.tonezone.network

data class Playlist(
    val id : String,
    val description: String,
    val href: String,
    val images: List<Image>,
    val name: String,
    val owner: Owner,
    val primary_color: Int?,
    val public: Boolean,
    val type: String,
    val uri: String
)
