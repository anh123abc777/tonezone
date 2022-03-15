package com.example.tonezone.network

import com.example.tonezone.R


data class Playlist(
    val id : String,
    val description: String,
    val href: String,
    val images: List<Image>?=listOf(Image(null,R.drawable.ic_baseline_home_24.toString(),null)),
    val name: String,
    val owner: Owner,
    val primary_color: Int?,
    val public: Boolean?,
    val type: String,
    val uri: String
)
