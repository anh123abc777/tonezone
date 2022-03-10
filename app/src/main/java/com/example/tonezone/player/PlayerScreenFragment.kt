package com.example.tonezone.player

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.tonezone.databinding.FragmentPlayerScreenBinding
import com.example.tonezone.network.Track

class PlayerScreenFragment : Fragment() {

    private lateinit var binding: FragmentPlayerScreenBinding
    private lateinit var viewModel: PlayerScreenViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPlayerScreenBinding.inflate(inflater)
        val application = requireNotNull(activity).application
        val factory = PlayerScreenViewModelFactory(application)
        viewModel = ViewModelProvider(requireActivity(),factory).get(PlayerScreenViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = activity

        setupImageCurrentTrack()
        observeCurrentTrack()
        observePlayerState()
        observeProgress()
        binding.executePendingBindings()

        return binding.root
    }

    private fun observeShuffleIcon(){
        viewModel.isShuffling.observe(viewLifecycleOwner){}
    }

    private fun observeProgress(){
        viewModel.progress.observe(viewLifecycleOwner){}
    }

    private fun observePlayerState(){
        viewModel.playerState.observe(viewLifecycleOwner){}
    }

    private fun observeCurrentTrack(){
        var currentTrack = Track()
        viewModel.currentTrack.observe(viewLifecycleOwner){
            if(it!= Track() && currentTrack!=it){
                viewModel.initSeekBar().start()
                currentTrack = it
            }
        }
    }

    private fun setupImageCurrentTrack(){
        viewModel.uriTrackResponse.observe(viewLifecycleOwner){
            viewModel.token.observe(viewLifecycleOwner){
                viewModel.getImageTrack()
            }
        }
    }

    override fun onDestroy() {
        viewModel.disconnect()
        super.onDestroy()
    }
    //    WTF should I do with this screen????

}