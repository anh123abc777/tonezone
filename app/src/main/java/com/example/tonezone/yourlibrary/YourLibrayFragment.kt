package com.example.tonezone.yourlibrary

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.tonezone.adapter.GenreAdapter
import com.example.tonezone.adapter.PlaylistAdapter
import com.example.tonezone.databinding.FragmentYourLibraryBinding

class YourLibraryFragment : Fragment() {

    private lateinit var binding : FragmentYourLibraryBinding
    private lateinit var viewModel:  YourLibraryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentYourLibraryBinding.inflate(inflater)
        val application = requireNotNull(activity).application
        val factory = YourLibraryViewModelFactory(application)
        viewModel = ViewModelProvider(this,factory).get(YourLibraryViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        observeToken()
        observeUserPlaylists()
        setupAdapterPlaylist()

        return binding.root
    }

    private fun setupAdapterPlaylist(){
        val adapter = PlaylistAdapter()
        binding.yourLibraryList.adapter = adapter

    }

    private fun observeToken(){
        viewModel.token.observe(viewLifecycleOwner){
            if(it!=null){
                viewModel.getDataUserPlaylists()
            }
        }
    }

    private fun observeUserPlaylists(){
        viewModel.userPlaylists.observe(viewLifecycleOwner){}
    }

}