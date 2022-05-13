package com.example.tonezone.artistdetails

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
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
import com.example.tonezone.network.Track
import com.example.tonezone.player.PlayerScreenViewModel
import com.example.tonezone.playlistdetails.PlaylistDetailsFragmentDirections
import com.example.tonezone.playlistdetails.PlaylistDetailsViewModel
import com.example.tonezone.playlistdetails.PlaylistDetailsViewModelFactory
import com.example.tonezone.utils.*
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener


class ArtistDetailsFragment : Fragment() {

    private lateinit var binding: FragmentArtistDetailsBinding

    private val mainViewModel : MainViewModel by activityViewModels()

    private val playlistInfo : PlaylistInfo by lazy {
        ArtistDetailsFragmentArgs.fromBundle(requireArguments()).playlistInfo
    }

    private val playlistDetailsViewModel: PlaylistDetailsViewModel by viewModels {
        PlaylistDetailsViewModelFactory(playlistInfo,mainViewModel.firebaseAuth.value!!)
    }

    private val playerViewModel: PlayerScreenViewModel by activityViewModels()

    private val viewModel: ArtistDetailsViewModel by viewModels {
        ArtistDetailsViewModelFactory(playlistInfo,mainViewModel.firebaseAuth.value!!)
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

        handleNavigateToPlaylistDetails()

        handleBackPress()

        setupAppbar()

        handleStatePlayer()

        setupAlbumsOfArtist()

        BottomSheetProcessor(
            playerViewModel,
            playlistDetailsViewModel,
            viewLifecycleOwner,
            requireActivity())

        return binding.root
    }

    private fun setupAlbumsOfArtist(){
        viewModel.artistAlbums.observe(viewLifecycleOwner){
            if (it!=null){
                binding.albumsTxt.visibility = View.VISIBLE
                binding.moreAlbum.visibility = View.VISIBLE
            }else{
                binding.albumsTxt.visibility = View.GONE
                binding.moreAlbum.visibility = View.VISIBLE
            }
        }
    }

    private fun handleStatePlayer(){
        playerViewModel.currentPlaylist.observe(viewLifecycleOwner) { tracks ->
            playerViewModel.playerState.observe(viewLifecycleOwner){ state ->
                /**Something new**/
                if (state == PlayerScreenViewModel.PlayerState.PLAY && tracks == playlistDetailsViewModel.playlistItems.value) {
                    binding.playButton.setIconResource(R.drawable.ic_pause)
                } else {
                    binding.playButton.setIconResource(R.drawable.ic_play_arrow)
                }
            }
        }
    }

    private fun initStateLikedItems(){
        playlistDetailsViewModel.playlistItems.observe(viewLifecycleOwner){
            if (it!=null){
                playlistDetailsViewModel.initStateLikedItems()
            }
        }
    }

    private fun setupAppbar(){
        binding.appBarLayout.addOnOffsetChangedListener(object : OnOffsetChangedListener {
            var scrollRange = -1
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                //Initialize the size of the scroll
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.totalScrollRange
                }
                //Check if the view is collapsed
                if (scrollRange + verticalOffset == 0) {
                    binding.toolbar.setBackgroundColor(
                        ContextCompat.getColor(
                            context!!,
                            R.color.black
                        )
                    )
                    binding.collapsingToolbarLayout.title = viewModel.artist.value?.name
                    binding.toolbar.setTitleTextColor(Color.WHITE)

                } else {
                    binding.toolbar.setBackgroundColor(
                            Color.TRANSPARENT
                    )
                    binding.toolbar.setTitleTextColor(Color.TRANSPARENT)
                    binding.toolbar.title = ""
                    binding.collapsingToolbarLayout.title = ""
                }
            }
        })
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

    private fun handlePlayingTrack(){
        if (playerViewModel.playerState.value==PlayerScreenViewModel.PlayerState.NONE) {
            playerViewModel.currentTrack.observe(requireActivity()) {
                if (it != Track()) {
                    playerViewModel.initSeekBar()
                    playerViewModel.onPlay()
                    playerViewModel.initPrimaryColor()
                }
            }
        }
    }

    private fun createArtistTopTracksAdapter(){
        val tracksAdapter = LibraryAdapter(LibraryAdapter.OnClickListener { item, idButton ->
            val trackItem = item as LibraryAdapter.DataItem.TrackItem

            when(idButton) {
                null -> {
                    val pos = playlistDetailsViewModel.playlistItems.value!!.indexOf(trackItem.track)
                    playerViewModel.onInit(pos,viewModel.tracks.value)
                }

                else -> {
                    playlistDetailsViewModel.showBottomSheet(trackItem.track.id,idButton)
                }
            }
        })
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

    private fun observeNavigateToYourPlaylists(){
        playlistDetailsViewModel.navigateYourPlaylists.observe(viewLifecycleOwner){
            if (it!=null){
                findNavController().navigate(
                    ArtistDetailsFragmentDirections
                        .actionArtistDetailsFragmentToYourPlaylistFragment(it))

                playlistDetailsViewModel.addToPlaylistComplete()
            }
        }
    }

}