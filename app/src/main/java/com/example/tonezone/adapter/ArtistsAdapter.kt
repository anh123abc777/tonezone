package com.example.tonezone.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tonezone.databinding.ItemArtistHorizontalBinding
import com.example.tonezone.network.Artist

class ArtistsAdapter (private val clickListener: OnClickListener) : ListAdapter<Artist, ArtistsAdapter.ViewHolder>(DiffCallBack){

    companion object DiffCallBack: DiffUtil.ItemCallback<Artist>() {
        override fun areItemsTheSame(oldItem: Artist, newItem: Artist): Boolean {
            return oldItem.id==newItem.id
        }

        override fun areContentsTheSame(oldItem: Artist, newItem: Artist): Boolean {
            return oldItem == newItem
        }

    }

    class ViewHolder private constructor
        (private val binding: ItemArtistHorizontalBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(Artist: Artist, clickListener: OnClickListener){
            binding.artist = Artist
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object{
            fun from(parent: ViewGroup) : ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemArtistHorizontalBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val newItem = getItem(position)
        holder.bind(newItem,clickListener)
    }

    class OnClickListener(val clickListener : (artist: Artist) -> Unit) {

        fun onClick(artist: Artist) = clickListener(artist)
    }
}
