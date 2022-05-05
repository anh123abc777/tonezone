package com.example.tonezone.home

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.network.*
import com.example.tonezone.utils.*
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import java.util.*


class HomeViewModel(val token: String, val user: User) : ViewModel() {

    private val firebaseRepo = FirebaseRepository()

    private var _groupPlaylists = firebaseRepo.getDataHomeScreen(user)
    val groupPlaylists : LiveData<List<GroupPlaylist>>
        get() = _groupPlaylists

    private val _navigateToPlaylistDetails = MutableLiveData<PlaylistInfo>()
    val navigateToPlaylistDetails : LiveData<PlaylistInfo>
        get() = _navigateToPlaylistDetails

    private val job = Job()
//    private val uiScope = CoroutineScope(job + Dispatchers.Main)

    init {
        getRelateArtists()
        getYourArtists()
        getRecentlyPlaylists()
//        getRecommendedTracks()
//        getGroupPlaylistsData()
    }

     fun displayPlaylistDetails(playlistInfo: PlaylistInfo){
        _navigateToPlaylistDetails.value = playlistInfo
    }

    @SuppressLint("NullSafeMutableLiveData")
     fun displayPlaylistDetailsComplete(){
        _navigateToPlaylistDetails.value = null
    }

    fun getRecommendedTracks(){
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

        firebaseRepo.db.collection("Recommendation")
            .document(user.id)
            .get()
            .addOnSuccessListener { docs ->
                if (docs != null && docs.exists()) {
                    val tracks = docs.data
                    Log.i("HomeViewModel","ys $tracks")
                    val temp = mutableListOf<GroupPlaylist>()
                    temp.addAll(_groupPlaylists.value?: listOf())
                    temp.add(GroupPlaylist("Mix for you",
                        listOf(Playlist(id="Day$today",
                        "Mix for ${user.display_name}",
                                name = "Mix #$today"
                            ))))
                    _groupPlaylists.value = temp
                }
            }
    }

    private fun getRecentlyPlaylists(){
        firebaseRepo.db.collection("History")
            .document(user.id)
            .addSnapshotListener { value, error ->
                if (value != null && value.exists() && value.get("playlists") != null) {
                    val result = value.get("playlists") as List<HashMap<*, *>>

                    val playlistIDs = mutableListOf<String>()
                    result.forEach { item ->
                        if (playlistIDs.size <= 10 && !playlistIDs.contains(item["id"])) {
                            playlistIDs.add(item["id"].toString())
                        }
                    }

                    firebaseRepo.db.collection("Playlist")
                        .whereIn("id", playlistIDs)
                        .addSnapshotListener { result,_ ->
                            if (result != null && !result.isEmpty) {
                                val playlists = result.toObjects(Playlist::class.java)
                                val group = GroupPlaylist("Recently Playlist", playlists)

                                val recentlyPlayedPlaylists =
                                    _groupPlaylists.value?.find { it.title == "Recently playlist" }

                                if (recentlyPlayedPlaylists != null)
                                    _groupPlaylists.value?.get(
                                        _groupPlaylists.value!!.indexOf(
                                            recentlyPlayedPlaylists
                                        )
                                    )?.playlists = playlists
                                else {

                                    val temp = mutableListOf<GroupPlaylist>()
                                    temp += _groupPlaylists.value?: listOf()
                                    temp.add(group)
                                    _groupPlaylists.value = temp

//                                    groupPlaylists.postValue(
//                                        groupPlaylists.value?.plus(group)
//                                    )
//                                        _groupPlaylists.value = _groupPlaylists.value?.plus(group)
                                }
                            }
                        }
                }
            }

    }

    private fun getYourArtists(){
        firebaseRepo.db.collection("User")
            .document(user.id)
            .collection("Followed")
            .addSnapshotListener { items, _ ->
                if (items != null) {
                    val artistIDGroups = subList(
                        items.toObjects(FirebaseRepository.FollowedObject::class.java)
                            .filter { it.type == Type.ARTIST.name }.map { it.id })
                    val listData = mutableListOf<Artist>()
                    artistIDGroups.forEach { artistIDs ->

                        firebaseRepo.db.collection("Artist")
                            .whereIn("id", artistIDs)
                            .addSnapshotListener { documents, _ ->
                                if (documents != null) {
                                    listData += documents.toObjects(Artist::class.java)
                                    val artists = convertArtistsToPlaylists(listData)

                                    val group = GroupPlaylist("Your favorite artists", artists)

                                    val recentlyPlayedPlaylists =
                                        _groupPlaylists.value?.find { it.title == "Your favorite artists" }

                                    if (recentlyPlayedPlaylists != null) {
                                        _groupPlaylists.value?.get(
                                            _groupPlaylists.value!!.indexOf(
                                                recentlyPlayedPlaylists
                                            )
                                        )?.playlists = artists


                                    } else {
                                        val temp = mutableListOf<GroupPlaylist>()
                                        temp += groupPlaylists.value ?: listOf()
                                        temp.add(group)
                                        _groupPlaylists.value = temp

                                    }
                                }
                            }
                    }
                }
            }
    }

    private fun getRelateArtists(){

        runBlocking(Dispatchers.IO) {
            firebaseRepo.db.collection("User")
                .document(user.id)
                .collection("Followed")
                .get()
                .addOnSuccessListener { docs ->
                    if (docs != null && !docs.isEmpty()) {
                        Log.i("Relateartists","$docs")

                        //get id's artists was followed
                        val artistIds = subList(docs.toObjects(FirebaseRepository.FollowedObject::class.java).filter { it.type=="ARTIST"}.map { it.id })
                        Log.i("Relateartists","$artistIds")

                        artistIds.forEach { ids ->
                            firebaseRepo.db.collection("Artist")
                                .whereIn("id", ids)
                                .get()
                                .addOnSuccessListener { results ->
                                    if (results != null && !results.isEmpty) {
                                        val artists = results.toObjects(Artist::class.java)

                                        //create userProfiles array for calculate cosines similarity
                                        val userProfiles = createUserProfile(artists)
                                        val genres = createGenres(artists)
                                        val itemProfiles = createItemProfile(artists,genres)

                                        val sortedMap = calculateCosineSimilarity(userProfiles,itemProfiles,genres)
                                        getArtistsBaseOnRelateGenres(sortedMap,artists)
                                    }
                                }
                        }
                    }
                }
        }
    }

    private fun getArtistsBaseOnRelateGenres(sortedMap: Map<String,Double>, artists: List<Artist>){
        firebaseRepo.db.collection("Artist")
            .whereArrayContainsAny("genres",
                sortedMap.keys.toList().subList(0,3))
            .get()
            .addOnSuccessListener { results ->
                if (results!=null && !results.isEmpty){
                    val relateArtists = results.toObjects(Artist::class.java)
                    artists.forEach {
                        relateArtists.remove(it)
                    }
                    relateArtists.sortByDescending { it.followers?.total }

                    val temp = mutableListOf<GroupPlaylist>()
                    temp += _groupPlaylists.value?: listOf()
                    temp.add(GroupPlaylist("Recommended artists",
                        convertArtistsToPlaylists(relateArtists.subList(0,8))))
                    _groupPlaylists.value = temp
                    Log.i("Relateartists","$temp")
                }
            }
    }

    private fun createUserProfile(artists: List<Artist>): DoubleArray{
        val userProfiles = arrayListOf<Double>()
        artists.forEach { artist ->
            userProfiles.add(
                (artist.followers?.total?.toDouble() ?: 1.0)
            )
        }
        // calculate value of each item in userProfile to decrease the value
        val average = userProfiles.average()
        userProfiles.onEach { it / average }
        return userProfiles.toDoubleArray()
    }

    private fun createItemProfile(artists: List<Artist>,genres: MutableList<String>): Array<Array<Double>>{

        //create itemProfiles array for calculate cosines similarity
        val itemProfiles = Array(genres.size) { Array(artists.size) { 0.0 } }

        genres.forEachIndexed { genreIndex, genre ->
            artists.forEachIndexed { artistIndex, artist ->
                if (artist.genres != null && artist.genres.contains(genre))
                    itemProfiles[genreIndex][artistIndex] = 1.0
            }
        }
        return itemProfiles
    }

    private fun createGenres(artists: List<Artist>): MutableList<String>{
        val genres = mutableListOf<String>()
        artists.forEach { artist ->
            if (artist.genres != null) {
                artist.genres.forEach { genre ->
                    if (!genres.contains(genre))
                        genres.add(genre)
                }
            }
        }
        return genres
    }

    private fun calculateCosineSimilarity(
        userProfiles: DoubleArray,
        itemProfiles: Array<Array<Double>>,
        label: MutableList<String>
    ): Map<String, Double> {
        //calculate cosine similarity of each genre
        val cosineSimilarityArray = itemProfiles.map {
            cosineSimilarity(
                it.toDoubleArray(),
                userProfiles
            )
        }

        val unsortMap = mutableMapOf<String, Double>()
        cosineSimilarityArray.forEachIndexed { index, value ->
            unsortMap[label[index]] = value
        }

        return sortByValue(unsortMap.toMap())
    }


    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }
}