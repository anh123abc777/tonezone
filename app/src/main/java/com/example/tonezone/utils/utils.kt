package com.example.tonezone.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.bumptech.glide.Glide
import com.example.tonezone.network.Artist
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

fun displayArtistNames(list: List<Artist>): String{
    var artists = ""
    list.forEachIndexed { index, artist ->
        artists += artist.name+ if(index!=list.size-1) ", " else ""
    }
    return artists
}

suspend fun createBitmapFromUrl(context: Context, url: String): Bitmap {


//
//    var bitmap = BitmapFactory.decodeResource(context.resources,R.drawable.splash_image)
//    Glide.with(context)
//        .asBitmap()
//        .load(url)
//        .into(object : CustomTarget<Bitmap>(){
//            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                bitmap = resource
//            }
//            override fun onLoadCleared(placeholder: Drawable?) {
//            }
//        })

    return Glide
        .with(context)
        .asBitmap()
        .load(url)
        .submit().get()
}