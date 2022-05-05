package com.example.tonezone.playlistdetails

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
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.databinding.FragmentPlaylistDetailsBinding
import com.example.tonezone.network.PlaylistInfo
import com.example.tonezone.network.Track
import com.example.tonezone.player.PlayerScreenViewModel
import com.example.tonezone.utils.*
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

class PlaylistDetailsFragment : Fragment() {

    private lateinit var binding: FragmentPlaylistDetailsBinding

    private val mainViewModel : MainViewModel by activityViewModels()

    private lateinit var playlistInfo : PlaylistInfo

    private val viewModel: PlaylistDetailsViewModel by viewModels {
        PlaylistDetailsViewModelFactory(mainViewModel.token,playlistInfo,
             mainViewModel.firebaseAuth.value!!
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

        observeNavigateToYourPlaylists()

        handleLikeButtonVisibility()

        handlePlayPlaylist()

        handleAddToQueue()

        handlePlayingTrack()

        handleSignalAddTracks()

        setupAppbar()

        handleBackPress()

        handleStatePlayer()

        handleAddToOtherPlaylist()

        handleRemoveTrack()

        viewModel.playlistItems.observe(viewLifecycleOwner){
            if (it!=null){
                viewModel.initStateLikedItems()
            }
        }

        return binding.root
    }

    private fun handleStatePlayer(){
        playerViewModel.playerState.observe(viewLifecycleOwner){ state ->
            playerViewModel.currentPlaylist.observe(viewLifecycleOwner) { tracks ->

                /**Something new**/
                if (state == PlayerScreenViewModel.PlayerState.PLAY && tracks == viewModel.playlistItems) {
                    binding.play.setIconResource(R.drawable.ic_pause)
                } else {
                    binding.play.setIconResource(R.drawable.ic_play_arrow)
                }
            }
        }
    }

    private fun handleSignalAddTracks(){
        viewModel.isRequestingToAddTracks.observe(viewLifecycleOwner){
            if (it!=null){
                val bundle = bundleOf("playlistID" to it)
                findNavController().navigate(R.id.addTracksFragment,bundle)
                viewModel.navigateToTheAddSongViewComplete()
            }
        }
    }

    private fun setupAppbar(){
        binding.appBarLayout.addOnOffsetChangedListener(object :
            AppBarLayout.OnOffsetChangedListener {
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
                    binding.collapsingToolbarLayout.title = viewModel.playlistInfo.name
                    binding.toolbar.setTitleTextColor(Color.WHITE)

                } else {
                    binding.toolbar.setBackgroundColor(
                        Color.TRANSPARENT
                    )
                    binding.toolbar.setTitleTextColor(Color.TRANSPARENT)
                    binding.toolbar.title = ""
                    binding.collapsingToolbarLayout.title = ""

                }

                val percentage = Math.abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
                if (Math.abs(verticalOffset) == appBarLayout.totalScrollRange) {
                    //  Collapsed
                    //Hide your TextView here
                    binding.playlistProfile.visibility = View.INVISIBLE
                } else if (verticalOffset == 0) {
                    //Expanded
                    //Show your TextView here
                    binding.playlistProfile.visibility = View.VISIBLE
                } else {
                    //In Between
                    binding.playlistProfile.visibility = View.VISIBLE
                }

            }
        })
    }

    private fun handleAddToQueue(){
        viewModel.queueTrack.observe(viewLifecycleOwner){
            if (it!=null){
                playerViewModel.addToQueue(it)
                viewModel.addToQueueComplete()
            }
        }
    }

    private fun handleBackPress(){
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun handlePlayPlaylist(){
        binding.play.setOnClickListener {
            playerViewModel.onInit(0,viewModel.playlistItems.value)
            viewModel.saveHistory()
        }
    }

    private fun handleLikeButtonVisibility(){
        viewModel.currentPlaylist.observe(viewLifecycleOwner){
            if (it!=null){
                viewModel.checkIsOwnedByUser()
            }
        }
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
                if (it==Signal.DELETE_PLAYLIST){
                    requireActivity().onBackPressed()
                }
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
        val isOwned = viewModel.isOwnedByUser.value
        when(buttonId) {
            R.id.more_option -> {
                val isSaved = viewModel.isUserPlaylistFollowed.value
                modalBottomSheet = if (isOwned == true)
                    ModalBottomSheet(ObjectRequest.YOUR_PLAYLIST,isSaved)
                else
                    ModalBottomSheet(ObjectRequest.PLAYLIST,isSaved)
            }

            R.id.more_option_with_track -> {

                val isSaved = viewModel.checkedTrackIsLiked()
                modalBottomSheet = if (isOwned == true)
                    ModalBottomSheet(ObjectRequest.TRACK_IN_YOUR_PLAYLIST,isSaved)
                else
                    ModalBottomSheet(ObjectRequest.TRACK,isSaved)

            }

            else -> {
                val isSaved = viewModel.checkedTrackIsLiked()
                modalBottomSheet = ModalBottomSheet(ObjectRequest.TRACK,isSaved)
            }
        }
    }

    private fun createAdapterPlaylist(){
        val adapter = LibraryAdapter(LibraryAdapter.OnClickListener { item, id ->
            val trackItem = item as LibraryAdapter.DataItem.TrackItem

            when(id) {
                null -> {
                    val pos = viewModel.playlistItems.value!!.indexOf(trackItem.track)
                    playerViewModel.onInit(pos,viewModel.playlistItems.value)
                    viewModel.saveHistory()
                }

                else -> {
                    viewModel.showBottomSheet(trackItem.track.id,id)
                }
            }
        })

        binding.playlist.adapter = adapter
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

    private fun setupShowingArtistsBottomSheet(){
        viewModel.isShowingTrackDetails.observe(viewLifecycleOwner){
            when (it) {

                Signal.VIEW_ARTIST -> {
                    modalBottomSheet.dismiss()
                    val artistsOfTrack = viewModel.playlistItems.value?.find { track ->
                        track.id == viewModel.selectedObjectID.value?.first
                    }?.album!!.artists ?: listOf()

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

    private fun observeNavigateToYourPlaylists(){
        viewModel.navigateYourPlaylists.observe(viewLifecycleOwner){
            if (it!=null){
                findNavController().navigate(
                    PlaylistDetailsFragmentDirections
                        .actionPlaylistDetailsFragmentToYourPlaylistFragment(it))

                viewModel.addToPlaylistComplete()
            }
        }
    }

    private fun handleAddToOtherPlaylist(){
        viewModel.isRequestingToAddToOtherPlaylist.observe(viewLifecycleOwner){
            if (it){
                val bundle = bundleOf("trackIds" to viewModel.playlistItems.value!!.map { it.id }.toTypedArray())
                findNavController().navigate(R.id.yourPlaylistFragment,bundle)
                viewModel.addToOtherPlaylistComplete()
            }
        }
    }

    private fun handleRemoveTrack(){
        viewModel.removeTrack.observe(viewLifecycleOwner){
            if (it!=null){
//                    (binding.playlist.adapter as LibraryAdapter).notifyItemRemoved(it.first)

                Snackbar.make(requireActivity().window.decorView.findViewById(android.R.id.content), "Track is removed", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        viewModel.undoRemove()
                    }.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>(){
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)
                            if (event == DISMISS_EVENT_TIMEOUT){
                                viewModel.removeFromThisPlaylistForever()
                                viewModel.removeTrackFromThisPlaylistComplete()
                            }
                        }
                    })
                    .show()
            }
        }
    }


}