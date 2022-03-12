package com.example.tonezone.yourlibrary

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.tonezone.R
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.databinding.FragmentYourLibraryBinding
import com.example.tonezone.network.PlaylistInfo

class YourLibraryFragment : Fragment() {

    private lateinit var binding : FragmentYourLibraryBinding
    private lateinit var viewModel:  YourLibraryViewModel
    private lateinit var adapter: LibraryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = FragmentYourLibraryBinding.inflate(inflater)
        val application = requireNotNull(activity).application
        val factory = YourLibraryViewModelFactory(application)
        viewModel = ViewModelProvider(this,factory).get(YourLibraryViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        observeToken()
        observeUserPlaylists()
        setupYourLibraryAdapter()
        setupMenuAppbarOnClick()
        bindChipGroup()
        setupSearchBar()
        setupSortOption()
        setupFilterType()
        binding.executePendingBindings()

        return binding.root
    }

    private fun setupFilterType(){
        viewModel.type.observe(viewLifecycleOwner){
            when(it){
                TypeItemLibrary.All -> adapter.filterType("all")
                TypeItemLibrary.Playlist -> adapter.filterType("playlist")
                TypeItemLibrary.Artist -> adapter.filterType("artist")
                else -> throw IllegalArgumentException("unknown value")
            }
        }
    }

    private fun setupSortOption(){
        viewModel.sortOption.observe(viewLifecycleOwner){
            when(it){
                SortOption.Alphabetical -> adapter.sortByAlphabetical()
                SortOption.Creator -> adapter.sortByCreator()
                else -> throw IllegalArgumentException("Unknown value")
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun bindChipGroup(){
        binding.chipGroup.viewModel = viewModel
        binding.chipGroup.filterTypeChipGroup.isSingleSelection = true
    }

    private fun setupSearchBar() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(query: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter.filterQuery(query.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun setupMenuAppbarOnClick(){
        binding.toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.search -> {
                    binding.toolbar.menu.setGroupVisible(R.id.menu_appbar_yourlibrary, false)
                    binding.searchBar.visibility = View.VISIBLE
                    binding.searchBar.requestFocus()
                    binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_new_24)
                    binding.arrangementFrame.visibility = View.GONE
                    binding.toolbar.setNavigationOnClickListener {
                        binding.searchBar.visibility = View.GONE
                        binding.toolbar.menu.setGroupVisible(R.id.menu_appbar_yourlibrary,true)
                        binding.toolbar.navigationIcon = null
                        binding.searchBar.clearFocus()
                        binding.searchBar.text.clear()
                        binding.arrangementFrame.visibility = View.VISIBLE
                    }
                }
            }
            true
        }
    }

    private fun setupYourLibraryAdapter(){
         adapter = LibraryAdapter(LibraryAdapter.OnClickListener {

            when(it.type){
                2 -> {
                    it as LibraryAdapter.DataItem.ArtistItem
                    Toast.makeText(context,it.artist.name,Toast.LENGTH_SHORT).show()
                }
                else -> {
                   it as LibraryAdapter.DataItem.PlaylistItem
                    findNavController().navigate(YourLibraryFragmentDirections
                        .actionYourLibraryFragmentToDetailPlaylistFragment(
                            PlaylistInfo( it.playlist.id,
                                it.playlist.name,
                                it.playlist.description,
                                it.playlist.images?.get(0)!!.url,
                                it.playlist.uri)))
                }
            }

        })
        binding.yourLibraryList.adapter = adapter

    }

    private fun observeToken(){
        viewModel.token.observe(viewLifecycleOwner){
            if(it!=null){
                viewModel.getDataUserPlaylists()
                viewModel.getDataFollowedArtists()
            }
        }
    }

    private fun observeUserPlaylists(){
        viewModel.userPlaylists.observe(viewLifecycleOwner){
            observeFollowedArtists()
            Log.i("observe","2")
        }
    }

    private fun observeFollowedArtists(){
        viewModel.followedArtists.observe(viewLifecycleOwner){}
    }

    override fun onStop() {
        super.onStop()
        Log.i("observe","top")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i("observe","destroy")
    }
}