package com.example.tonezone.player

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.tonezone.MainViewModel
import com.example.tonezone.databinding.FragmentPlayerScreenBinding
import com.example.tonezone.network.Track

class PlayerScreenFragment : Fragment() {

    private lateinit var binding: FragmentPlayerScreenBinding
    private val mainViewModel : MainViewModel by activityViewModels()
    private val viewModel: PlayerScreenViewModel by activityViewModels{
        PlayerScreenViewModelFactory(requireNotNull(activity).application,mainViewModel.firebaseAuth.value)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPlayerScreenBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = activity

        handlePlayingTrack()
        handleShowCurrentPlaylist()
        viewModel.setupPropertiesView()

        return binding.root
    }

    private fun handlePlayingTrack(){
        if (viewModel.playerState.value==PlayerScreenViewModel.PlayerState.NONE) {
            viewModel.currentTrack.observe(requireActivity()) {
                if (it != Track()) {
                    viewModel.initSeekBar()
                    viewModel.onPlay()
                    viewModel.initPrimaryColor()
                    viewModel.checkIsLikeTrack()
                }
            }
        }
    }

    private fun handleShowCurrentPlaylist(){
        viewModel.isShowingCurrentPlaylist.observe(requireActivity()){
            if (it==true){
                findNavController().navigate(PlayerScreenFragmentDirections.actionPlayerScreenFragmentToCurrentPlaylistFragment())
                viewModel.showCurrentPlaylistComplete()
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
        super.onDestroy()
    }
    //    WTF should I do with this screen????

}