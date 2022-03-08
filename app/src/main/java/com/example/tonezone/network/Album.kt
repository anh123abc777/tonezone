package com.example.tonezone.network


data class Album(
    val album_type: String,
    val id: String,
    val images: List<Image>,
    val name: String,
    val uri: String
)
