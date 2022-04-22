package com.example.tonezone.utils

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.databinding.ModalBottomSheetContentBinding
import com.example.tonezone.network.*
import com.example.tonezone.player.PlayerScreenViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class YourPlaylistBottomSheet (private val tracks: List<Track>) : BottomSheetDialogFragment() {

    private lateinit var binding: ModalBottomSheetContentBinding

    private val viewModel: PlayerScreenViewModel by activityViewModels()
    private val firebaseRepo = FirebaseRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = ModalBottomSheetContentBinding.inflate(inflater)

        setupBottomSheetItems()

        return binding.root

    }

    private fun setupBottomSheetItems(){

        val adapter = LibraryAdapter(LibraryAdapter.OnClickListener{ item, idButton ->
            val playlistInfo = PlaylistInfo(
                item.id.toString(),
                item.name.toString(),
                item.description.toString(),
                item.image,
                item.description.toString()
            )

            firebaseRepo.addItemToYourPlaylist(item.id!!,tracks.map { it.id })
            this.dismiss()
            viewModel.handleLongPressEventComplete()
        })

        firebaseRepo.db.collection("Playlist")
            .whereEqualTo("owner",
                Owner(display_name = "Godoflived",
                    id = "dgusbmm3xvmzuix4sowbjb868",
                    type = "user",
                    uri = ""))
            .get().addOnCompleteListener {

                val playlists = it.result.map {doc -> convertDocToPlaylist(doc) }

                adapter.submitList(playlists.map { LibraryAdapter.DataItem.PlaylistItem(it) })

            }

        binding.listOption.adapter = adapter
    }

    companion object{
        const val TAG = "YourPlaylistModalBottomSheet"
    }
}