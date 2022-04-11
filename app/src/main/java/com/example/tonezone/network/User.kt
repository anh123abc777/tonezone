package com.example.tonezone.network

data class User(
    val country: String = "",
    val display_name: String = "",
    var id: String = "",
    val images: List<Image> = listOf(),
    val product: String = "",
    val type: String = "user",
    val uri: String = "",
    val email :  String = "",
    )