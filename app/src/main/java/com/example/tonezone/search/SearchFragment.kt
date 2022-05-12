package com.example.tonezone.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.tonezone.MainViewModel
import com.example.tonezone.adapter.GenreAdapter
import com.example.tonezone.databinding.FragmentSearchBinding
import com.example.tonezone.network.PlaylistInfo


class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private val mainViewModel : MainViewModel by activityViewModels()
    private val viewModel: SearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{

        binding =  FragmentSearchBinding.inflate(inflater)

        binding.viewModel= viewModel
        binding.lifecycleOwner = this

        setupAdapterGenres()
        setupNavigateSearchForItem()

        return binding.root
    }

    private fun setupNavigateSearchForItem(){
        binding.searchBar.setOnClickListener {
            findNavController().navigate(
                SearchFragmentDirections.actionSearchFragmentToSearchForItemFragment()
            )
        }
    }

    private fun setupAdapterGenres(){
        val adapter = GenreAdapter(GenreAdapter.OnClickListener {
            findNavController().navigate(SearchFragmentDirections
                .actionSearchFragmentToPlaylistsFragment(
                    PlaylistInfo(it.id!!, it.name!!,"","","genre")))
        })
        binding.genre.adapter = adapter
        viewModel.categories.observe(viewLifecycleOwner){
            if(it!=null)
                adapter.submitList(it)
        }
    }

}