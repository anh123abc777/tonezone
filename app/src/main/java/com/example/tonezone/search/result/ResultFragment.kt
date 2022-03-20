package com.example.tonezone.search.result

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
import com.example.tonezone.databinding.FragmentResultBinding
import com.example.tonezone.network.PlaylistInfo

class ResultFragment : Fragment() {

    private lateinit var binding: FragmentResultBinding

    private val mainViewModel : MainViewModel by activityViewModels()

    private lateinit var genreName: String

    private val viewModel: ResultViewModel by viewModels {
        ResultViewModelFactory(mainViewModel.token, genreName)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentResultBinding.inflate(inflater,container,false)

        genreName = ResultFragmentArgs.fromBundle(requireArguments()).genreName

        binding.viewModel= viewModel
        binding.lifecycleOwner = this

        createAdapter()
        observeDataSearch()

        return binding.root
    }

    private fun createAdapter(){
        binding.dataSearch.adapter = PlaylistAdapter(PlaylistAdapter.OnClickListener {
            findNavController().navigate(ResultFragmentDirections.actionResultFragmentToPlaylistDetailsFragment(
                PlaylistInfo(
                    it.id,
                    it.name,
                    it.description,
                    it.images?.get(0)!!.url,
                    it.uri,
                    it.type
                )
            ))
        })
    }

    private fun observeDataSearch(){
        viewModel.categoryPlaylists.observe(viewLifecycleOwner){
            if(it!=null){
                val adapter = binding.dataSearch.adapter as PlaylistAdapter
                adapter.submitList(it)
            }
        }
    }
}