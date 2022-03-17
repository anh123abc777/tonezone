package com.example.tonezone.search.searchfoitem


import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import com.example.tonezone.R
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.databinding.FragmentSearchForItemBinding
import com.example.tonezone.network.Artists
import com.example.tonezone.network.Playlists
import com.example.tonezone.network.Tracks
import com.example.tonezone.yourlibrary.TypeItemLibrary

class SearchForItemFragment : Fragment() {

    private lateinit var binding: FragmentSearchForItemBinding
    private lateinit var viewModel: SearchForItemViewModel
    private lateinit var adapter: LibraryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchForItemBinding.inflate(inflater,container,false)
        val application = requireNotNull(activity).application
        val factory = SearchForItemViewModelFactory(application)
        viewModel = ViewModelProvider(this,factory).get(SearchForItemViewModel::class.java)

        binding.viewModel= viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        observeDataToken()
        observeSearchedItems()
        setupFilterType()

        return binding.root
    }
//
//    @SuppressLint("Recycle")
//    private fun temp(){
//        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X,4f)
//        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y,4f)
//
//        val animator
//    }

    private fun setupSearchbar(){
        binding.searchBar.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                viewModel.search(p0.toString())
            }
        })

        binding.backSearchButton.setOnClickListener {
            binding.searchBar.text?.clear()
            binding.searchBar.clearFocus()
            (activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as
                    InputMethodManager).hideSoftInputFromWindow(binding.root.windowToken,0)
        }
    }

    private fun bindChipGroup(){
        binding.chipGroup.filterTypeChipGroup.isSingleSelection = true
        binding.chipGroup.playlistData = viewModel.searchedItems.value?.playlists ?: Playlists()
        binding.chipGroup.artistData = viewModel.searchedItems.value?.artists ?: Artists()
        binding.chipGroup.trackData = viewModel.searchedItems.value?.tracks ?: Tracks()

        binding.chipGroup.filterTypeChipGroup.setOnCheckedChangeListener { _, checkedId ->

            when(checkedId){
                R.id.all_type -> viewModel.filterType(TypeItemLibrary.All)
                R.id.playlist_type -> viewModel.filterType(TypeItemLibrary.Playlist)
                R.id.artist_type -> viewModel.filterType(TypeItemLibrary.Artist)
                R.id.track_type -> viewModel.filterType(TypeItemLibrary.Track)
            }
        }
    }

    private fun observeDataToken(){
        viewModel.token.observe(viewLifecycleOwner){
            setupSearchbar()
        }
    }

    private fun observeSearchedItems(){
        adapter = LibraryAdapter(LibraryAdapter.OnClickListener {})
        binding.searchedItems.adapter = adapter
        viewModel.searchedItems.observe(viewLifecycleOwner){
            if (it!=null)
                bindChipGroup()
        }
    }

    private fun setupFilterType(){
        viewModel.type.observe(viewLifecycleOwner){
            when(it){
                TypeItemLibrary.All -> adapter.filterType("all")
                TypeItemLibrary.Playlist -> adapter.filterType("playlist")
                TypeItemLibrary.Artist -> adapter.filterType("artist")
                else -> adapter.filterType("Track")
            }
        }
    }
}