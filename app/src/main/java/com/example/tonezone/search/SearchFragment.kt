package com.example.tonezone.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.tonezone.adapter.GenreAdapter
import com.example.tonezone.databinding.FragmentSearchBinding


class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var viewModel: SearchViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{

        binding =  FragmentSearchBinding.inflate(inflater)
        val application = requireNotNull(activity).application
        val factory = SearchViewModelFactory(application)
        viewModel = ViewModelProvider(this,factory).get(SearchViewModel::class.java)

        binding.viewModel= viewModel
        binding.lifecycleOwner = this

        observeDataToken()
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
                .actionSearchFragmentToResultFragment(it.id))
        })
        binding.genre.adapter = adapter
        viewModel.categories.observe(viewLifecycleOwner){
            if(it!=null)
                adapter.submitList(it)
        }
    }

    private fun observeDataToken(){
        viewModel.token.observe(viewLifecycleOwner){
            if(it!=null) {
                viewModel.getGenres()
            }
        }
    }
}