package com.example.tonezone.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tonezone.databinding.ItemLibraryBinding
import com.example.tonezone.network.Playlist


class PlaylistAdapter(private val clickListener: OnClickListener): ListAdapter<Playlist, PlaylistAdapter.ViewHolder>(DiffCallBack) {

    companion object DiffCallBack: DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return newItem === oldItem
        }

        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return newItem.id == oldItem.id
        }
    }

    class ViewHolder private constructor
        (private val binding: ItemLibraryBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist, clickListener: OnClickListener){
            binding.playlist = playlist
            binding.clickListener = clickListener
//            binding.executePendingBindings()
        }

        companion object{
            fun from(parent: ViewGroup): ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemLibraryBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position),clickListener)
    }

    class OnClickListener(val clickListener: (playlist: Playlist) -> Unit){
        fun onClick(playlist: Playlist) = clickListener(playlist)
    }


}
