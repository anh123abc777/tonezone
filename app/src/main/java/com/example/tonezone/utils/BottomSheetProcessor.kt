package com.example.tonezone.utils

import android.app.Activity
import android.util.Log
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.tonezone.R
import com.example.tonezone.network.Artist
import com.example.tonezone.network.FirebaseRepository
import com.example.tonezone.network.Track
import com.example.tonezone.player.PlayerScreenViewModel
import com.example.tonezone.playlistdetails.PlaylistDetailsFragmentDirections
import com.example.tonezone.playlistdetails.PlaylistDetailsViewModel
import com.example.tonezone.playlistdetails.PlaylistDetailsViewModelFactory
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

class BottomSheetProcessor(
                           private val playerViewModel: PlayerScreenViewModel,
                           private val viewModel: PlaylistDetailsViewModel,
                            private val viewLifecycleOwner: LifecycleOwner,
                           private val activity: FragmentActivity,
                           ) {

    private val modalBottomSheetViewModel: ModalBottomSheetViewModel by lazy {
        ViewModelProvider(activity).get(ModalBottomSheetViewModel::class.java)
    }

    private val navController: NavController by lazy {
        activity.findNavController(R.id.nav_host)
    }

    private lateinit var modalBottomSheet: ModalBottomSheet

    init {
        setupBottomSheet()

        handleSignalFromBottomSheet()

        setupShowingArtistsBottomSheet()

        observeNavigateToYourPlaylists()

        handleLikeButtonVisibility()

        handlePlayingTrack()

        handleSignalAddTracks()

        handleAddToOtherPlaylist()

        handleRemoveTrack()

        initStateLikedItems()

    }

    private fun initStateLikedItems(){
        viewModel.playlistItems.observe(viewLifecycleOwner){
            if (it!=null){
                viewModel.initStateLikedItems()
                Log.i("bottomSheet","$it")
            }
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
                    activity.onBackPressed()
                }
            }
        }
    }

    private fun setupBottomSheet(){
        viewModel.selectedObjectID.observe(viewLifecycleOwner){
            if(it!=null){
                setUpItemsBottomSheet(it.first,it.second)
                modalBottomSheet.show(activity.supportFragmentManager, ModalBottomSheet.TAG)
            }
        }
    }
    private fun setUpItemsBottomSheet(objectId: String, buttonId: Int){
        val isOwned = viewModel.isOwnedByUser.value
        when(buttonId) {
            R.id.more_option -> {
                val isSaved = viewModel.isUserPlaylistFollowed.value
                modalBottomSheet = when (isOwned) {
                    null -> ModalBottomSheet(ObjectRequest.LIKED_PLAYLIST,false)
                    true -> ModalBottomSheet(ObjectRequest.YOUR_PLAYLIST,false)
                    else -> ModalBottomSheet(ObjectRequest.PLAYLIST,isSaved)
                }
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

    private fun handlePlayingTrack(){
        if (playerViewModel.playerState.value== PlayerScreenViewModel.PlayerState.NONE) {
            playerViewModel.currentTrack.observe(activity) {
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

                    val artistsLiveData = FirebaseRepository().getSeveralArtists(artistsOfTrack.map { it.id!! })

                    var observer = Observer<List<Artist>>{}

                    observer = Observer { artists ->
                        if (artists.size==artistsOfTrack.size){
                            val artistsModalBottomSheet = ArtistsModalBottomSheet(artists)
                            artistsModalBottomSheet.show(
                                activity.supportFragmentManager,
                                ArtistsModalBottomSheet.TAG
                            )

                            viewModel.showTracksDetailsComplete()
                            artistsLiveData.removeObserver(observer)
                        }
                    }

                    artistsLiveData.observeForever(observer)

                }

                Signal.VIEW_ALBUM -> {
                    modalBottomSheet.dismiss()
                    val albumsOfTrack = listOf(viewModel.playlistItems.value?.find { track ->
                        track.id == viewModel.selectedObjectID.value?.first
                    }?.album)
                    Log.i("setUpShowingArtists", albumsOfTrack.toString())
                    val artistsModalBottomSheet = ArtistsModalBottomSheet(albumsOfTrack)
                    artistsModalBottomSheet.show(
                        activity.supportFragmentManager,
                        ArtistsModalBottomSheet.TAG
                    )
                    viewModel.showTracksDetailsComplete()
                }

                else -> Log.i("isShowingTrackDetails","Nothing")
            }
        }
    }

    private fun observeNavigateToYourPlaylists(){
        viewModel.navigateYourPlaylists.observe(viewLifecycleOwner){
            if (it!=null){
                val bundle = bundleOf("trackID" to it)
                navController.navigate(R.id.yourPlaylistFragment,bundle)

                viewModel.addToPlaylistComplete()
            }
        }
    }

    private fun handleAddToOtherPlaylist(){
        viewModel.isRequestingToAddToOtherPlaylist.observe(viewLifecycleOwner){
            if (it){
                val bundle = bundleOf("trackIds" to viewModel.playlistItems.value!!.map { it.id }.toTypedArray())
                navController.navigate(R.id.yourPlaylistFragment,bundle)
                viewModel.addToOtherPlaylistComplete()
            }
        }
    }

    private fun handleRemoveTrack(){
        viewModel.removeTrack.observe(viewLifecycleOwner){
            if (it!=null){
//                    (binding.playlist.adapter as LibraryAdapter).notifyItemRemoved(it.first)

                Snackbar.make(activity.window.decorView.findViewById(android.R.id.content), "Track is removed", Snackbar.LENGTH_LONG)
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

    private fun handleSignalAddTracks(){
        viewModel.isRequestingToAddTracks.observe(viewLifecycleOwner){
            if (it!=null){
                val bundle = bundleOf("playlistID" to it)
                navController.navigate(R.id.addTracksFragment,bundle)
                viewModel.navigateToTheAddSongViewComplete()
            }
        }
    }


}