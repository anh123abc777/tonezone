package com.example.tonezone.network

data class UserProfile(
    val country: String?="",
    val display_name: String?="",
    val id: String?="",
    val images: List<Image>?= listOf(),
    val product: String?="",
    val type: String?="",
    val uri: String?=""
)