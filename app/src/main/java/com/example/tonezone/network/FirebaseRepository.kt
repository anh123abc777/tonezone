package com.example.tonezone.network

import TrackInPlaylist
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.tonezone.utils.convertDocToPlaylist
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class FirebaseRepository {

    private val db = Firebase.firestore

    /** track **/
    fun getAllTracks(){
        val tracks = mutableListOf<Track>()
        db.collection("Track").get()
            .addOnSuccessListener { documents ->
                for (document in documents){
                    val track = document.toObject<Track>()
                    tracks.add(track)
                }
            }
            .addOnFailureListener {
                Log.i("getTracks","Failure $it")
            }
    }

    fun insertTracks(tracks: List<Track>){
        tracks.forEach { track ->
            insertTrack(track)
//            insertArtists(track.artists?: listOf())
            track.album?.let { insertAlbum(it) }
        }
    }

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
                }
            }

        return tracks
    }

    fun getTracksOfPlaylist(id: String): MutableLiveData<List<Track>>{
        var tracks = MutableLiveData<List<Track>>()
               db.collection("Playlist")
                   .document(id)
                   .get()
                   .addOnSuccessListener{ value ->

                       if (value != null && value.exists()) {
                           val itemIDs =
                               convertHashMapToTrackInPlaylist(value.get("tracks") as List<*>)
                           var subListItemIDs = subList(itemIDs)

                           val listData = mutableListOf<Track>()
                           subListItemIDs.forEach { itemIDs ->

                               db.collection("Track")
                                   .whereIn("id", itemIDs)
                                   .get()
                                   .addOnSuccessListener { value ->
                                       if (value != null) {
                                           listData+=value.toObjects(Track::class.java)
                                           val temp = listData
                                           tracks.value = temp
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

    fun insertArtists(artists: List<Artist>){
        artists.forEach { artist ->
            insertArtist(artist)
        }
    }

    fun insertArtist(artist: Artist){
        db.collection("Artist")
            .document(artist.id!!)
            .set(artist)

    }

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
            .addSnapshotListener { documents, _ ->
                if (documents!=null){
                    artists.value = documents.toObjects(Artist::class.java)
                }
            }
        return artists
    }

    /** playlist **/

    fun insertPlaylist(playlists: List<Playlist>){
        playlists.forEach { playlist ->

            db.collection("Playlist")
                .document(playlist.id!!)
                .set(playlist, SetOptions.merge())
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
            .document()
            .delete()

        unfollowObject(userID,id,"playlist")
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

                                followObject(user.id,doc.id,"playlist")
                            }
                        }
                    }
            }

        return playlist
    }

    fun getPlaylist(id: String): MutableLiveData<Playlist>{
        val playlist = MutableLiveData<Playlist>()
        db.collection("Playlist")
            .document(id)
            .addSnapshotListener { value, error ->
                if (error!=null){
                    return@addSnapshotListener
                }

                if(value!=null && value.exists()){
                    playlist.value = Playlist()
                    playlist.value!!.name = value.getString("name")
                    playlist.value!!.id = value.getString("id")
                    playlist.value!!.images = value.get("images") as List<Image>
                    playlist.value!!.owner = value.toObject<Owner>()
                    playlist.value!!.description = value.getString("description")
                }
            }
        return playlist
    }

    fun addItemToYourPlaylist(playlistID: String, newTrackIDs: List<String>){

        db.collection("Playlist")
            .document(playlistID)
            .set(hashMapOf("tracks" to newTrackIDs), SetOptions.mergeFields("tracks"))
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
                            }
                        }
                }
            }

        return playlists
    }

    fun insertSystem(ids: List<String>,systemTopic: String){
            db.collection("System")
                .document("SystemTopic")
                .set(hashMapOf(systemTopic to ids), SetOptions.mergeFields(systemTopic))
                .addOnFailureListener {
                    Log.i("insertSystem","$it")
                }
    }

    enum class SystemTopic{
        featured_playlists, new_releases, charts
    }

    /** album **/

    fun insertAlbums(albums: List<Album>){
        albums.forEach { album ->
            insertAlbum(album)
//            insertArtists(album.artists?: listOf())
        }
    }

    fun insertAlbum(album: Album){
            album.id?.let {
                db.collection("Album")
                    .document(it)
                    .set(album)
            }
    }

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
        val tracks = MutableLiveData<List<Track>>()

        db.collection("Album")
            .document(id)
            .get()
            .addOnSuccessListener { doc ->
                val album = doc.toObject(Album::class.java)
                db.collection("Track")
                    .whereEqualTo("album", album)
                    .get()
                    .addOnSuccessListener { results ->
                        if (results != null) {
                            tracks.value = results.toObjects(Track::class.java)
                        }
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
    fun followObject(userID: String,id: String, type: String){

        val field = convertTypeToField(type)

        if (field!= "error") {
            db.collection("Followed")
                .document(userID)
                .addSnapshotListener { value, _ ->

                    val followedIDs = mutableListOf(id)
                    if (value != null && value.exists()) {

                        if (value[field] != null) {

                            val availableList = (value[field] as List<String>)

                            if (!availableList.contains(id))
                                followedIDs += availableList
                            else {
                                followedIDs.clear()
                                followedIDs +=availableList
                            }

                        }
                    }

                    Log.i("Follow","$followedIDs")
                    db.collection("Followed")
                        .document(userID)
                        .set(hashMapOf(field to followedIDs), SetOptions.merge())

                }
        }
    }

    fun unfollowObject(userID: String,id: String, type: String){

        val field = convertTypeToField(type)

        if (field!= "error") {
            db.collection("Followed")
                .document(userID)
                .get()
                .addOnSuccessListener{ value ->

                    if (value != null) {

                        if (value[field] != null) {

                            val availableList = (value[field] as MutableList<String>)

                            if (availableList.contains(id)) {
                                availableList.remove(id)

                                db.collection("Followed")
                                    .document(userID)
                                    .set(
                                        hashMapOf(field to availableList),
                                        SetOptions.merge()
                                    )
                            }
                        }
                    }

                }

        }
    }

    fun checkObjectIsFollowed(userID: String,playlistID: String, type: String): MutableLiveData<Boolean>{

            val field = convertTypeToField(type)
            var isFollowed = MutableLiveData<Boolean>()

           db.collection("Followed")
                .document(userID)
                .get()
                .addOnSuccessListener {
                    if (it[field] != null) {
                        val followedObjects = it[field] as List<String>
                        isFollowed.value = followedObjects.contains(playlistID)
                    } else {
                        isFollowed.value = false
                    }
                }
        return isFollowed
    }

    fun checkObjectIsFollowed(userID: String, playlistIDs: List<String>, type: String): MutableLiveData<List<Boolean>>{

        val field = convertTypeToField(type)
        var isFollowedList = MutableLiveData<List<Boolean>>()
        var dataList = mutableListOf<Boolean>()

            db.collection("Followed")
                .document(userID)
                .get()
                .addOnSuccessListener{ doc ->
                    if (doc != null) {
                        if (doc[field] != null) {
                            val followedObjects = doc[field] as List<String>
                            playlistIDs.forEach { id ->
                                dataList.add(followedObjects.contains(id))
                            }
                        }else{
                            dataList = MutableList(playlistIDs.size){false}
                        }
                        isFollowedList.value = dataList
                        Log.i("likeTrack","$dataList")
                    }
                }
        return isFollowedList
    }

    private fun convertTypeToField(type: String): String=
        when(type){
            "artist" -> "artists"
            "playlist" -> "playlists"
            "track" -> "tracks"
            "album" -> "albums"
            else -> "error"
        }


    fun getLikedPlaylists(userID: String): MutableLiveData<List<Playlist>>{
        val playlists = MutableLiveData<List<Playlist>>()
        db.collection("Followed")
            .document(userID)
            .addSnapshotListener { items, error ->

                if (items!=null){
                    if (items["playlists"] !=null) {
                        val playlistIDGroups = subList((items.get("playlists") as List<*>))
                        val listData = mutableListOf<Playlist>()

                        playlistIDGroups.forEach { playlistIDs ->
                            db.collection("Playlist")
                                .whereIn("id", playlistIDs)
                                .addSnapshotListener { documents, _ ->
                                    if (documents != null) {
                                        listData += documents.toObjects(Playlist::class.java)
                                        val currentData = listData
                                        playlists.value = currentData
                                    }
                                }
                        }
                    }
                }
            }
        return playlists
    }

    fun getFollowedAlbums(userID: String): MutableLiveData<List<Album>>{
        val albums = MutableLiveData<List<Album>>()
        db.collection("Followed")
            .document(userID)
            .addSnapshotListener { items, _ ->

                if (items!=null){
                    if (items["albums"] !=null) {
                        val albumIDGroups = subList((items.get("albums") as List<*>))
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
                        val artistIDGroups = subList(items.get("artists") as List<*>)
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
        db.collection("Followed")
            .document(userID)
            .addSnapshotListener { items, _ ->
                if (items!=null){
                    val trackIDGroups = subList(items.get("tracks") as List<*>)
                    val listData = mutableListOf<Track>()

                    trackIDGroups.forEach { trackIDs ->
                        db.collection("Track")
                            .whereIn("id", trackIDs)
                            .addSnapshotListener { documents, _ ->
                                if (documents != null) {
                                    listData += documents.toObjects(Track::class.java)
                                    val currentData = listData
                                    tracks.value = listData
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

    fun insertCategories(categories: List<Category>){
        categories.forEach { category ->
            db.collection("Category")
                .document(category.id!!)
                .set(category, SetOptions.merge())
        }
    }

    fun insertItemsToCategory(categoryID: String,playlistIDs: List<String>){
        db.collection("Category")
            .document(categoryID)
            .set(hashMapOf("playlists" to playlistIDs), SetOptions.merge())
    }

    /** Genre **/
    fun insertGenre(genres : List<String>){
        db.collection("Genre")
            .document()
            .set(hashMapOf("genres" to genres), SetOptions.merge())
    }

    /** Recommendation **/

    fun getRelateArtist(id: String): MutableLiveData<List<Artist>>{

        val relateArtists = MutableLiveData<List<Artist>>()

        db.collection("Artist")
            .document(id)
            .get()
            .addOnSuccessListener { artistDoc ->
                if(artistDoc!=null){
                    val genres = artistDoc.get("genres") as List<*>
                    db.collection("Artist")
                        .whereArrayContainsAny("genres",genres)
                        .get()
                        .addOnSuccessListener { relateArtistsDoc ->
                            if (relateArtistsDoc!=null){
                                relateArtists.value = relateArtistsDoc.toObjects(Artist::class.java)
                            }
                        }
                }
            }
        return relateArtists
    }

}
