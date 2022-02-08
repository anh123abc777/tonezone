package com.example.tonezone.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tonezone.databinding.ItemPlaylistBinding
import com.google.android.exoplayer2.extractor.mp4.Track

class TrackAdapter : ListAdapter<Track, TrackAdapter.ViewHolder>(DiffCallBack){

    companion object DiffCallBack: DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
            TODO("Not yet implemented")
        }

        override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
            TODO("Not yet implemented")
        }

    }

    class ViewHolder(private val binding: ItemPlaylistBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(track: Track){
        }

        companion object{
            fun from(parent: ViewGroup) : ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemPlaylistBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }
}