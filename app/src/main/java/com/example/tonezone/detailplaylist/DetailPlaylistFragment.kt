package com.example.tonezone.detailplaylist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.tonezone.adapter.OnClickListener
import com.example.tonezone.adapter.TrackAdapter
import com.example.tonezone.databinding.FragmentDetailPlaylistBinding

class DetailPlaylistFragment : Fragment() {

    private lateinit var binding: FragmentDetailPlaylistBinding
    private lateinit var viewModel: DetailPlaylistViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentDetailPlaylistBinding.inflate(inflater)
        val playlistInfo = DetailPlaylistFragmentArgs.fromBundle(requireArguments()).playlistInfo
        val application = requireNotNull(activity).application
        val factory = DetailPlaylistViewModelFactory(application,playlistInfo)
        viewModel = ViewModelProvider(this,factory).get(DetailPlaylistViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        val adapter = TrackAdapter(OnClickListener {  })
        binding.playlist.adapter = adapter


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createAdapterPlaylist()
    }

    private fun createAdapterPlaylist(){

        viewModel.token.observe(viewLifecycleOwner){
            if (it!=null)
               viewModel.getDataPlaylistItems()
        }


        viewModel.playlistItems.observe(viewLifecycleOwner){}
    }

}