package com.example.tonezone.player.currentplaylist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.tonezone.R
import com.example.tonezone.adapter.TrackAdapter
import com.example.tonezone.databinding.FragmentCurrentPlaylistBinding
import com.example.tonezone.network.Track
import com.example.tonezone.player.PlayerScreenViewModel
import com.example.tonezone.utils.ArtistsModalBottomSheet
import com.example.tonezone.utils.CustomItemTouchHelper
import com.example.tonezone.utils.YourPlaylistBottomSheet

class CurrentPlaylistFragment : Fragment() {

    private lateinit var binding : FragmentCurrentPlaylistBinding
    private val playerViewModel : PlayerScreenViewModel by activityViewModels()

    private val callback : CustomItemTouchHelper by lazy {
        CustomItemTouchHelper(playerViewModel)
    }
    private val itemTouchHelper : ItemTouchHelper by lazy {
        ItemTouchHelper(callback)
    }

    private val adapter = TrackAdapter(TrackAdapter.OnClickListener{ track, actionID ->
        when(actionID){
            0 -> playerViewModel.onPlay(track)
            1 -> handleLongPressItem()
            2 -> playerViewModel.selectTrack(track)
            3 -> playerViewModel.unselectTrack(track)
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentCurrentPlaylistBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = playerViewModel

        createCurrentPlaylistAdapter()
        handleSelectedTracks()
        handleBackPress()
        handleDragAndDropEvent()
        handleViewVisibility()
        setupOptionToolbar()
        setupToolbar()

        return binding.root
    }

    private fun handleDragAndDropEvent(){
        itemTouchHelper.attachToRecyclerView(binding.recyclerviewCurrentPlaylist)
    }

    private fun handleBackPress(){
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            playerViewModel.clearSelectedTracks()

            if (playerViewModel.isLongPress.value==View.GONE){
                playerViewModel.handleLongPressEventComplete()
            }else{
                findNavController().popBackStack()
                findNavController().navigate(R.id.playerScreenFragment)
            }

            true
        }
    }


    private fun handleSelectedTracks(){
        playerViewModel.selectedTracks.observe(viewLifecycleOwner){
                adapter.addHeaderAndSubmitList(
                    playerViewModel.currentPlaylist.value,
                    playerViewModel.currentTrack.value,
                    playerViewModel.selectedTracks.value!!
                )

                binding.hideButton.isClickable = it.isNotEmpty()
                binding.addToPlaylistButton.isClickable = it.isNotEmpty()
        }

    }

    private fun handleViewVisibility(){
        playerViewModel.isLongPress.observe(viewLifecycleOwner){
            if (it==View.VISIBLE) {
                playerViewModel.clearSelectedTracks()
                adapter.disappearCheckboxes()
            }
            else {
                adapter.showCheckboxes()
            }
        }
    }

    private fun createCurrentPlaylistAdapter(){

        playerViewModel.currentTrack.observe(viewLifecycleOwner) {
            if (it!= Track()) {
                adapter.addHeaderAndSubmitList(
                    playerViewModel.currentPlaylist.value,
                    playerViewModel.currentTrack.value,
                    playerViewModel.selectedTracks.value!!
                )
            }
        }

        playerViewModel.currentPlaylist.observe(viewLifecycleOwner){
            if (it!=null){
                adapter.addHeaderAndSubmitList(
                    playerViewModel.currentPlaylist.value,
                    playerViewModel.currentTrack.value,
                    playerViewModel.selectedTracks.value!!
                )
            }
        }

        playerViewModel.darkColorOnPrimary.observe(viewLifecycleOwner){
            if (it!=null){
                adapter.setDarkColor(it)
            }
        }
        
        playerViewModel.colorOnPrimary.observe(viewLifecycleOwner){
            if (it!=null){
                adapter.setColor(it)
            }
        }

        playerViewModel.lightBackgroundDrawable.observe(viewLifecycleOwner){
            if (it!=null){
                adapter.setBackgroundChoiceTrack(it)
            }
        }
        
        binding.recyclerviewCurrentPlaylist.adapter = adapter
    }

    private fun handleLongPressItem(){
        playerViewModel.receiveLongPressEvent()
    }


    private fun setupToolbar(){
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupOptionToolbar(){
        handleClickEventSelectedOptionLayout()
        handleClickEventOptionToolbar()
        playerViewModel.selectedTracks.observe(viewLifecycleOwner) {
            if (it!=null)
                binding.selectedOptionToolbar.title = "Songs ${it.size}"
        }
    }

    private fun handleClickEventOptionToolbar(){

        playerViewModel.selectedTracks.observe(viewLifecycleOwner){
            if (it.isEmpty())
                binding.selectedOptionToolbar.setNavigationIcon(R.drawable.ic_unchecked)
            if(it.size== playerViewModel.currentPlaylist.value?.size ?: 0)
                binding.selectedOptionToolbar.setNavigationIcon(R.drawable.ic_check)
        }

        var isCheckAll = false
        binding.selectedOptionToolbar.setNavigationOnClickListener {
            when (isCheckAll) {
                false -> {
                    playerViewModel.selectALlTracks()
                }

                true -> {
                    playerViewModel.clearSelectedTracks()
                }
            }
            isCheckAll = !isCheckAll
        }
    }

    private fun handleClickEventSelectedOptionLayout(){
//        }
        binding.addToPlaylistButton.setOnClickListener {
            val yourPlaylistModalBottomSheet = YourPlaylistBottomSheet(playerViewModel.selectedTracks.value!!,playerViewModel.user!!)
            yourPlaylistModalBottomSheet.show(
                requireActivity().supportFragmentManager,
                ArtistsModalBottomSheet.TAG
            )
        }
    }

}