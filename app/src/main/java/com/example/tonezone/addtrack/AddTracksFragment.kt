package com.example.tonezone.addtrack

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.tonezone.MainViewModel
import com.example.tonezone.adapter.GroupTracksAdapter
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.databinding.FragmentAddTracksBinding
import com.example.tonezone.network.Playlist
import com.example.tonezone.network.Track
import com.example.tonezone.player.PlayerScreenViewModel

class AddTracksFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()

    private val viewModel: AddTrackViewModel by viewModels {
        AddTracksViewModelFactory(mainViewModel.firebaseUser.value!!)
    }

    private val playerViewModel: PlayerScreenViewModel by activityViewModels()

    private val playlistID: String by lazy {
        AddTracksFragmentArgs.fromBundle(requireArguments()).playlistID
    }

    private lateinit var binding: FragmentAddTracksBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddTracksBinding.inflate(inflater)

        setupViewPaper()
        handleNavigateSearch()

        return binding.root
    }

    private fun setupViewPaper(){
        val adapter = GroupTracksAdapter(LibraryAdapter.OnClickListener{_,_ ->},playlistID,playerViewModel)
        binding.groupTracks.adapter = adapter
        viewModel.groupTracks.observe(viewLifecycleOwner){
            if (it!=null){
                Log.i("FirebaseRepo"," lo l $it")
                adapter.submitList(listOf(Playlist("1",name = "recommendation",deltailTracks = it),
                    Playlist("2",name = "something",deltailTracks = listOf(Track(), Track()))))
            }
        }
    }

    private fun handleNavigateSearch(){
        binding.searchButton.setOnClickListener {
            findNavController().navigate(AddTracksFragmentDirections.actionAddTracksFragmentToSearchForItemFragment(playlistID))
        }
    }
}