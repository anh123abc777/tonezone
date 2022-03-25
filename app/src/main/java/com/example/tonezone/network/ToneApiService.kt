package com.example.tonezone.network

import DataPlaylistItems
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
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
//    .addCallAdapterFactory(CoroutineCallAdapterFactory())
//    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

private val retrofitGetString = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
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
    suspend fun getGenresAsync(
        @Header("Authorization") auth: String
    ): Topic

    @GET("me/playlists")
    suspend fun getCurrentUserPlaylistsAsync(
        @Header("Authorization") auth: String
    ): Playlists

    @GET("me/tracks")
    suspend fun getUserSavedTracks(
        @Header("Authorization") auth: String,
        @Query("market") market: String = "VN"
        ): SavedTracks

    @GET("playlists/{playlist_id}/tracks")
    suspend fun getPlaylistItemsAsync(
        @Header("Authorization") auth: String,
        @Path("playlist_id") playlist_Id: String
        ): DataPlaylistItems

    @GET("tracks/{id}")
    suspend fun getTrackAsync(
        @Header("Authorization") auth: String,
        @Path("id") id: String
    ): Track

    @GET("me/following")
    suspend fun getFollowedArtistsAsync(
        @Header("Authorization") auth: String,
        @Query("type") type: String
    ): ArtistsObject

    @GET("artists/{id}/top-tracks")
    suspend fun getArtistTopTracksAsync(
        @Header("Authorization") auth: String,
        @Path("id") idArtist: String,
        @Query("market") market: String
    ): ArtistTopTracks

    @GET("search")
    suspend fun searchForItemAsync(
        @Header("Authorization") auth: String,
        @Query("q") query: String,
        @Query("type") type: String = "track,artist,playlist",
        @Query("market") market: String = "VN",
        ): SearchedItem

    @GET("browse/categories")
    suspend fun getCategoriesAsync(
        @Header("Authorization") auth: String,
        @Query("country") country: String = "VN"
        ): CategoriesObject

    @GET("browse/categories/{category_id}/playlists")
    suspend fun getCategoryPlaylistsAsync(
        @Header("Authorization") auth: String,
        @Path("category_id") category_id: String,
        @Query("country") country: String = "VN"
        ): PlaylistsObject

    @GET("browse/new-releases")
    suspend fun getNewReleasesAsync(
        @Header("Authorization") auth: String,
        @Query("country") country: String = "VN"
    ): AlbumsObject

    @GET("browse/featured-playlists")
    suspend fun getFeaturedPlaylistsAsync(
        @Header("Authorization") auth: String,
        @Query("country") country: String = "VN",
        @Query("locale") locale: String = "sv_VN"
    ): PlaylistsObject

    @GET("browse/categories/toplists/playlists")
    suspend fun getChartsAsync(
        @Header("Authorization") auth: String,
        @Query("country") country: String = "VN",
        @Query("offset") offset: Int = 9
        ): PlaylistsObject

    @GET("playlists/{playlist_id}/followers/contains")
    suspend fun checkUserFollowPlaylist(
        @Header("Authorization") auth: String,
        @Path("playlist_id") playlist_Id: String,
        @Query("ids") ids: String
        ): List<Boolean>

    @PUT("playlists/{playlist_id}/followers")
    suspend fun followPlaylist(
        @Header("Authorization") auth: String,
        @Path("playlist_id") playlist_Id: String,
    )

    @DELETE("playlists/{playlist_id}/followers")
    suspend fun unfollowPlaylist(
        @Header("Authorization") auth: String,
        @Path("playlist_id") playlist_Id: String,
    )

    @GET("me")
    suspend fun getUserProfile(
        @Header("Authorization") auth: String,
        ): UserProfile

    @GET("me/tracks/contains")
    suspend fun checkUserSavedTrack(
        @Header("Authorization") auth: String,
        @Query("ids") ids: String
    ): List<Boolean>

    @PUT("me/tracks")
    suspend fun saveTracksForCurrentUser(
        @Header("Authorization") auth: String,
        @Query("ids") ids: String
        )

    @DELETE("me/tracks")
    suspend fun removeTracksForCurrentUser(
        @Header("Authorization") auth: String,
        @Query("ids") ids: String
    )

    @GET("artists/{id}")
    suspend fun getArtist(
        @Header("Authorization") auth: String,
        @Path("id") id: String
    ): Artist
}

object ToneApi{
    val retrofitService: ToneApiService by lazy{
        retrofit.create(ToneApiService::class.java)
    }

    val retrofitService2: ToneApiService by lazy {
        retrofitGetString.create(ToneApiService::class.java)
    }
}
