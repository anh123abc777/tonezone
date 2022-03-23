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
import com.example.tonezone.R
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.databinding.FragmentPlaylistDetailsBinding
import com.example.tonezone.network.PlaylistInfo
import com.example.tonezone.network.Track
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
        PlaylistDetailsViewModelFactory(mainViewModel.token,playlistInfo,mainViewModel.userProfile)
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

        binding.moreOption.setOnClickListener {
            showBottomSheet(binding.moreOption.id,playlistInfo.id)
        }

        createAdapterPlaylist()

        handleSignalFromBottomSheet()

        return binding.root
    }

    private fun handleSignalFromBottomSheet(){

        modalBottomSheetViewModel.signal.observe(viewLifecycleOwner){
            when(it){
                null -> Log.i("signal","unknown value")
                else -> {
                    viewModel.receiveSignal(it)
                    Log.i("signal", convertSignalToText(it))
                }
            }
        }

        viewModel.signal.observe(viewLifecycleOwner){
            if (it!=null) {
                modalBottomSheet.dismiss()
                viewModel.handleSignal()
                viewModel.handleSignalComplete()
            }
        }
    }

    private fun showBottomSheet(buttonId: Int, objectId: String){
        setUpBottomSheet(buttonId,objectId)
        modalBottomSheet.show(requireActivity().supportFragmentManager, ModalBottomSheet.TAG)

    }

    private fun setUpBottomSheet(buttonId: Int, objectId: String){
        when(buttonId) {
            R.id.more_option -> {
                val isSaved = viewModel.checkIfUserFollowPlaylist()
                modalBottomSheet = ModalBottomSheet(ObjectRequest.PLAYLIST,isSaved)

            }

            R.id.more_option_with_track -> {
                val isSaved = viewModel.checkUserSavedTrack(objectId)
                modalBottomSheet = ModalBottomSheet(ObjectRequest.TRACK,isSaved)
            }

            else -> {
                Toast.makeText(context,"WTF is this $buttonId",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createAdapterPlaylist(){

        val adapter = LibraryAdapter(LibraryAdapter.OnClickListener { item, id ->
            val trackItem = item as LibraryAdapter.DataItem.TrackItem

            when(id) {
                null -> {
                    val pos = viewModel.playlistItems.value!!.indexOf(trackItem.track)
                    playerViewModel.onPlay(playlistInfo.uri, pos)
                }

                else -> {
                    showBottomSheet(id,item.id)
                }
            }
        })

        binding.playlist.adapter = adapter

    }

    private fun observeSelectedTrack(){
        viewModel.selectedTrack.observe(viewLifecycleOwner){ track ->
            if(track!= Track()){
                val pos = viewModel.playlistItems.value!!.indexOf(track)
                playerViewModel.onPlay(playlistInfo.uri,pos)
            }
        }
    }

}