package com.example.tonezone.utils

import android.util.Log
import com.example.tonezone.network.Image
import com.example.tonezone.network.Owner
import com.example.tonezone.network.Playlist
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.toObject

fun convertDocToPlaylist(value: DocumentSnapshot): Playlist {
    val playlist = Playlist()
    playlist.name = value.getString("name")
    playlist.id = value.getString("id")
    playlist.owner = value.toObject<Owner>()
    playlist.description = value.getString("description")
    val imagesHashMap = (value.get("images") as List<*>)
    playlist.images = convertHashMapToImage(imagesHashMap)

    Log.i("images","${value.data}")
    Log.i("images","${(value.get("images") as List<Image>)}")
    return playlist
}

fun convertHashMapToImage(imagesHashMap: List<*>): List<Image>{
    val list = mutableListOf<Image>()
    imagesHashMap.forEach {
        val image = it as HashMap<String,*>
        if (image["url"] !=null){
            list.add(
                Image(
                null,
                image.get("url") as String,
                null
                 )
            )
        }
    }
    return list
}