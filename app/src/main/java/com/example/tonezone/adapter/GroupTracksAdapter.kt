package com.example.tonezone.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tonezone.R
import com.example.tonezone.databinding.GroupTracksBinding
import com.example.tonezone.network.FirebaseRepository
import com.example.tonezone.network.Playlist
import com.example.tonezone.player.PlayerScreenViewModel
import com.example.tonezone.utils.Type

class GroupTracksAdapter (private val clickListener: LibraryAdapter.OnClickListener,
                          private val playlistID: String,
                          private val playerViewModel: PlayerScreenViewModel
                          ) : ListAdapter<Playlist, GroupTracksAdapter.ViewHolder>(DiffCallBack){

    companion object DiffCallBack: DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem.id==newItem.id
        }

        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem == newItem
        }

    }

    class ViewHolder private constructor
        (private val binding: GroupTracksBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(
            playlist: Playlist,
            clickListener: LibraryAdapter.OnClickListener,
            playlistID: String,
            playerViewModel: PlayerScreenViewModel
        ){
            binding.playlist = playlist
            val adapter = LibraryAdapter(LibraryAdapter.OnClickListener { item, buttonId ->

                val track = (item as LibraryAdapter.DataItem.TrackItem).track
                if (playlistID!=null ){
                    if (buttonId == R.id.more_option_with_track){
                        FirebaseRepository().addItemToYourPlaylist(playlistID!!, listOf(track.id))
                        playlist.deltailTracks?.let {
                            (binding.tracks.adapter as LibraryAdapter).notifyItemRemoved(
                                it.indexOf(track))
                        }
                    }else{
                        playerViewModel.onInit(0, listOf(track) )
                    }

                }else{
                    playerViewModel.onInit(0, listOf(track) )
                }
            },true)
            adapter.submitYourLibrary(null,null,playlist.deltailTracks,null)
            binding.tracks.adapter = adapter
            binding.executePendingBindings()
        }

        companion object{
            fun from(parent: ViewGroup) : ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = GroupTracksBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val newItem = getItem(position)
        holder.bind(newItem,clickListener,playlistID,playerViewModel)
    }

}
