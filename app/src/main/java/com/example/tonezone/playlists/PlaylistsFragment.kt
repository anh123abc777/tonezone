package com.example.tonezone.playlists

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.tonezone.MainViewModel
import com.example.tonezone.adapter.PlaylistAdapter
import com.example.tonezone.artistdetails.ArtistDetailsViewModel
import com.example.tonezone.artistdetails.ArtistDetailsViewModelFactory
import com.example.tonezone.databinding.FragmentPlaylistsBinding
import com.example.tonezone.network.Owner
import com.example.tonezone.network.Playlist
import com.example.tonezone.network.PlaylistInfo

class PlaylistsFragment : Fragment() {

    private lateinit var binding: FragmentPlaylistsBinding

    private val mainViewModel : MainViewModel by activityViewModels()

    private val playlistInfo: PlaylistInfo by lazy {
       PlaylistsFragmentArgs.fromBundle(requireArguments()).playlistInfo
    }

    private val viewModel: PlaylistsViewModel by viewModels {
        PlaylistsViewModelFactory( playlistInfo)
    }

    private val artistViewModel: ArtistDetailsViewModel by viewModels {
        ArtistDetailsViewModelFactory(playlistInfo,mainViewModel.firebaseAuth.value!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPlaylistsBinding.inflate(inflater,container,false)

        binding.viewModel= viewModel
        binding.lifecycleOwner = this

        createAdapter()
        submitListPlaylist()

        return binding.root
    }

    private fun createAdapter(){
        binding.dataSearch.adapter = PlaylistAdapter(PlaylistAdapter.OnClickListener {
            findNavController().navigate(PlaylistsFragmentDirections.actionPlaylistsFragmentToPlaylistDetailsFragment(
                PlaylistInfo(
                    it.id!!,
                    it.name!!,
                    it.description!!,
                    it.images?.get(0)!!.url,
                    it.type!!
                )
            ))
        })
    }

    private fun submitListPlaylist(){
        val adapter = binding.dataSearch.adapter as PlaylistAdapter
        viewModel.categoryPlaylists.observe(viewLifecycleOwner) { playlists ->
            if (playlists != null) {
                adapter.submitList(playlists)
            }
        }

        if(playlistInfo.type=="artist"){
            artistViewModel.artistAlbums.observe(viewLifecycleOwner){ albums ->
                if (albums!=null){
                    val list = albums.map { Playlist(
                        id = it.id!!,
                        description = it.album_group.toString(),
                        images = it.images?: listOf(),
                        name = it.name!!,
                        owner = Owner(),
                        type = it.type!!,
                        public = true
                    ) }

                    adapter.submitList(list)
                }
            }
        }

    }
}