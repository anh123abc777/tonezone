package com.example.tonezone.network

data class CategoriesObject (
    val categories: Categories
        )

data class Categories(
    val items: List<Category>
)

data class Category(
    val id: String?="",
    val name: String?="",
    val icons: List<Image>?= listOf(),
//    val playlists: List<String>?= listOf()
)