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
import com.example.tonezone.network.Artist
import com.example.tonezone.network.PlaylistInfo
import com.example.tonezone.player.PlayerScreenViewModel
import com.example.tonezone.utils.*

class PlaylistDetailsFragment : Fragment() {

    private lateinit var binding: FragmentPlaylistDetailsBinding

    private val mainViewModel : MainViewModel by activityViewModels()

    private lateinit var playlistInfo : PlaylistInfo

    private val viewModel: PlaylistDetailsViewModel by viewModels {
        PlaylistDetailsViewModelFactory(mainViewModel.token,playlistInfo,
            mainViewModel.user.value!!
        )
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

        setupBottomSheet()

        createAdapterPlaylist()

        handleSignalFromBottomSheet()

        setupShowingArtistsBottomSheet()

        return binding.root
    }

    private fun handleSignalFromBottomSheet(){

        modalBottomSheetViewModel.signal.observe(viewLifecycleOwner){
            when(it){
                null -> Log.i("receivedSignal","unknown value")
                else -> {
                    viewModel.receiveSignal(it)
                    Log.i("receivedSignal", convertSignalToText(it))
                }
            }
        }

        viewModel.receivedSignal.observe(viewLifecycleOwner){
            if (it!=null) {
                modalBottomSheet.dismiss()
                viewModel.handleSignal()
                viewModel.handleSignalComplete()
            }
        }
    }

    private fun setupBottomSheet(){

        viewModel.selectedObjectID.observe(viewLifecycleOwner){
            if(it!=null){
                setUpItemsBottomSheet(it.first,it.second)
                modalBottomSheet.show(requireActivity().supportFragmentManager, ModalBottomSheet.TAG)
            }
        }

    }

    private fun setUpItemsBottomSheet(objectId: String, buttonId: Int){
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
                    viewModel.showBottomSheet(trackItem.track.id,id)
                }
            }
        })

        binding.playlist.adapter = adapter
    }

    private fun setupShowingArtistsBottomSheet(){
        viewModel.isShowingTrackDetails.observe(viewLifecycleOwner){
            when (it) {

                Signal.VIEW_ARTIST -> {
                    modalBottomSheet.dismiss()
                    val artistsOfTrack = viewModel.playlistItems.value?.find { track ->
                        track.id == viewModel.selectedObjectID.value?.first
                    }?.artists ?: listOf(Artist(), Artist())
                    Log.i("setUpShowingArtists", artistsOfTrack.toString())
                    val artistsModalBottomSheet = ArtistsModalBottomSheet(artistsOfTrack)
                    artistsModalBottomSheet.show(
                        requireActivity().supportFragmentManager,
                        ArtistsModalBottomSheet.TAG
                    )
                    viewModel.showTracksDetailsComplete()
                }

                Signal.VIEW_ALBUM -> {
//                    modalBottomSheet.dismiss()
//                    val albumsOfTrack = listOf(viewModel.playlistItems.value?.find { track ->
//                        track.id == viewModel.selectedObjectID.value?.first
//                    }?.album)
//                    Log.i("setUpShowingArtists", albumsOfTrack.toString())
//                    val artistsModalBottomSheet = ArtistsModalBottomSheet(albumsOfTrack)
//                    artistsModalBottomSheet.show(
//                        requireActivity().supportFragmentManager,
//                        ArtistsModalBottomSheet.TAG
//                    )
                    viewModel.showTracksDetailsComplete()
                }

                else -> Log.i("isShowingTrackDetails","Nothing")
            }
        }
    }
}