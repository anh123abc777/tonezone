package com.example.tonezone.artistdetails

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.tonezone.MainViewModel
import com.example.tonezone.R
import com.example.tonezone.adapter.ArtistsAdapter
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.databinding.FragmentArtistDetailsBinding
import com.example.tonezone.network.Artist
import com.example.tonezone.network.PlaylistInfo
import com.example.tonezone.player.PlayerScreenViewModel
import com.example.tonezone.playlistdetails.PlaylistDetailsViewModel
import com.example.tonezone.playlistdetails.PlaylistDetailsViewModelFactory
import com.example.tonezone.utils.*

class ArtistDetailsFragment : Fragment() {

    private lateinit var binding: FragmentArtistDetailsBinding

    private val mainViewModel : MainViewModel by activityViewModels()

    private val playlistInfo : PlaylistInfo by lazy {
        ArtistDetailsFragmentArgs.fromBundle(requireArguments()).playlistInfo
    }

    private val playlistDetailsViewModel: PlaylistDetailsViewModel by viewModels {
        PlaylistDetailsViewModelFactory(mainViewModel.token,playlistInfo,mainViewModel.user.value!!)
    }

    private val playerViewModel: PlayerScreenViewModel by activityViewModels()

    private val viewModel: ArtistDetailsViewModel by viewModels {
        ArtistDetailsViewModelFactory(mainViewModel.token,playlistInfo,mainViewModel.user.value!!)
    }

    private val modalBottomSheetViewModel: ModalBottomSheetViewModel by activityViewModels()
    private lateinit var modalBottomSheet: ModalBottomSheet

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentArtistDetailsBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        createArtistTopTracksAdapter()
        createArtistAlbumsAdapter()
        createRelateArtistsAdapter()
        setupShowMoreTracks()
        setupShowMoreAlbums()
        handleOnPlay()
        setupBottomSheet()
        handleSignalFromBottomSheet()
        setupShowingArtistsBottomSheet()
        handleNavigateToPlaylistDetails()
        handleBackPress()

        viewModel.artist.observe(viewLifecycleOwner){
            if (it!=null){
                Log.i("getArtist","$it")
            }
        }

        return binding.root
    }

    private fun handleBackPress(){
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun handleOnPlay(){
        binding.playButton.setOnClickListener {
            playerViewModel.onInit(0,viewModel.tracks.value)
        }
    }

    private fun createArtistTopTracksAdapter(){
        val tracksAdapter = LibraryAdapter(LibraryAdapter.OnClickListener { item, idButton ->
            val trackItem = item as LibraryAdapter.DataItem.TrackItem

            when(idButton) {
                null -> {
                    val pos = playlistDetailsViewModel.playlistItems.value!!.indexOf(trackItem.track)
//                    playerViewModel.onInit(playlistInfo.uri, pos)
                }

                else -> {
                    playlistDetailsViewModel.showBottomSheet(trackItem.track.id,idButton)
                }
            }
        })
        tracksAdapter.setLimitItem(6)
        binding.topTracksOfArtist.adapter = tracksAdapter
    }

    private fun createArtistAlbumsAdapter(){
        val albumsAdapter = LibraryAdapter(LibraryAdapter
            .OnClickListener{ item, _ ->
                viewModel.displayPlaylistDetails(item)
            })
        albumsAdapter.setLimitItem(6)
        binding.artistAlbums.adapter = albumsAdapter
    }

    private fun createRelateArtistsAdapter(){
        val relateArtistsAdapter = ArtistsAdapter(ArtistsAdapter
            .OnClickListener{ item ->
                val relateArtist = PlaylistInfo(
                    item.id!!,
                    item.name!!,
                    item.popularity.toString(),
                    item.images?.get(0)?.url,
                    item.type!!)

                val bundle = bundleOf( "playlistInfo" to relateArtist)
                findNavController().navigate(R.id.artistDetailsFragment,bundle)
        })
        binding.relateArtists.adapter = relateArtistsAdapter
    }

    private fun handleNavigateToPlaylistDetails() {
        viewModel.navigateToDetailPlaylist.observe(viewLifecycleOwner) {
            if (it!=null){
                   this.findNavController()
                        .navigate(
                            ArtistDetailsFragmentDirections
                                .actionArtistDetailsFragmentToPlaylistDetailsFragment(it)
                        )

                viewModel.displayPlaylistDetailsComplete()
            }
        }
    }

    private fun setupShowMoreTracks(){
        viewModel.isNavigateToMoreTracks.observe(viewLifecycleOwner){
            if (it!=null){
                findNavController()
                    .navigate(ArtistDetailsFragmentDirections
                        .actionArtistDetailsFragmentToPlaylistDetailsFragment(it))
                viewModel.navigateToMoreTracksComplete()
            }
        }
    }

    private fun setupShowMoreAlbums(){
        viewModel.isNavigateToMoreAlbums.observe(viewLifecycleOwner){
            if (it!=null){
                findNavController()
                    .navigate(ArtistDetailsFragmentDirections
                        .actionArtistDetailsFragmentToPlaylistsFragment(it))
                viewModel.navigateToMoreAlbumsComplete()
            }
        }
    }

    private fun handleSignalFromBottomSheet(){
        modalBottomSheetViewModel.signal.observe(viewLifecycleOwner){
            when(it){
                null -> Log.i("receivedSignal","unknown value")
                else -> {
                    playlistDetailsViewModel.receiveSignal(it)
                    Log.i("receivedSignal", convertSignalToText(it))
                }
            }
        }

        playlistDetailsViewModel.receivedSignal.observe(viewLifecycleOwner){
            if (it!=null) {
                modalBottomSheet.dismiss()
                playlistDetailsViewModel.handleSignal()
                playlistDetailsViewModel.handleSignalComplete()
            }
        }
    }

    private fun setupBottomSheet(){
        playlistDetailsViewModel.selectedObjectID.observe(viewLifecycleOwner){
            if(it!=null){
                setUpItemsBottomSheet(it.first,it.second)
                modalBottomSheet.show(requireActivity().supportFragmentManager, ModalBottomSheet.TAG)
            }
        }
    }

    private fun setUpItemsBottomSheet(objectId: String, buttonId: Int){
        when(buttonId) {
            R.id.more_option -> {
                modalBottomSheet = ModalBottomSheet(ObjectRequest.PLAYLIST,playlistDetailsViewModel.isUserPlaylistFollowed.value)
            }

            R.id.more_option_with_track -> {
                val isSaved = playlistDetailsViewModel.checkedTrackIsLiked()
                modalBottomSheet = ModalBottomSheet(ObjectRequest.TRACK,isSaved)
            }

            else -> {
                val isSaved = playlistDetailsViewModel.checkedTrackIsLiked()
                modalBottomSheet = ModalBottomSheet(ObjectRequest.TRACK,isSaved)
            }
        }
    }

    private fun setupShowingArtistsBottomSheet(){
        playlistDetailsViewModel.isShowingTrackDetails.observe(viewLifecycleOwner){
            when (it) {

                Signal.VIEW_ARTIST -> {
                    modalBottomSheet.dismiss()
                    val artistsOfTrack = playlistDetailsViewModel.playlistItems.value?.find { track ->
                        track.id == playlistDetailsViewModel.selectedObjectID.value?.first
                    }?.artists ?: listOf(Artist(), Artist())
                    Log.i("setUpShowingArtists", artistsOfTrack.toString())
                    val artistsModalBottomSheet = ArtistsModalBottomSheet(artistsOfTrack)
                    artistsModalBottomSheet.show(
                        requireActivity().supportFragmentManager,
                        ArtistsModalBottomSheet.TAG
                    )
                    playlistDetailsViewModel.showTracksDetailsComplete()
                }

                Signal.VIEW_ALBUM -> {
                    playlistDetailsViewModel.showTracksDetailsComplete()
                }

                else -> Log.i("isShowingTrackDetails","Nothing")
            }
        }
    }

}