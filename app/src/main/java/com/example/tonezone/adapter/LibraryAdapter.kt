package com.example.tonezone.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tonezone.databinding.ItemArtistBinding
import com.example.tonezone.databinding.ItemPlaylistInListBinding
import com.example.tonezone.databinding.ItemTrackBinding
import com.example.tonezone.network.Artist
import com.example.tonezone.network.Playlist
import com.example.tonezone.network.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LibraryAdapter(private val clickListener: OnClickListener): ListAdapter<LibraryAdapter.DataItem, RecyclerView.ViewHolder>(DiffCallBack) {

    private val ARTIST = 2
    private val PLAYLIST = 1
    private val TRACK = 3

    companion object DiffCallBack: DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return newItem.id == oldItem.id
        }

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return newItem == oldItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is DataItem.ArtistItem -> ARTIST
            is DataItem.TrackItem -> TRACK
            else -> PLAYLIST
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            1 -> PlaylistViewHolder.from(parent)
            2 -> ArtistViewHolder.from(parent)
            else -> TrackViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is ArtistViewHolder -> {
                val artistItem = getItem(position) as DataItem.ArtistItem
                holder.bind(artistItem.artist,clickListener)
            }

            is PlaylistViewHolder -> {
                val playlistItem = getItem(position) as DataItem.PlaylistItem
                holder.bind(playlistItem.playlist,clickListener)
            }

            is TrackViewHolder -> {
                val trackItem = getItem(position) as DataItem.TrackItem
                holder.bind(trackItem.track,clickListener)
            }
        }
    }

    private val adapterScope = CoroutineScope(Dispatchers.Default)
    private var defaultData = listOf<DataItem>()
    fun submitYourLibrary(playlists: List<Playlist>?, artists: List<Artist>?, tracks: List<Track>?){
        adapterScope.launch {
            val items = (artists?.map { DataItem.ArtistItem(it) }
                ?: listOf()) + (playlists?.map { DataItem.PlaylistItem(it) } ?: listOf())+
                    (tracks?.map { DataItem.TrackItem(it) } ?: listOf())

            withContext(Dispatchers.Main){
                submitList(items)
                defaultData = items
            }
        }
    }



    @SuppressLint("NotifyDataSetChanged")
    fun filterQuery(query: String) {
        val items = mutableListOf<DataItem>()
        if (query.isNotEmpty()) {
            for (item in defaultData) {
                if (item.name.lowercase().contains(query.lowercase())) {
                    items.add(item)
                }
            }
        }
        if(query.isEmpty()) {
            submitList(defaultData)
        }else{
            submitList(items)
        }
        notifyDataSetChanged()
    }

    fun filterType(type: String){
        val items = mutableListOf<DataItem>()

        if (type!="all") {
            for (item in defaultData) {
                if (item.typeName == type) {
                    items.add(item)
                }
            }
        } else{
            items.addAll(defaultData)
        }

        updateDate(items)
    }

    fun sortByAlphabetical(){
        defaultData = defaultData.sortedBy { it.name }
        updateDate(defaultData)
    }

    fun sortByCreator(){
        defaultData = defaultData.sortedBy { it.typeName }
        updateDate(defaultData)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateDate(data: List<DataItem>){
        submitList(data)
        notifyDataSetChanged()
    }

    class ArtistViewHolder private constructor(
        private val binding: ItemArtistBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(artist: Artist, clickListener: OnClickListener){
            binding.artist = artist
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object{
            fun from(parent: ViewGroup): ArtistViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemArtistBinding.inflate(layoutInflater,parent,false)
                return ArtistViewHolder(binding)
            }
        }
    }

    class PlaylistViewHolder private constructor
        (private val binding: ItemPlaylistInListBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist, clickListener: OnClickListener){
            binding.playlist = playlist
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object{
            fun from(parent: ViewGroup): PlaylistViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemPlaylistInListBinding.inflate(layoutInflater,parent,false)
                return PlaylistViewHolder(binding)
            }
        }
    }

    class TrackViewHolder private constructor
        (private val binding: ItemTrackBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(track: Track, clickListener: OnClickListener){
            binding.track = track
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object{
            fun from(parent: ViewGroup): TrackViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemTrackBinding.inflate(layoutInflater,parent,false)
                return TrackViewHolder(binding)
            }
        }
    }

    sealed class DataItem{
        data class PlaylistItem(val playlist: Playlist): DataItem(){
            override val id = playlist.id
            override val type = 1
            override val name = playlist.name
            override val typeName = playlist.type
            override val uri = playlist.uri
            override val description = playlist.description
            override val image =
                if(playlist.images?.size!=0)
                playlist.images?.get(0)?.url
            else ""
        }

        data class ArtistItem( val artist: Artist): DataItem(){
            override val id = artist.id
            override val type = 2
            override val name = artist.name
            override val typeName = artist.type
            override val uri = artist.uri
            override val description = artist.type
            override val image =
                if(artist.images?.size!=0)
                    artist.images?.get(0)?.url
            else ""

        }

        data class TrackItem( val track: Track): DataItem(){
            override val id = track.id
            override val type = 3
            override val name = track.name
            override val typeName = "Track"
            override val uri = track.uri
            override val description = ""
            override val image = track.album?.uri

        }

        abstract val id: String
        abstract val type: Int
        abstract val name: String
        abstract val typeName: String
        abstract val uri: String
        abstract val description: String
        abstract val image: String?
    }

    class OnClickListener(val clickListener: (dataItem: DataItem) -> Unit){
        fun onClick(playlist: Playlist) = clickListener(DataItem.PlaylistItem(playlist))
        fun onClick(artist: Artist) = clickListener(DataItem.ArtistItem(artist))
        fun onClick(track: Track) = clickListener(DataItem.TrackItem(track))
    }

}
