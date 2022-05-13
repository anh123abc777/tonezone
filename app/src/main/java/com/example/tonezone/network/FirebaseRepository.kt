package com.example.tonezone.network

import TrackInPlaylist
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.utils.*
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.collections.HashMap

class FirebaseRepository {

    val db = Firebase.firestore
    init {
        db.firestoreSettings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build()
    }

    /** track **/
    fun getAllTracks(){
        val tracks = mutableListOf<Track>()
        db.collection("Track").get()
            .addOnSuccessListener { documents ->
                for (document in documents){
                    val track = document.toObject<Track>()
//                    val map = hashMapOf(
//                        "id" to track.id,
//                        "type" to track.type,
//                        "search_keywords" to generateSearchKeywords(track.name!!)
//                    )
//                    db.collection("Search")
//                        .document(track.id!!)
//                        .set(map)
                }
            }
            .addOnFailureListener {
                Log.i("getTracks","Failure $it")
            }
    }

//    fun insertTracks(tracks: List<Track>){
//        tracks.forEach { track ->
//            insertTrack(track)
////            insertArtists(track.artists?: listOf())
//            track.album?.let { insertAlbum(it) }
//        }
//    }

     private fun insertTrack(track: Track){
         if (track.preview_url!=null) {
             val trackInserting = TrackInserting(
                 artists = track.artists?.map { ArtistInTrack(it.id,it.name) },
                 duration_ms = track.duration_ms,
                 id = track.id,
                 name = track.name,
                 preview_url = track.preview_url,
                 type = track.type,
                 uri = track.uri,
                 album = track.album
             )
             db.collection("Track")
                 .document(trackInserting.id)
                 .set(trackInserting)
                 .addOnFailureListener {
                     Log.i("insertTrack", "Failure $it")
                 }
         }
    }

    data class TrackInserting(
        val artists : List<ArtistInTrack>?= listOf(),
        val duration_ms : Long? = 0L,
        val id : String = "",
        val name : String="",
        val preview_url : String? = "",
        val type : String = "track",
        val uri : String = "",
        val album: Album?=Album()
    )

    data class ArtistInTrack(
        val id : String?="",
        val name : String?="",
    )

    fun getTracksOfArtist(artist: Artist): MutableLiveData<List<Track>>{
        val artistInTrack = ArtistInTrack(artist.id,artist.name)
        val tracks = MutableLiveData<List<Track>>()
        db.collection("Track")
            .whereArrayContains("artists",artistInTrack)
            .addSnapshotListener { value, error ->

                if (error!=null)
                    return@addSnapshotListener

                if (value!=null){
                    tracks.value = value.toObjects(Track::class.java)
                }
            }

        return tracks
    }

    fun getTracksOfArtist(id: String, name: String): MutableLiveData<List<Track>>{
        val artistInTrack = ArtistInTrack(id,name)
        val tracks = MutableLiveData<List<Track>>()
        db.collection("Track")
            .whereArrayContains("artists",artistInTrack)
            .addSnapshotListener { value, error ->

                if (error!=null)
                    return@addSnapshotListener

                if (value!=null){
                    tracks.value = value.toObjects(Track::class.java)

//                    tracks.value!!.forEach {
//                        val map = hashMapOf(
//                            "id" to it.id,
//                            "type" to it.type,
//                            "search_keywords" to generateSearchKeywords(it.name)
//                        )
//                        db.collection("Search")
//                            .document(it.id)
//                            .set(map)
//                    }
                }
            }

        return tracks
    }

    fun getTracksOfPlaylist(id: String): MutableLiveData<List<Track>>{
        var tracks = MutableLiveData<List<Track>>()
               db.collection("Playlist")
                   .document(id)
                   .addSnapshotListener { value, _ ->

                       if (value != null && value.exists()) {
                           val itemIDs =
                               convertHashMapToTrackInPlaylist(value.get("tracks") as List<*>)
                           var subListItemIDs = subList(itemIDs)

                           val listData = mutableListOf<Track>()
                           subListItemIDs.forEach { itemIDs ->

                               db.collection("Track")
                                   .whereIn("id", itemIDs)
                                   .addSnapshotListener { value, _ ->
                                       if (value != null) {
                                           val tracksRaw = value.toObjects(Track::class.java)
                                           tracksRaw.forEach { track ->
                                               if (!listData.contains(track))
                                                   listData.add(track)
                                           }
                                           tracks.value = listData
                                       }
                                   }
                           }

                       }
                   }
        return tracks
    }

    private fun convertHashMapToTrackInPlaylist(tracksHashMap: List<*>): List<String>{
        val list = mutableListOf<String>()
        tracksHashMap.forEach {
            val track = it as HashMap<String,String>
                list.add(
                    track["id"]!!
                )
        }
        return list
    }


    /** artist **/

//    fun insertArtists(artists: List<Artist>){
//        artists.forEach { artist ->
////            insertArtist(artist)
//        }
//    }

//    suspend fun insertArtist(artist: Artist){
//        db.collection("Artist")
//            .document(artist.id!!)
//            .set(artist)
//    }

    fun getArtist(id: String): MutableLiveData<Artist>{
        val artist = MutableLiveData<Artist>()
        db.collection("Artist")
            .document(id)
            .addSnapshotListener { value, error ->
                if (error!=null){
                    return@addSnapshotListener
                }

                if(value!=null && value.exists()){
                    artist.value = value.toObject<Artist>()
                }
            }
        return artist
    }

    fun getSeveralArtists(ids: List<String>): MutableLiveData<List<Artist>>{
        val artists = MutableLiveData<List<Artist>>()
        db.collection("Artist")
            .whereIn("id",ids)
            .get()
            .addOnSuccessListener{ documents ->
                if (documents!=null){
                    artists.value = documents.toObjects(Artist::class.java)
                }
            }
        return artists
    }

    /** playlist **/

//    fun insertPlaylist(playlists: List<Playlist>){
//        playlists.forEach { playlist ->
//
//            db.collection("Playlist")
//                .document(playlist.id!!)
//                .set(playlist, SetOptions.merge())
//        }
//    }

    fun getRecentPlaylists(user: User): MutableLiveData<GroupPlaylist>{

        val mutableLiveData = MutableLiveData<GroupPlaylist>()
        db.collection("User")
            .document(user.id)
            .collection("History")
            .whereEqualTo("type",Type.PLAYLIST)
            .addSnapshotListener { value, error ->
                if (value != null && !value.isEmpty()) {
                    val result = value.toObjects(FirebaseRepository.History::class.java)

                    val playlistIDs = mutableListOf<String>()
                    result.forEach { item ->
                        if (playlistIDs.size <= 10 && !playlistIDs.contains(item.id)) {
                            playlistIDs.add(item.id)
                        }
                    }

                    db.collection("Playlist")
                        .whereIn("id", playlistIDs)
                        .addSnapshotListener { result,_ ->
                            if (result != null && !result.isEmpty) {
                                val playlists = result.toObjects(Playlist::class.java)
                                val group = GroupPlaylist("Recently Playlist", playlists)
                                mutableLiveData.value = group
                                }
                            }
                        }
                }
        return mutableLiveData

    }


    fun removeTrackFromPlaylist(playlistID: String, trackID: String){

        db.collection("Playlist")
            .document(playlistID)
            .get()
            .addOnSuccessListener { doc ->
                if (doc!=null && doc.exists()){
                    val trackInPlaylist = (doc["tracks"] as List<HashMap<*,*>>).find { it["id"] == trackID }

                    db.collection("Playlist")
                        .document(playlistID)
                        .update("tracks",FieldValue.arrayRemove(trackInPlaylist))

                }
            }
    }

    fun addItemToSystemPlaylist(playlistID: String,tracks: List<TrackInPlaylist>){
        val dataInserting = tracks.map {
            TrackInPlaylist(
                id = it.track.id,
                added_at = it.added_at
            )
        }

            db.collection("Playlist")
                .document(playlistID)
                .set(hashMapOf("tracks" to dataInserting), SetOptions.mergeFields("tracks"))

    }

    fun deletePlaylist(userID: String,id: String){
        db.collection("Playlist")
            .document(id)
            .delete()

        unfollowObject(userID,id)
    }

    private fun createPlaylist(playlist: Playlist): Task<Void> {

        return db.collection("Playlist")
            .document()
            .set(playlist)
    }

    fun createPlaylist(name: String,user: User): Playlist {
        val playlist = Playlist(
            name = name,
            type = "playlist",
            owner = Owner(user.display_name,user.id),
            id = user.id
        )
        db.collection("Playlist")
            .document()
            .set(playlist)
            .addOnSuccessListener {

                db.collection("Playlist")
                    .whereEqualTo("id",user.id)
                    .addSnapshotListener { documents, _ ->
                        if (documents!=null){
                            for (doc in documents){
                                playlist.id = doc.id
                                db.collection("Playlist")
                                    .document(doc.id)
                                    .set(playlist)

                                followObject(user.id,doc.id,Type.PLAYLIST)
                            }
                        }
                    }
            }

        return playlist
    }

//    fun getAllPlaylists(){
//        db.collection("Playlist")
//            .get()
//            .addOnSuccessListener {docs ->
//                for(doc in docs){
//                    val playlist = convertDocToPlaylist(doc)
////                    val map = hashMapOf(
////                        "id" to playlist.id,
////                        "type" to playlist.type,
////                        "search_keywords" to generateSearchKeywords(playlist.name!!)
////                    )
////                    db.collection("Search")
////                        .document(playlist.id!!)
////                        .set(map)
//                }
//            }
//    }

    fun getPlaylist(id: String?): MutableLiveData<Playlist>{
        val playlist = MutableLiveData<Playlist>()
        if (id!=null && id!="")
            db.collection("Playlist")
                .document(id)
                .addSnapshotListener { value, error ->
                    if (error!=null){
                        return@addSnapshotListener
                    }

                    if(value!=null && value.exists()){
                        playlist.value = value.toObject(Playlist::class.java)
                    }
                }
        return playlist
    }

    fun addItemToYourPlaylist(playlistID: String, newTrackIDs: List<String>){

        val date = Calendar.getInstance().time.toString()
        newTrackIDs.forEach {
            val item = hashMapOf("added_at" to date,"id" to it)
            db.collection("Playlist")
                .document(playlistID)
//            .set(hashMapOf("tracks" to newTrackIDs), SetOptions.mergeFields("tracks"))
                .update("tracks", FieldValue.arrayUnion(item))
        }
    }

    fun getSeveralPlaylist(ids: List<String>): MutableLiveData<List<Playlist>>{
        val playlists = MutableLiveData<List<Playlist>>()

        db.collection("Playlist")
            .whereIn("id",ids)
            .addSnapshotListener { documents, error ->

                if (documents!=null){
                    val listData = mutableListOf<Playlist>()
                    for (doc in documents){
                        val playlist = Playlist()
                        playlist.name = doc.getString("name")
                        playlist.id = doc.getString("id")
                        playlist.images = doc.get("images") as List<Image>
                        playlist.owner = doc.toObject<Owner>()
                        playlist.description = doc.getString("description")

                    }
                    playlists.value =listData
                }
            }

        return playlists
    }

    fun getFeaturePlaylists(): MutableLiveData<List<Playlist>>{
        val playlists = MutableLiveData<List<Playlist>>()

        db.collection("System")
            .document("SystemTopic")
            .get()
            .addOnSuccessListener { results ->
                if (results!=null){
                    val featurePlaylistIDs = results.get("featured_playlists") as List<*>
                    val listData = mutableListOf<Playlist>()

                    db.collection("Playlist")
                        .whereIn("id",featurePlaylistIDs)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents!=null){
                                for(doc in documents){
                                    listData.add(convertDocToPlaylist(doc))
                                }

                                playlists.value = listData
//                                playlists.value!!.forEach {
//                                    val map = hashMapOf(
//                                        "id" to it.id,
//                                        "type" to it.type,
//                                        "se   arch_keywords" to generateSearchKeywords(it.name!!)
//                                    )
//                                    db.collection("Search")
//                                        .document(it.id!!)
//                                        .set(map)
//                                }
                            }
                        }
                }
            }

        return playlists
    }

//    fun insertSystem(ids: List<String>,systemTopic: String){
//            db.collection("System")
//                .document("SystemTopic")
//                .set(hashMapOf(systemTopic to ids), SetOptions.mergeFields(systemTopic))
//                .addOnFailureListener {
//                    Log.i("insertSystem","$it")
//                }
//    }

    enum class SystemTopic{
        featured_playlists, new_releases, charts
    }

    /** album **/

//    fun insertAlbums(albums: List<Album>){
//        albums.forEach { album ->
//            insertAlbum(album)
////            insertArtists(album.artists?: listOf())
//        }
//    }

//    fun insertAlbum(album: Album){
//            album.id?.let {
//                db.collection("Album")
//                    .document(it)
//                    .set(album)
//            }
//    }

    fun getTracksOfAlbum(album: Album): MutableLiveData<List<Track>>{
        val tracks = MutableLiveData<List<Track>>()

        db.collection("Track")
            .whereEqualTo("album",album)
            .get()
            .addOnSuccessListener { results ->
                if (results!=null){
                    tracks.value = results.toObjects(Track::class.java)
                }
            }

        return tracks
    }

    fun getAlbumsOfArtist(artist: Artist): MutableLiveData<List<Album>>{
        val albums = MutableLiveData<List<Album>>()

        db.collection("Album")
            .whereArrayContains("artists",artist)
            .get()
            .addOnSuccessListener {
                if (it!=null){
                    albums.value = it.toObjects(Album::class.java)
                }
            }

        return albums
    }

    fun getAlbumsOfArtist(artistID: String): MutableLiveData<List<Album>>{
        val albums = MutableLiveData<List<Album>>()

        db.collection("Artist")
            .document(artistID)
            .get()
            .addOnSuccessListener { artistDoc ->

                if (artistDoc!=null) {

                    val artist = artistDoc.toObject(Artist::class.java)

                    artist?.let {
                        db.collection("Album")
                            .whereArrayContains("artists", it)
                            .get()
                            .addOnSuccessListener {
                                if (it!=null)
                                    albums.value = it.toObjects(Album::class.java)
                            }
                    }
                }
            }
        return albums
    }

    fun getTracksOfAlbum(id: String): MutableLiveData<List<Track>>{
        Log.i("Observer",id)
        val tracks = MutableLiveData<List<Track>>()

                db.collection("Track")
                    .whereEqualTo("album.id",id)
                    .get()
                    .addOnSuccessListener { results ->
                        if (!results.isEmpty) {
                            tracks.value = results.toObjects(Track::class.java)
                        }
            }

        return tracks
    }

    fun getAlbumReleases(): MutableLiveData<List<Album>>{
        val albums = MutableLiveData<List<Album>>()

        db.collection("System")
            .document("SystemTopic")
            .get()
            .addOnSuccessListener { results ->
                if (results!=null){
                    val newReleasesIDs = results.get("new_releases") as List<*>

                    db.collection("Album")
                        .whereIn("id",newReleasesIDs)
                        .get()
                        .addOnSuccessListener { documents ->
                                albums.value = documents.toObjects(Album::class.java)
//                            albums.value!!.forEach {
//                                val map = hashMapOf(
//                                    "id" to it.id,
//                                    "type" to it.type,
//                                    "search_keywords" to generateSearchKeywords(it.name!!)
//                                )
//                                db.collection("Search")
//                                    .document(it.id!!)
//                                    .set(map)
//                            }
                            }

                }
            }

        return albums
    }

    /** user **/

    fun addUser(user: User){
        var user = user
        db.collection("User")
            .document()
            .set(user)

        db.collection("User")
            .whereEqualTo("id",user.id)
            .addSnapshotListener { documents, error ->
                if (documents!=null){
                    for (doc in documents){
                        user.id = doc.id
                        db.collection("User")
                            .document(doc.id)
                            .set(user)
                    }
                }
            }

    }

    fun getUserProfile(userID: String): MutableLiveData<User>{
        var user = MutableLiveData<User>()
        db.collection("User")
            .document(userID)
            .addSnapshotListener { document, error ->

                if (error!=null){
                    return@addSnapshotListener
                }

                if (document!=null){
                    user.value = document.toObject<User>()
                }
            }
        return user
    }

    /** Follow **/

    fun getFollowedObjects(userID: String): MutableLiveData<List<LibraryAdapter.DataItem>>{
        val dataItems = MutableLiveData<List<LibraryAdapter.DataItem>>()
        val list = mutableListOf<Pair<LibraryAdapter.DataItem,Boolean>>()

        db.collection("User")
            .document(userID)
            .collection("Followed")
            .orderBy("pin",Query.Direction.DESCENDING)
            .addSnapshotListener { value, _ ->
                list.clear()
                if (value!=null && !value.isEmpty){
                    val followedObjectsRaw = value.toObjects(FollowedObject::class.java)
                    followedObjectsRaw.forEach { item ->
                        when(item.type) {
                            Type.TRACK.name -> {
                                db.collection("Track")
                                    .whereEqualTo("id", item.id)
                                    .get()
                                    .addOnCompleteListener { doc ->
                                        if (doc.isSuccessful) {
                                            val track = doc.result.toObjects(Track::class.java)
                                            if (list.find { it.first.id == "liked_track" }==null) {

                                                list += listOf(
                                                    Pair(
                                                        LibraryAdapter.DataItem.PlaylistItem(
                                                            Playlist(
                                                                id = "liked_track",
                                                                name = "Liked Tracks",
                                                                images = track[0].album?.images,
                                                            ),true
                                                        ),
                                                        true
                                                    )
                                                )
                                                list.sortBy { it.first.name }
                                                list.sortByDescending { it.second }
                                                dataItems.value = list.map { it.first }
                                            }
                                        }
                                    }
                            }

                            Type.ALBUM.name -> {
                                db.collection("Album")
                                    .whereEqualTo("id", item.id)
                                    .get()
                                    .addOnCompleteListener { doc ->
                                        if (doc.isSuccessful) {
                                            val album = doc.result.toObjects(Album::class.java)
                                            if (!list.contains(Pair(LibraryAdapter.DataItem.AlbumItem(album[0],item.pin),item.pin))) {
                                                list += listOf(
                                                    Pair(
                                                        LibraryAdapter.DataItem.AlbumItem(
                                                            album[0],
                                                            item.pin
                                                        ), item.pin
                                                    )
                                                )
                                                list.sortBy { it.first.name }
                                                list.sortByDescending { it.second }
                                                dataItems.value = list.map { it.first }
                                            }
                                        }
                                    }
                            }

                            Type.ARTIST.name -> {
                                db.collection("Artist")
                                    .whereEqualTo("id", item.id)
                                    .get()
                                    .addOnCompleteListener { doc ->
                                        if (doc.isSuccessful) {
                                            val artist = doc.result.toObjects(Artist::class.java)
                                            if (!list.contains( Pair(LibraryAdapter.DataItem.ArtistItem(artist[0], item.pin), item.pin))) {
                                                list += listOf(
                                                    Pair(
                                                        LibraryAdapter.DataItem.ArtistItem(
                                                            artist[0],
                                                            item.pin
                                                        ), item.pin
                                                    )
                                                )
                                                list.sortBy { it.first.name }
                                                list.sortByDescending { it.second }
                                                dataItems.value = list.map { it.first }
                                            }
                                        }
                                    }
                            }

                            Type.PLAYLIST.name -> {
                                db.collection("Playlist")
                                    .whereEqualTo("id", item.id)
                                    .get()
                                    .addOnCompleteListener { doc ->
                                        if (doc.isSuccessful) {
                                            val playlist =
                                                doc.result.toObjects(Playlist::class.java)
                                            if (playlist.isNotEmpty() && !list.contains(Pair(LibraryAdapter.DataItem.PlaylistItem(playlist[0], item.pin), item.pin))
                                            ) {
                                                list += listOf(
                                                    Pair(
                                                        LibraryAdapter.DataItem.PlaylistItem(
                                                            playlist[0],
                                                            item.pin
                                                        ), item.pin
                                                    )
                                                )
                                                list.sortBy { it.first.name }
                                                list.sortByDescending { it.second }
                                                dataItems.value = list.map { it.first }
                                            }
                                        }
                                    }
                            }

                        }
                    }
                }
            }
        return dataItems
    }

    data class FollowedObject(
        val id: String = "",
        val type: String = "",
        val pin: Boolean = false,
        val date: String = Calendar.getInstance().time.time.toString(),
    )

    fun pinObject(userID: String,id: String) {
        db.collection("User")
            .document(userID)
            .collection("Followed")
            .document(id)
            .update("pin",true)
//        val field = convertTypeToField(type)
//        val savedObject = hashMapOf("id" to id, "pin" to true)
//        val removedObject = hashMapOf("id" to id, "pin" to false)
//        if (field != "error") {
//
//            db.collection("Followed")
//                .document(userID)
//                .update(field, FieldValue.arrayRemove(removedObject))
//
//
//            db.collection("Followed")
//                .document(userID)
//                .update(field, FieldValue.arrayUnion(savedObject))
//                .addOnFailureListener {
//
//                    db.collection("Followed")
//                        .document(userID)
//                        .set(hashMapOf(field to listOf(savedObject)))
//
//                }
//        }
    }

    fun unpinObject(userID: String,id: String) {
        db.collection("User")
            .document(userID)
            .collection("Followed")
            .document(id)
            .update("pin",false)
//        val field = convertTypeToField(type)
//        val savedObject = hashMapOf("id" to id, "pin" to false)
//        val removedObject = hashMapOf("id" to id, "pin" to true)
//
//        if (field != "error") {
//
//            db.collection("Followed")
//                .document(userID)
//                .update(field, FieldValue.arrayRemove(removedObject))
//
//            db.collection("Followed")
//                .document(userID)
//                .update(field, FieldValue.arrayUnion(savedObject))
//                .addOnFailureListener {
//
//                    db.collection("Followed")
//                        .document(userID)
//                        .set(hashMapOf(field to listOf(savedObject)))
//
//                }
//        }
    }

    fun followObject(userID: String,id: String, type: Type){

        val savedObject = hashMapOf("id" to id, "type" to type.name,"pin" to false )

        db.collection("User")
            .document(userID)
            .collection("Followed")
            .document(id)
            .set(savedObject)

//
//        val field = convertTypeToField(type)
//        val savedObject =
//            if (type==Type.TRACK)
//                id
//            else
//                hashMapOf("id" to id, "pin" to false)
//
//        if (field!= "error") {
//            db.collection("Followed")
//                .document(userID)
//                .update(field,FieldValue.arrayUnion(savedObject))
//                .addOnFailureListener {
//
//                    db.collection("Followed")
//                        .document(userID)
//                        .set(hashMapOf(field to listOf(savedObject)))
//
//                }
//                .addSnapshotListener { value, _ ->

//                    val followedIDs = mutableListOf(id)
//                    if (value != null && value.exists()) {
//
//                        if (value[field] != null) {
//
//                            val availableList = (value[field] as List<String>)
//
//                            if (!availableList.contains(id))
//                                followedIDs += availableList
//                            else {
//                                followedIDs.clear()
//                                followedIDs +=availableList
//                            }
//
//                        }
//                    }

//                    db.collection("Followed")
//                        .document(userID)
//                        .set(hashMapOf(field to followedIDs), SetOptions.merge())

//                }
//        }
    }

    fun unfollowObject(userID: String,id: String) {

        db.collection("User")
            .document(userID)
            .collection("Followed")
            .document(id)
            .delete()

//        val field = convertTypeToField(type)
//
//        if (field != "error") {
//
//            if (type==Type.TRACK){
//                db.collection("Followed")
//                    .document(userID)
//                    .update(field,FieldValue.arrayRemove(id))
//            }
//            else
//                db.collection("Followed")
//                    .document(userID)
//                    .get()
//                    .addOnSuccessListener { docs ->
//                        if (docs != null && docs.exists() && docs[field] != null) {
//                            val fieldObject = docs[field] as List<HashMap<*, *>>
//                            val unfollowedObject = fieldObject.find { it["id"].toString() == id }
//                            db.collection("Followed")
//                                .document(userID)
//                                .update(field, FieldValue.arrayRemove(unfollowedObject))
//                        }
//
//
//                    }
//        }
    }
//                .get()
//                .addOnSuccessListener{ value ->
//
//                    if (value != null) {
//
//                        if (value[field] != null) {
//
//                            val availableList = (value[field] as MutableList<String>)
//
//                            if (availableList.contains(id)) {
//                                availableList.remove(id)
//
//                                db.collection("Followed")
//                                    .document(userID)
//                                    .set(
//                                        hashMapOf(field to availableList),
//                                        SetOptions.merge()
//                                    )
//                            }
//                        }
//                    }

//                }



    fun checkObjectIsFollowed(userID: String,playlistID: String, type: Type): MutableLiveData<Boolean>{

            val field = convertTypeToField(type)
            var isFollowed = MutableLiveData<Boolean>()

           db.collection("User")
               .document(userID)
               .collection("Followed")
               .whereEqualTo("id",playlistID)
                .addSnapshotListener { docs, _ ->
                    isFollowed.value = docs!=null && !docs.isEmpty
                }
        return isFollowed
    }

    fun checkObjectIsFollowed(userID: String, playlistIDs: List<String>, type: Type): MutableLiveData<List<Boolean>>{

        val field = convertTypeToField(type)
        var isFollowedList = MutableLiveData<List<Boolean>>()
        var dataList = MutableList(playlistIDs.size){false}

        playlistIDs.forEachIndexed { index, id  ->
            db.collection("User")
                .document(userID)
                .collection("Followed")
                .document(id)
                .addSnapshotListener { doc, _ ->
                    if (doc != null) {
                        dataList[index] = doc.exists()
                        val temp = dataList
                        isFollowedList.value = temp
                    }
                }
        }
        return isFollowedList
    }

    fun getLikedPlaylists(userID: String): MutableLiveData<List<Pair<Playlist,Boolean>>>{
        val playlistWithPinsObserve = MutableLiveData<List<Pair<Playlist,Boolean>>>()
        db.collection("User")
            .document(userID)
            .collection("Followed")
            .whereEqualTo("type",Type.PLAYLIST.name)
            .addSnapshotListener { items, _ ->

                if (items!=null && !items.isEmpty){
                    val playlistWithPins = items as List<HashMap<*,*>>
                    val playlistIDGroups = subList(playlistWithPins.map { it["id"] })
                    val listData = mutableListOf<Pair<Playlist,Boolean>>()

                    playlistIDGroups.forEach { playlistIDs ->
                        db.collection("Playlist")
                            .whereIn("id", playlistIDs)
                            .addSnapshotListener { documents, _ ->
                                if (documents != null) {
                                    val playlists = documents.toObjects(Playlist::class.java)
                                    listData += playlists.map { playlist ->
                                        val isPin = (playlistWithPins.find { playlist.id==it["id"] }!!["pin"] as Boolean)
                                        Pair(playlist,isPin)
                                    }
                                    val currentData = listData
                                    playlistWithPinsObserve.value = currentData
                                }
                            }
                    }
                }
            }
        return playlistWithPinsObserve
    }

    fun getFollowedAlbums(userID: String): MutableLiveData<List<Album>>{
        val albums = MutableLiveData<List<Album>>()
        db.collection("Followed")
            .document(userID)
            .addSnapshotListener { items, _ ->

                if (items!=null){
                    if (items["albums"] !=null) {
                        val albumIDGroups = subList((items.get("albums") as List<HashMap<*,*>>).map { it["id"] })
                        val listData = mutableListOf<Album>()

                        albumIDGroups.forEach { albumIDs ->
                            db.collection("Album")
                                .whereIn("id", albumIDs)
                                .addSnapshotListener { documents, _ ->
                                    if (documents != null) {
                                        listData += documents.toObjects(Album::class.java)!!
                                        val currentData = listData
                                        albums.value = currentData
                                    }
                                }
                        }
                    }
                }
            }
        return albums
    }

    fun getFollowedArtists(userID: String): MutableLiveData<List<Artist>>{
        val artists = MutableLiveData<List<Artist>>()
        db.collection("Followed")
            .document(userID)
            .addSnapshotListener { items, _ ->
                if (items!=null) {
                    if (items["artists"] != null) {
                        val artistIDGroups = subList((items.get("artists") as List<HashMap<*,*>>).map { it["id"] })
                        val listData = mutableListOf<Artist>()
                        artistIDGroups.forEach { artistIDs ->

                            db.collection("Artist")
                                .whereIn("id", artistIDs)
                                .addSnapshotListener { documents, _ ->
                                    if (documents != null) {
                                        listData += documents.toObjects(Artist::class.java)
                                        val currentData = listData
                                        artists.value = currentData
                                    }
                                }
                        }
                    }
                }
            }
        return artists
    }

    fun getLikedTracks(userID: String): MutableLiveData<List<Track>>{
        val tracks = MutableLiveData<List<Track>>()
        db.collection("User")
            .document(userID)
            .collection("Followed")
            .whereEqualTo("type",Type.TRACK.name)
            .addSnapshotListener { items, _ ->
                if (items!=null && !items.isEmpty){
                    val trackIDGroups = subList((items.toObjects(FollowedObject::class.java).map { it.id }))
                    val listData = mutableListOf<Track>()
                    Log.i("trackABCs","$trackIDGroups")

                    trackIDGroups.forEach { trackIDs ->
                        db.collection("Track")
                            .whereIn("id", trackIDs)
                            .addSnapshotListener { documents, _ ->
                                if (documents != null && !documents.isEmpty) {
                                    listData += documents.toObjects(Track::class.java)
                                    val currentData = listData
                                    tracks.value = currentData
                                }
                            }
                    }
                }
            }
        return tracks
    }

    /**Category**/
    fun getCategories(): MutableLiveData<List<Category>>{
        val categories = MutableLiveData<List<Category>>()
        db.collection("Category")
            .get()
            .addOnSuccessListener { results ->
                if (results!=null){

                    categories.value = results.toObjects(Category::class.java)
                }
            }
        return categories
    }

    fun getPlaylistsOfCategory(categoryID: String): MutableLiveData<List<Playlist>>{
        val playlists = MutableLiveData<List<Playlist>>()

        db.collection("Category")
            .document(categoryID)
            .get().addOnSuccessListener { results ->
                if (results!=null){
                    if (results["playlists"]!=null) {
                        val playlistIDs = results["playlists"] as List<*>
                        val playlistIDGroups = subList(playlistIDs)

                        val listData = mutableListOf<Playlist>()
                        playlistIDGroups.forEachIndexed { index, ids ->
                            db.collection("Playlist")
                                .whereIn("id",ids )
                                .get()
                                .addOnSuccessListener { documents ->
                                    if (documents != null) {
                                        for (doc in documents) {
                                            listData.add(convertDocToPlaylist(doc))
                                        }
                                    }
                                    val currentData = listData

//                                    currentData.forEach {
//                                        val map = hashMapOf(
//                                            "id" to it.id,
//                                            "type" to it.type,
//                                            "search_keywords" to generateSearchKeywords(it.name!!)
//                                        )
//                                        db.collection("Search")
//                                            .document(it.id!!)
//                                            .set(map)
//                                    }

//                                    if (index==playlistIDsSub.size-1) {
                                        playlists.value = currentData
//                                    }
                                }
                        }
                    }
                }
            }
        return playlists
    }


//
//    fun uploadCategories(categories: List<Category>){
//        categories.forEach { category ->
//            db.collection("Category")
//                .document(category.id!!)
//                .set(category, SetOptions.merge())
//        }
//    }
//
//    fun uploadToCategoryItems(categoryID: String,playlistIDs: List<String>){
//        db.collection("Category")
//            .document(categoryID)
//            .set(hashMapOf("playlists" to playlistIDs), SetOptions.merge())
//    }

    /** Genre **/
//    fun insertGenre(genres : List<String>){
//        db.collection("Genre")
//            .document()
//            .set(hashMapOf("genres" to genres), SetOptions.merge())
//    }

    /** Home **/

    fun getDataHomeScreen(user: User): MutableLiveData<List<GroupPlaylist>> {
        val groupPlaylists = MutableLiveData<List<GroupPlaylist>>()
        db.collection("System")
            .document("SystemTopic")
            .get()
            .addOnSuccessListener{ results ->

                val topic = results.data
                val availableList = mutableListOf<GroupPlaylist>()

                if (results != null) {
                    topic!!.keys.forEach {
                        db.collection("Playlist")
                            .whereIn("id", topic[it] as List<String>)
                            .get()
                            .addOnSuccessListener{ documents ->
                                val listData = mutableListOf<Playlist>()
                                if (documents != null && !documents.isEmpty) {
                                    for (doc in documents) {
                                        listData.add(convertDocToPlaylist(doc))
                                    }
                                    availableList.add(GroupPlaylist(it, listData))
                                    groupPlaylists.value = availableList

                                }
                            }

                        db.collection("Album")
                            .whereIn("id", topic[it] as List<String>)
                            .get()
                            .addOnSuccessListener{ documents ->
                                if (documents != null && !documents.isEmpty) {
                                    val albums = documents.toObjects(Album::class.java)
                                    availableList.add(
                                        GroupPlaylist(
                                            it,
                                            convertAlbumsToPlaylists(albums)
                                        )
                                    )
                                    groupPlaylists.value = availableList
                                }
                            }

                    }
                }
                getRecommendedPlaylists(user,groupPlaylists,availableList)
                getRecentPlaylists(user,groupPlaylists,availableList)
                getYourArtists(user,groupPlaylists,availableList)
                getRelateArtists(user,groupPlaylists,availableList)
            }

        return groupPlaylists
    }

    private fun getRecommendedPlaylists(user: User,
                                        _groupPlaylists: MutableLiveData<List<GroupPlaylist>>,
                                        availableList: MutableList<GroupPlaylist>){

        db.collection("User")
            .document(user.id)
            .collection("Recommendation")
            .get()
            .addOnSuccessListener { docs ->
                if (docs != null && !docs.isEmpty) {
                    val playlists = docs.toObjects(Playlist::class.java)
                    availableList.add(
                        GroupPlaylist(
                            "Mix for you",
                            playlists
                        )
                    )
                    _groupPlaylists.value = availableList

                }
            }
    }

    private fun getRecentPlaylists(user: User,
                                   _groupPlaylists: MutableLiveData<List<GroupPlaylist>>,
                                   availableList: MutableList<GroupPlaylist>){
        db.collection("User")
            .document(user.id)
            .collection("History")
            .whereEqualTo("type",Type.PLAYLIST)
            .get()
            .addOnSuccessListener{ value ->
                if (value != null && !value.isEmpty) {
                    val result = value.toObjects(FirebaseRepository.History::class.java)

                    val playlistIDs = mutableListOf<String>()
                    result.forEach { item ->
                        if (playlistIDs.size <= 10 && !playlistIDs.contains(item.id)) {
                            playlistIDs.add(item.id)
                        }
                    }

                    db.collection("Playlist")
                        .whereIn("id", playlistIDs)
                        .get()
                        .addOnSuccessListener{ result ->
                            if (result != null && !result.isEmpty) {
                                val playlists = result.toObjects(Playlist::class.java)
                                val group = GroupPlaylist("Recent Playlists", playlists)

                                val recentlyPlayedPlaylists =
                                    availableList.find { it.title == "Recent playlist" }
//
                                if (recentlyPlayedPlaylists != null) {
                                    availableList[availableList.indexOf(recentlyPlayedPlaylists)]
                                        .playlists = playlists
                                    _groupPlaylists.value = availableList
                                }
                                else {
                                    availableList.add(group)
                                    _groupPlaylists.value = availableList

                                }

                            }
                        }
                }
            }

    }

    private fun getYourArtists(user: User,
                               _groupPlaylists: MutableLiveData<List<GroupPlaylist>>,
                               availableList: MutableList<GroupPlaylist>){
        db.collection("User")
            .document(user.id)
            .collection("Followed")
            .get()
            .addOnSuccessListener{ items ->
                if (items != null) {
                    val artistIDGroups = subList(
                        items.toObjects(FollowedObject::class.java)
                            .filter { it.type == Type.ARTIST.name }.map { it.id })
                    val listData = mutableListOf<Artist>()
                    artistIDGroups.forEach { artistIDs ->

                        db.collection("Artist")
                            .whereIn("id", artistIDs)
                            .get()
                            .addOnSuccessListener{ documents ->
                                if (documents != null) {
                                    listData += documents.toObjects(Artist::class.java)
                                    val artists = convertArtistsToPlaylists(listData)

                                    val group = GroupPlaylist("Your favorite artists", artists)

                                    val recentlyPlayedPlaylists =
                                        availableList.find { it.title == "Your favorite artists" }

                                    if (recentlyPlayedPlaylists != null) {
                                        availableList[availableList.indexOf(recentlyPlayedPlaylists)]
                                            .playlists = artists
                                        _groupPlaylists.value = availableList
                                    } else {
                                        availableList.add(group)
                                        _groupPlaylists.value = availableList

                                    }

                                }
                            }
                    }
                }
            }
    }

    private fun getRelateArtists(user: User,
                                 _groupPlaylists: MutableLiveData<List<GroupPlaylist>>,
                                 availableList: MutableList<GroupPlaylist>){

        runBlocking(Dispatchers.IO) {
            db.collection("User")
                .document(user.id)
                .collection("Followed")
                .get()
                .addOnSuccessListener { docs ->
                    if (docs != null && !docs.isEmpty()) {

                        //get id's artists was followed
                        val artistIds = subList(docs.toObjects(FollowedObject::class.java).filter { it.type=="ARTIST"}.map { it.id })

                        artistIds.forEach { ids ->
                            db.collection("Artist")
                                .whereIn("id", ids)
                                .get()
                                .addOnSuccessListener { results ->
                                    if (results != null && !results.isEmpty) {
                                        val artists = results.toObjects(Artist::class.java)

                                        //create userProfiles array for calculate cosines similarity
                                        val userProfiles = createUserProfile(artists)
                                        val genres = createGenres(artists)
                                        val itemProfiles = createItemProfile(artists,genres)

                                        val sortedMap = calculateArtistsCosineSimilarity(userProfiles,itemProfiles,genres)
                                        getArtistsBaseOnRelateGenres(_groupPlaylists,availableList,sortedMap,artists)
                                    }
                                }
                        }
                    }
                }
        }
    }

    private fun getArtistsBaseOnRelateGenres(
        _groupPlaylists: MutableLiveData<List<GroupPlaylist>>,
        availableList: MutableList<GroupPlaylist>
        ,sortedMap: Map<String,Double>, artists: List<Artist>){
        db.collection("Artist")
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

                    availableList.add(GroupPlaylist("Recommended artists",
                        convertArtistsToPlaylists(relateArtists.subList(0,8))))
                    _groupPlaylists.value = availableList
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

    private fun calculateArtistsCosineSimilarity(
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


    /** Recommendation **/


    fun getRecommendedTracks(userId: String): MutableLiveData<List<Track>> {
        val recommendedTracks = MutableLiveData<List<Track>>()

        getInformationYourLikedTracks(userId).addOnSuccessListener { results ->
            if (!results.isEmpty){
                val followedTracks = results.toObjects(FollowedObject::class.java)
                val trackIds = followedTracks.map { it.id }
                val tracksLiveData = getYourLikedTracks(trackIds)

                var observer =  Observer<List<Track>>{}
                observer = Observer<List<Track>> { tracks ->
                    if (tracks.size==followedTracks.size){
                        val artists = createArtistArray(tracks)
                        val itemProfiles = createTrackProfiles(tracks,artists)

                        val currentDate = Calendar.getInstance().time.time
                        val userProfiles = tracks.map {track ->
                            val dateLikeSong = followedTracks.find { it.id==track.id }!!.date.toLong()
                            calculateDayBetweenToDate(dateLikeSong,currentDate).toDouble()
                        }.toDoubleArray()

                        val similarityArray = calculateCosineSimilarity(userProfiles,itemProfiles,artists)

                        if (similarityArray.keys.size<10)
                            getTracksOfArtists(similarityArray.map { it.key }).addOnSuccessListener {
                                recommendedTracks.value = it.toObjects(Track::class.java)
                            }
                        else
                            getTracksOfArtists(similarityArray.map { it.key }
                                .subList(0,similarityArray.size*3/10)).addOnSuccessListener {
                                recommendedTracks.value = it.toObjects(Track::class.java)
                            }

                        tracksLiveData.removeObserver(observer)
                    }
                }


                tracksLiveData.observeForever(observer)
            }
        }
        return recommendedTracks
    }

    private fun getInformationYourLikedTracks(userId: String): Task<QuerySnapshot> {
        return db.collection("User")
            .document(userId)
            .collection("Followed")
            .whereEqualTo("type",Type.TRACK.name)
            .get()
    }

    private fun getYourLikedTracks(trackIds: List<String>): MutableLiveData<List<Track>> {

        val allTrack = MutableLiveData<List<Track>>()
        val additionalTrack = mutableListOf<Track>()
        val listTrackIds = subList(trackIds)
        listTrackIds.forEach { ids ->
            db.collection("Track")
                .whereIn("id",ids)
                .get()
                .addOnSuccessListener { results ->
                    if (!results.isEmpty){
                        val tracks = results.toObjects(Track::class.java)
                        additionalTrack.addAll(tracks)
                        allTrack.value = additionalTrack
                    }
                }
        }

        return allTrack
    }

    private fun getTracksOfArtists(topArtists: List<Artist>): Task<QuerySnapshot> {
        return db.collection("Track")
            .whereArrayContainsAny("album.artists",topArtists)
            .get()
    }

    //get relate artist
    fun getRelateArtists(id: String?): MutableLiveData<List<Artist>>{

        val relateArtists = MutableLiveData<List<Artist>>()
        if (id!=null && id!="")
            db.collection("Artist")
                .document(id)
                .get()
                .addOnSuccessListener { artistDoc ->
                    if(artistDoc!=null){
                        if(artistDoc.get("genres")!=null) {
                            val genres = artistDoc.get("genres") as List<*>
                            db.collection("Artist")
                                .whereEqualTo("genres", genres)
                                .get()
                                .addOnSuccessListener { relateArtistsDoc ->
                                    if (relateArtistsDoc != null && !relateArtistsDoc.isEmpty) {
                                        val relateArtistData =
                                            relateArtistsDoc.toObjects(Artist::class.java).toMutableList()
                                        relateArtistData.remove(relateArtistData.find { it.id==id }!!)
                                        relateArtists.value = relateArtistData
                                    }
                                }
                        }
                    }
                }

        return relateArtists
    }


    fun saveHistory(userID: String,trackID: String,score: Double, type: Type){
        val currentTime = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val newHistory = hashMapOf("id" to trackID, "date" to currentTime, "score" to score, "type" to type )
        db.collection("User")
            .document(userID)
            .collection("History")
            .document()
            .set(newHistory)
    }

//     fun getRecommendedTracks(userID: String): MutableLiveData<List<Track>>{
//         val trackObserves = MutableLiveData<List<Track>>()
//         val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
//         db.collection("Recommendation")
//             .document(userID)
//             .collection("Day6")
//             .get()
//             .addOnSuccessListener { docs ->
//                 if (docs != null && !docs.isEmpty) {
//                     val tracks = docs.toObjects(Track::class.java)
//                     trackObserves.value = tracks
//                 }
//             }
//         return trackObserves
//    }

    data class History(
        val id: String = "",
        val date: Int = 0,
        val score: Double = 0.0,
        val type: Type = Type.TRACK
    )

    fun putRecommendedTracks(userId: String){
        db.collection("User")
            .document(userId)
            .collection("History")
            .whereEqualTo("type",Type.TRACK.name)
            .get()
            .addOnSuccessListener { docs ->
                if (docs != null && !docs.isEmpty) {

                    val histories = docs.toObjects(History::class.java)
                    val days = sublistByDay(histories)

                    val day = days[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1]

                    if (day.isNotEmpty() && day.size>10) {
                        val recentlyPlayedIds = day.map { it.id }
                        val recentlyPlayedTrackIdsSublist = subList(recentlyPlayedIds)

                        val userProfiles = day.map { it.score }.toDoubleArray()

                        val tracksObserver = MutableLiveData<List<Track>>()
                        val trackData = mutableListOf<Track>()

                        recentlyPlayedTrackIdsSublist.forEachIndexed { index, trackIds ->
                            db.collection("Track")
                                .whereIn("id", trackIds)
                                .get()
                                .addOnSuccessListener { results ->
                                    if (results != null && !results.isEmpty) {
                                        val recentlyPlayedTracks =
                                            results.toObjects(Track::class.java)

                                        recentlyPlayedTracks.forEach {
                                            if (!trackData.contains(it))
                                                trackData.add(it)
                                        }

                                        trackData.sortBy { it.id }
                                        tracksObserver.value = trackData

                                    }
                                }
                        }

                        var observer = Observer<List<Track>> {  }
                        observer = Observer { tracks ->
                            if (tracks != null && tracks.size == recentlyPlayedIds.toSet()
                                    .toList().size
                            ) {
                                val artists = createArtistArray(tracks)
                                val recentlyPlayedTracks = recentlyPlayedIds.map { id ->
                                    tracks.find { it.id == id } ?: Track(id = id)
                                }

                                val itemProfiles =
                                    createTrackProfiles(recentlyPlayedTracks, artists)

                                val cosineSimilarityArray =
                                    calculateCosineSimilarity(userProfiles, itemProfiles, artists)

                                postRecommendedTracks(
                                    userId,
                                    cosineSimilarityArray.keys.toList().subList(0, 3)
                                )

                                tracksObserver.removeObserver(observer)
                            }
                        }

                        tracksObserver.observeForever(observer)
                    }
                }
            }

    }


    private fun postRecommendedTracks(userId: String, topArtists: List<Artist>){
        db.collection("Track")
            .whereArrayContainsAny("album.artists",topArtists)
            .get()
            .addOnSuccessListener { docs ->
                Log.i("HomeViewModel", "day ${topArtists[1]}")

                if (docs != null && !docs.isEmpty) {
                    val recommendedTracks = docs.toObjects(Track::class.java)

                    val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                    Log.i("HomeViewModel", "recommend ${docs.documents}")

                    val trackInPlaylists = recommendedTracks.map {
                        TrackInPlaylist(
                            it.id,
                            Calendar.getInstance().get(Calendar.DATE).toString()
                        )
                    }
                    val playlist = Playlist(
                        id = "temp",
                        tracks = trackInPlaylists,
                        owner = Owner("ToneZone"),
                        name= "Daily Mix ${day}",
                        description = getArtistNames(recommendedTracks[0].artists?: listOf()),
                        images = recommendedTracks[0].album?.images
                    )
                    Log.i("recommendation","$playlist")


                    db.collection("Playlist")
                        .document()
                        .set(playlist)
                        .addOnSuccessListener {

                            db.collection("Playlist")
                                .whereEqualTo("id", playlist.id)
                                .addSnapshotListener { documents, _ ->
                                    if (documents != null) {
                                        for (doc in documents) {


                                            playlist.id = doc.id
                                            db.collection("Playlist")
                                                .document(doc.id)
                                                .set(playlist)
                                                .addOnSuccessListener {
                                                    db.collection("User")
                                                        .document(userId)
                                                        .collection("Recommendation")
                                                        .document("day${day}")
                                                        .set(playlist)
                                                }
                                        }
                                    }
                                }
                        }
                    }


                }
            }




    private fun calculateCosineSimilarity(
        userProfiles: DoubleArray,
        itemProfiles: Array<Array<Double>>,
        artists: MutableList<Artist>
    ): Map<Artist, Double> {
        //calculate cosine similarity of each genre
        val cosineSimilarityArray = itemProfiles.map {
            cosineSimilarity(
                it.toDoubleArray(),
                userProfiles
            )
        }

        val unsortMap = mutableMapOf<Artist, Double>()
        cosineSimilarityArray.forEachIndexed { index, value ->
            unsortMap[artists[index]] = value
        }

        return sortArtistsByValue(unsortMap.toMap())
    }

    private fun createTrackProfiles(tracks: List<Track>, artists: MutableList<Artist>): Array<Array<Double>>{

        //create itemProfiles array for calculate cosines similarity
        val itemProfiles = Array(artists.size) { Array(tracks.size) { 0.0 } }
        artists.forEachIndexed { artistIndex, artist ->
            tracks.forEachIndexed { trackIndex, track ->
                if (track.album?.artists != null && track.album.artists.contains(artist))
                    itemProfiles[artistIndex][trackIndex] = 1.0
            }
        }
        return itemProfiles
    }


    private fun createArtistArray(tracks: List<Track>): MutableList<Artist>{
        val artists = mutableListOf<Artist>()
        tracks.forEach { track ->
            if (track.album?.artists != null) {
                track.album.artists.forEach { artist ->
                    if (!artists.contains(artist))
                        artists.add(artist)
                }
            }
        }


        return artists
    }

    /** search **/
    fun searchInFirebase(searchText: String): MutableLiveData<List<LibraryAdapter.DataItem>>{
        val searchList = MutableLiveData<List<LibraryAdapter.DataItem>>()
        db.collection("Search")
            .whereArrayContains("search_keywords",searchText)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    val result = it.result.toObjects(SearchModel::class.java)

                    var list = listOf<LibraryAdapter.DataItem>()
                    result.forEach { item ->
                        when(item.type){

                            "track" -> {
                                db.collection("Track")
                                    .whereEqualTo("id",item.id)
                                    .get()
                                    .addOnCompleteListener { doc ->
                                        if (doc.isSuccessful){
                                            val track = doc.result.toObjects(Track::class.java)
                                            list += listOf(LibraryAdapter.DataItem.TrackItem(track[0]))
                                            searchList.value = list
                                        }
                                    }
                            }

                            "album" -> {
                                db.collection("Album")
                                    .whereEqualTo("id",item.id)
                                    .get()
                                    .addOnCompleteListener { doc ->
                                        if (doc.isSuccessful){
                                            val album = doc.result.toObjects(Album::class.java)
                                            list += listOf(LibraryAdapter.DataItem.AlbumItem(album[0]))
                                            searchList.value = list
                                        }
                                    }
                            }

                            "artist" -> {
                                db.collection("Artist")
                                    .whereEqualTo("id",item.id)
                                    .get()
                                    .addOnCompleteListener { doc ->
                                        if (doc.isSuccessful){
                                            val artist = doc.result.toObjects(Artist::class.java)
                                            list += listOf(LibraryAdapter.DataItem.ArtistItem(artist[0]))
                                            searchList.value = list
                                        }
                                    }
                            }

                            "playlist" -> {
                                db.collection("Playlist")
                                    .whereEqualTo("id",item.id)
                                    .get()
                                    .addOnCompleteListener { doc ->
                                        if (doc.isSuccessful){
                                            val playlist = doc.result.toObjects(Playlist::class.java)
                                            list += listOf(LibraryAdapter.DataItem.PlaylistItem(playlist[0]))
                                            searchList.value = list
                                        }
                                    }
                            }
                        }
                    }


                }else{
                    Log.d(TAG,"Failure: ${it.exception!!.message}")
                }
            }
        return searchList
    }

    fun searchInFirebase(searchText: String, type: Type): MutableLiveData<List<LibraryAdapter.DataItem>>{
        val searchList = MutableLiveData<List<LibraryAdapter.DataItem>>()
        db.collection("Search")
            .whereArrayContains("search_keywords",searchText)
            .whereEqualTo("type",type.name.lowercase())
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    val result = it.result.toObjects(SearchModel::class.java)

                    var list = listOf<LibraryAdapter.DataItem>()
                    result.forEach { item ->
                        db.collection("Track")
                            .whereEqualTo("id",item.id)
                            .get()
                            .addOnCompleteListener { doc ->
                                if (doc.isSuccessful){
                                    val track = doc.result.toObjects(Track::class.java)
                                    list += listOf(LibraryAdapter.DataItem.TrackItem(track[0]))
                                    searchList.value = list
                                }
                        }
                    }
                }else{
                    Log.d(TAG,"Failure: ${it.exception!!.message}")
                }
            }
        return searchList
    }

    data class SearchModel(
        val id: String= "" ,
        val type: String =""
    )

}
