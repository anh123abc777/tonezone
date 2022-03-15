package com.example.tonezone.search.result

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.tonezone.adapter.PlaylistAdapter
import com.example.tonezone.databinding.FragmentResultBinding
import com.example.tonezone.network.PlaylistInfo

class ResultFragment : Fragment() {

    private lateinit var binding: FragmentResultBinding
    private lateinit var viewModel: ResultViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentResultBinding.inflate(inflater,container,false)
        val application = requireNotNull(activity).application
        val genreName = ResultFragmentArgs.fromBundle(requireArguments()).genreName
        val factory = ResultViewModelFactory(application,genreName)
        viewModel = ViewModelProvider(this,factory).get(ResultViewModel::class.java)

        binding.viewModel= viewModel
        binding.lifecycleOwner = this

        createAdapter()
        observeDataToken()
        observeDataSearch()

        return binding.root
    }

    private fun createAdapter(){
        binding.dataSearch.adapter = PlaylistAdapter(PlaylistAdapter.OnClickListener {
            findNavController().navigate(ResultFragmentDirections.actionResultFragmentToDetailPlaylistFragment(
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

    private fun observeDataToken(){
        viewModel.token.observe(viewLifecycleOwner){
            if(it!=null) {
                viewModel.getSearchResultData()
            }
        }
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