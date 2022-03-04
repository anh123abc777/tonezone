package com.example.tonezone.search

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
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

        return binding.root
    }

    private fun setupAdapterGenres(){
        val adapter = GenreAdapter()
        binding.genre.adapter = adapter
        viewModel.topics.observe(viewLifecycleOwner){
            if(it!=null)
                adapter.submitList(it.genres)
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