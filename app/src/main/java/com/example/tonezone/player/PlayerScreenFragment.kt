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

        handlePlayingTrack()

        return binding.root
    }

    private fun handlePlayingTrack(){
        var currentTrack = Track()
        viewModel.currentTrack.observe(viewLifecycleOwner){
            if(it!= Track() && currentTrack!=it){
                viewModel.initSeekBar()
                currentTrack = it
                viewModel.onPlay()
            }
        }
    }


//    private fun translater() {
//        val animator = ObjectAnimator.ofFloat(binding.frameThumbnail, View.TRANSLATION_X,-800f)
//        animator.repeatCount = 0
//        animator.repeatMode = ObjectAnimator.RESTART
//        animator.start()
//    }


    override fun onDestroy() {
        viewModel.disconnect()
        super.onDestroy()
    }
    //    WTF should I do with this screen????

}