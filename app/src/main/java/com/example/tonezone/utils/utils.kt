package com.example.tonezone.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.tonezone.network.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.pow
import kotlin.math.sqrt

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

fun convertAlbumsToPlaylists(albums: List<Album>): List<Playlist> =
    albums.map {
        Playlist(
            id = it.id!!,
            description = it.album_group!!,
            images = it.images!!,
            name = it.name!!,
            owner = Owner(),
            public = false,
            type = it.type!!,
        )
}

fun convertArtistsToPlaylists(artists: List<Artist>): List<Playlist> =
    artists.map {
        Playlist(
            id = it.id!!,
            description = it.followers?.total.toString(),
            images = it.images!!,
            name = it.name!!,
            owner = Owner(),
            public = false,
            type = it.type!!,
        )
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

    return Glide
        .with(context)
        .asBitmap()
        .load(url)
        .submit().get()
}

fun generateSearchKeywords(inputText: String): List<String>{
    var inputString = inputText.lowercase()
    var keyWords = mutableListOf<String>()

    val words = inputString.split(" ")

    for (word in words){
        var appendString = ""

        //For every character in the whole string
        for(charPosition in inputString.indices){
            appendString += inputString[charPosition].toString()
            keyWords.add(appendString)
        }

        //remove first word from the string
        inputString = inputString.replace("$word ","")

    }

    return keyWords
}

fun convertTypeToField(type: Type): String=
    when(type){
        Type.ARTIST -> "artists"
        Type.PLAYLIST -> "playlists"
        Type.TRACK -> "tracks"
        Type.ALBUM -> "albums"
        else -> "error"
    }

fun subList(list: List<*>):List<List<*>>{
    val sublist = mutableListOf<List<*>>()
    var index = 0
    while ((list.size.toFloat()/10.toFloat())>index.toFloat()){
        if (list.size>(index+1)*10)
            sublist.add(list.subList(index*10,(index+1)*10))
        else
            sublist.add(list.subList(index*10,list.size))
        index++
    }
    return sublist
}

fun cosineSimilarity(vectorA: DoubleArray, vectorB: DoubleArray): Double {
    var dotProduct = 0.0
    var normA = 0.0
    var normB = 0.0
    vectorA.forEachIndexed { i, _ ->
        dotProduct += vectorA[i] * vectorB[i]
        normA += vectorA[i].pow(2.0)
        normB += vectorB[i].pow(2.0)
    }
    return dotProduct / (sqrt(normA) * sqrt(normB))
}



fun sortByValue(unsortMap: Map<String, Double>): Map<String, Double> {

    val list: List<Map.Entry<String, Double>> = LinkedList(unsortMap.entries)

    Collections.sort(list) { (_, value), (_, value1) -> value1.compareTo(value) }

    val sortedMap: MutableMap<String, Double> = LinkedHashMap()
    for ((key, value) in list) {
        sortedMap[key] = value
    }

    return sortedMap
}

fun sortArtistsByValue(unsortMap: Map<Artist, Double>): Map<Artist, Double> {

    val list: List<Map.Entry<Artist, Double>> = LinkedList(unsortMap.entries)

    Collections.sort(list) { (_, value), (_, value1) -> value1.compareTo(value) }

    val sortedMap: MutableMap<Artist, Double> = LinkedHashMap()
    for ((key, value) in list) {
        sortedMap[key] = value
    }

    return sortedMap
}

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}

fun sublistByDay(list: List<FirebaseRepository.History>): List<List<FirebaseRepository.History>>{

    val days = MutableList(7){mutableListOf<FirebaseRepository.History>()}
    list.forEach { history ->
        if (history.type == Type.TRACK)
            days[history.date-1].add(history)
    }
//
//    var i = 0
//    while(i< days.size){
//        if(days[i].isEmpty()){
//            days.remove(days[i])
//        }else{
//            i++
//        }
//    }
    return days
}

fun getArtistNames(artists: List<Artist>): String{
    var artistNames = ""
    artists.forEachIndexed { index, artist ->
        artistNames += artist.name+ if(index!=artists.size-1) ", " else " and more"
    }
    return artistNames
}

