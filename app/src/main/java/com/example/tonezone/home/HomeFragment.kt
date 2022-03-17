package com.example.tonezone.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.tonezone.adapter.GroupPlaylistAdapter
import com.example.tonezone.adapter.PlaylistAdapter
import com.example.tonezone.databinding.FragmentHomeBinding
import com.example.tonezone.network.PlaylistInfo


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val application = requireNotNull(activity).application
        val factory = HomeViewModelFactory(application)
        viewModel = ViewModelProvider(this,factory).get(HomeViewModel::class.java)
        binding = FragmentHomeBinding.inflate(inflater)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        observeToken()
        observeGroupPlaylistsData()
        createAdapterGroupPlaylist()

        return binding.root
    }

    private fun createAdapterGroupPlaylist(){
        val adapter = GroupPlaylistAdapter(PlaylistAdapter.OnClickListener {

            findNavController().navigate(HomeFragmentDirections
                .actionHomeFragmentToDetailPlaylistFragment(PlaylistInfo(
                    it.id,
                    it.name,
                    it.description,
                    it.images?.get(0)?.url,
                    it.uri,
                    it.type
                )))
        })
        binding.groupPlaylist.adapter = adapter
    }

    private fun observeToken(){
        viewModel.token.observe(viewLifecycleOwner){
            if(it!=null) {
                viewModel.getGroupPlaylistsData()
            }
        }
    }

    private fun observeGroupPlaylistsData(){
        viewModel.groupPlaylists.observe(viewLifecycleOwner){}
    }

}