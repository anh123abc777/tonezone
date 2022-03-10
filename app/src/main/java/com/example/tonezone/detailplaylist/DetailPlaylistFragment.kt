package com.example.tonezone.detailplaylist

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.tonezone.adapter.OnClickListener
import com.example.tonezone.adapter.TrackAdapter
import com.example.tonezone.databinding.FragmentDetailPlaylistBinding
import com.example.tonezone.network.PlaylistInfo
import com.example.tonezone.player.PlayerScreenViewModel
import com.example.tonezone.player.PlayerScreenViewModelFactory

class DetailPlaylistFragment : Fragment() {

    private lateinit var binding: FragmentDetailPlaylistBinding
    private lateinit var viewModel: DetailPlaylistViewModel
    private lateinit var application: Application
    private lateinit var playlistInfo: PlaylistInfo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentDetailPlaylistBinding.inflate(inflater)
        playlistInfo = DetailPlaylistFragmentArgs.fromBundle(requireArguments()).playlistInfo
        application = requireNotNull(activity).application
        val factory = DetailPlaylistViewModelFactory(application,playlistInfo)
        viewModel = ViewModelProvider(this,factory).get(DetailPlaylistViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createAdapterPlaylist()


    }

    private fun createAdapterPlaylist(){

        val playerFactory = PlayerScreenViewModelFactory(application)
        val playerViewModel = ViewModelProvider(requireActivity(),playerFactory).get(PlayerScreenViewModel::class.java)

        val adapter = TrackAdapter(OnClickListener {
            val pos = viewModel.playlistItems.value!!.indexOf(it)
            playerViewModel.onPlay(playlistInfo.uri,pos)
            Toast.makeText(application, viewModel.playlistItems.value!!.size.toString(),Toast.LENGTH_SHORT).show()
        })
        binding.playlist.adapter = adapter

        viewModel.token.observe(viewLifecycleOwner){
            if (it!=null)
               viewModel.getDataPlaylistItems()
        }
        viewModel.playlistItems.observe(viewLifecycleOwner){}
    }


}