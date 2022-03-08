package com.example.tonezone.player

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.tonezone.databinding.FragmentPlayerScreenBinding

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

        return binding.root
    }

    override fun onStop() {
        super.onStop()
        viewModel.onPause()
    }

    override fun onDestroy() {
        viewModel.onPause()
        viewModel.disconnect()
        super.onDestroy()
    }
    //    WTF should I do with this screen????

}