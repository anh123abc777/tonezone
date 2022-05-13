package com.example.tonezone.utils

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.tonezone.R
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.databinding.ModalBottomSheetContentBinding
import com.example.tonezone.network.Album
import com.example.tonezone.network.Artist
import com.example.tonezone.network.PlaylistInfo
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ArtistsModalBottomSheet (private val list: List<*>) : BottomSheetDialogFragment() {

    private lateinit var binding: ModalBottomSheetContentBinding

    private val viewModel: ModalBottomSheetViewModel by activityViewModels()

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

        val adapter = LibraryAdapter(LibraryAdapter.OnClickListener{ item, _ ->
            val playlistInfo = PlaylistInfo(
                item.id.toString(),
                item.name.toString(),
                item.description.toString(),
                item.image,
                item.typeName.toString(),
            )


            Log.i("artists",playlistInfo.toString())
            val bundle = bundleOf("playlistInfo" to playlistInfo)

            when(item) {
                is LibraryAdapter.DataItem.ArtistItem -> findNavController().navigate(R.id.artistDetailsFragment, bundle)
                else -> findNavController().navigate(R.id.playlistDetailsFragment, bundle)
            }
            this.dismiss()
        })


        when(list[0]){
            is Artist -> adapter.submitList(list.map { LibraryAdapter.DataItem.ArtistItem(it as Artist) })
            is Album -> adapter.submitList(list.map {LibraryAdapter.DataItem.AlbumItem(it as Album)})
        }
        binding.listOption.adapter = adapter
    }

    companion object{
        const val TAG = "ArtistsModalBottomSheet"
    }
}