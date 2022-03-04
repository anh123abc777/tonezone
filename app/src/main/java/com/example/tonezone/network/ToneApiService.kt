package com.example.tonezone.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

private const val BASE_URL = "https://api.spotify.com/v1/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
//    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface ToneApiService {
    @GET("albums/4aawyAB9vmqN3uQ7FjRGTy/tracks")
    fun getAlbumTracksAsync(
        @Header("Authorization") auth: String,
        @Query("market") market: String) : Deferred<SpotifyData>

//    @PUT("me/player/play")
//    fun play(
//        @Header("Authorization") auth: String,
//        @Body context_uri: String,
//        @Query("device_id") device_id: String
//    ): Deferred<String>

    @GET("recommendations/available-genre-seeds")
    fun getGenresAsync(
        @Header("Authorization") auth: String
    ): Deferred<Topic>

    @GET("me/playlists")
    fun getCurrentUserPlaylistsAsync(
        @Header("Authorization") auth: String
    ): Deferred<UserPlaylists>
}

object ToneApi{
    val retrofitService: ToneApiService by lazy{
        retrofit.create(ToneApiService::class.java)
    }
}
