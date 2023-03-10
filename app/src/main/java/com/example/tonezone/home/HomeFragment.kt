package com.example.tonezone.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.tonezone.MainViewModel
import com.example.tonezone.adapter.GroupPlaylistAdapter
import com.example.tonezone.adapter.PlaylistAdapter
import com.example.tonezone.databinding.FragmentHomeBinding
import com.example.tonezone.network.PlaylistInfo



class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private val mainViewModel: MainViewModel by activityViewModels()

    private val viewModel: HomeViewModel by activityViewModels {
        HomeViewModelFactory(mainViewModel.firebaseUser.value!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        createAdapterGroupPlaylist()
        observeNavigateToPlaylistDetails()

        binding.logout.setOnClickListener {
//            mainViewModel.logout()
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToUserProfileFragment())
        }


        return binding.root
    }

    private fun createAdapterGroupPlaylist(){
        val adapter = GroupPlaylistAdapter(PlaylistAdapter.OnClickListener {
            val playlistInfo = PlaylistInfo(
                it.id!!,
                it.name!!,
                it.description!!,
                it.images?.get(0)?.url,
                it.type!!
            )
            when(it.type){
                "playlist","album" -> viewModel.displayPlaylistDetails(playlistInfo)

                "artist" -> findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToArtistDetailsFragment(playlistInfo))
            }

        } )
        binding.groupPlaylist.adapter = adapter
    }

    private fun observeNavigateToPlaylistDetails(){
        viewModel.navigateToPlaylistDetails.observe(viewLifecycleOwner){
            if(it!=null) {
                this.findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToPlaylistDetailsFragment(it)
                )

                viewModel.displayPlaylistDetailsComplete()
            }
        }
    }
}