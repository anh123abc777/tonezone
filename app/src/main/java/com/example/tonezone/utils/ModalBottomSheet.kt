package com.example.tonezone.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.tonezone.adapter.BottomSheetItemAdapter
import com.example.tonezone.databinding.ModalBottomSheetContentBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ModalBottomSheet(private val objectRequest: ObjectRequest, private val isFollowing: Boolean?,private val isPin: Boolean = false) : BottomSheetDialogFragment() {

    private lateinit var binding: ModalBottomSheetContentBinding

    private val viewModel: ModalBottomSheetViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = ModalBottomSheetContentBinding.inflate(inflater)

        setupBottomSheetItems(objectRequest,isFollowing,isPin)

        return binding.root

    }

    private fun setupBottomSheetItems(objectRequest: ObjectRequest,isFollowing: Boolean?, isPin: Boolean){
        when(objectRequest){

            ObjectRequest.ARTIST -> submitBottomSheetList(listOf())

            ObjectRequest.PLAYLIST -> submitBottomSheetList(
                if(isFollowing == true)
                    listOf(Signal.LIKED_PLAYLIST)
                else
                    listOf(Signal.LIKE_PLAYLIST)
            )

            ObjectRequest.TRACK -> {

                val itemLike = if(isFollowing == true) listOf(Signal.LIKED_TRACK) else listOf(Signal.LIKE_TRACK)

                submitBottomSheetList(
                    itemLike + listOf(
                                            Signal.HIDE_THIS_SONG,
                                            Signal.ADD_TO_PLAYLIST,
                                            Signal.VIEW_ARTIST,
                                            Signal.VIEW_ALBUM,)
                )
            }

            ObjectRequest.YOUR_PLAYLIST -> submitBottomSheetList(
                listOf(
                    Signal.DELETE_PLAYLIST,
                    Signal.EDIT_PLAYLIST,
                    Signal.ADD_SONGS,
                )
            )

            ObjectRequest.PLAYLIST_FROM_LIBRARY -> {
                submitBottomSheetList(
                    listOf(
                        Signal.LIKED_PLAYLIST,
                        if (isPin) Signal.UNPIN_PLAYLIST else Signal.PIN_PLAYLIST,))
            }

            ObjectRequest.OWNER_PLAYLIST_FROM_LIBRARY -> {
                submitBottomSheetList(
                    listOf(
                        if (isPin) Signal.UNPIN_PLAYLIST else Signal.PIN_PLAYLIST,
                        Signal.DELETE_PLAYLIST,
                    )
                )
            }

            ObjectRequest.ARTIST_FROM_LIBRARY -> {
                submitBottomSheetList(
                    listOf(
                        Signal.STOP_FOLLOWING,
                        if (isPin) Signal.UNPIN_ARTIST else Signal.PIN_ARTIST,
                    )
                )
            }


            else -> submitBottomSheetList(listOf())
        }
    }

    private fun submitBottomSheetList(itemNames: List<Signal>){
        val adapter = BottomSheetItemAdapter(BottomSheetItemAdapter.OnClickListener {
            viewModel.sendSignal(it)
            viewModel.sendSignalComplete()
        })
        adapter.submitList(itemNames)
        binding.listOption.adapter = adapter
    }

    companion object{
        const val TAG = "ModalBottomSheet"
    }
}