package com.example.tonezone.detailplaylist

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.tonezone.MainViewModel
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.databinding.FragmentPlaylistDetailsBinding
import com.example.tonezone.network.PlaylistInfo
import com.example.tonezone.player.PlayerScreenViewModel
import com.example.tonezone.utils.ModalBottomSheet
import com.example.tonezone.utils.ModalBottomSheetViewModel
import com.example.tonezone.utils.ObjectRequest
import com.example.tonezone.utils.convertSignalToText

class PlaylistDetailsFragment : Fragment() {

    private lateinit var binding: FragmentPlaylistDetailsBinding

    private val mainViewModel : MainViewModel by activityViewModels()

    private lateinit var playlistInfo : PlaylistInfo

    private val viewModel: PlaylistDetailsViewModel by viewModels {
        PlaylistDetailsViewModelFactory(mainViewModel.token,playlistInfo)
    }

    private val modalBottomSheetViewModel: ModalBottomSheetViewModel by activityViewModels()

    private val playerViewModel : PlayerScreenViewModel by activityViewModels()

    private lateinit var modalBottomSheet: ModalBottomSheet

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPlaylistDetailsBinding.inflate(inflater)
        playlistInfo = PlaylistDetailsFragmentArgs.fromBundle(requireArguments()).playlistInfo

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        modalBottomSheet = ModalBottomSheet(ObjectRequest.PLAYLIST)

        binding.moreOption.setOnClickListener {
            showBottomSheet()
        }
        handleSignalFromBottomSheet()

        return binding.root
    }

    private fun handleSignalFromBottomSheet(){

        modalBottomSheetViewModel.signal.observe(viewLifecycleOwner){
            when(it){
                null -> Log.i("signal","unknown value")
                else -> viewModel.receiveSignal(it)
            }
        }

        viewModel.signal.observe(viewLifecycleOwner){
            viewModel.handleSignal()
        }
    }

    private fun showBottomSheet(){
        modalBottomSheet.show(requireActivity().supportFragmentManager, ModalBottomSheet.TAG)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createAdapterPlaylist()
    }

    private fun createAdapterPlaylist(){

        val adapter = LibraryAdapter(LibraryAdapter.OnClickListener {
            val trackItem = it as LibraryAdapter.DataItem.TrackItem
            val pos = viewModel.playlistItems.value!!.indexOf(trackItem.track)
            playerViewModel.onPlay(playlistInfo.uri, pos)

        })

        binding.playlist.adapter = adapter

    }

}