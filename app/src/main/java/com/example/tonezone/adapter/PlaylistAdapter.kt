package com.example.tonezone.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tonezone.databinding.ItemLargeArtistBinding
import com.example.tonezone.databinding.ItemPlaylistInGridBinding
import com.example.tonezone.network.Playlist

class PlaylistAdapter(private val clickListener: OnClickListener) : ListAdapter<Playlist, RecyclerView.ViewHolder>(DiffCallBack){

    companion object DiffCallBack: DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem.id==newItem.id
        }

        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem == newItem
        }

    }

    class ArtistViewHolder private constructor
        (private val binding: ItemLargeArtistBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist, clickListener: OnClickListener){
            binding.playlist = playlist
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object{
            fun from(parent: ViewGroup) : RecyclerView.ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemLargeArtistBinding.inflate(layoutInflater,parent,false)
                return ArtistViewHolder(binding)
            }
        }
    }

    class PlaylistViewHolder private constructor
        (private val binding: ItemPlaylistInGridBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist, clickListener: OnClickListener){
            binding.playlist = playlist
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object{
            fun from(parent: ViewGroup) : RecyclerView.ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemPlaylistInGridBinding.inflate(layoutInflater,parent,false)
                return PlaylistViewHolder(binding)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position).type){
            "artist" -> 1
            else -> 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            1 -> ArtistViewHolder.from(parent)
            else -> PlaylistViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val newItem = getItem(position)
        when(getItemViewType(position)) {
            1 -> {
                (holder as ArtistViewHolder).bind(newItem, clickListener)
            }
            else -> {
                (holder as PlaylistViewHolder).bind(newItem, clickListener)
            }

        }
    }

    class OnClickListener(val clickListener : (playlist: Playlist) -> Unit) {

        fun onClick(playlist: Playlist) = clickListener(playlist)
    }
}
