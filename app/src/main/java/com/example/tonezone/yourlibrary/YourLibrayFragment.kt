package com.example.tonezone.yourlibrary

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.tonezone.MainViewModel
import com.example.tonezone.MainViewModelFactory
import com.example.tonezone.R
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.databinding.FragmentYourLibraryBinding

class YourLibraryFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels{
        MainViewModelFactory(requireActivity())
    }
    private val viewModel: YourLibraryViewModel by viewModels {
        YourLibraryViewModelFactory(mainViewModel.token)
    }

    private lateinit var binding: FragmentYourLibraryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentYourLibraryBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.yourLibraryList.adapter = LibraryAdapter(LibraryAdapter.OnClickListener {
            viewModel.displayPlaylistDetails(it)
        })

        setupYourLibraryAdapter(binding)
        observeNavigateToPlaylistDetails()
        setupMenuAppbarOnClick(binding)
        bindChipGroup(binding)
//        setupSearchBar(binding)
//        setupSortOption()

        return binding.root
    }



    //    private fun setupFilterType() {
//        viewModel.type.observe(viewLifecycleOwner) {
//            when (it) {
//                TypeItemLibrary.All -> adapter.filterType("all")
//                TypeItemLibrary.Playlist -> adapter.filterType("playlist")
//                TypeItemLibrary.Artist -> adapter.filterType("artist")
//                else -> throw IllegalArgumentException("unknown value")
//            }
//        }
//    }

    private fun setupSortOption() {
//        viewModel.sortOption.observe(viewLifecycleOwner){
//            when(it){
//                SortOption.Alphabetical -> adapter.sortByAlphabetical()
//                SortOption.Creator -> adapter.sortByCreator()
//                else -> throw IllegalArgumentException("Unknown value")
//            }
//        }
    }

    @SuppressLint("ResourceAsColor")
    private fun bindChipGroup(binding: FragmentYourLibraryBinding) {
        binding.chipGroup.filterTypeChipGroup.isSingleSelection = true

        binding.chipGroup.filterTypeChipGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.all_type -> viewModel.filterType(TypeItemLibrary.All)
                R.id.playlist_type -> viewModel.filterType(TypeItemLibrary.Playlist)
                R.id.artist_type -> viewModel.filterType(TypeItemLibrary.Artist)
            }
        }
    }

    private fun setupSearchBar(binding: FragmentYourLibraryBinding) {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(query: CharSequence?, p1: Int, p2: Int, p3: Int) {
                (binding.yourLibraryList.adapter as LibraryAdapter).filterQuery(query.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun setupMenuAppbarOnClick(binding: FragmentYourLibraryBinding) {
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.search -> {
                    binding.toolbar.menu.setGroupVisible(R.id.menu_appbar_yourlibrary, false)
                    binding.searchBar.visibility = View.VISIBLE
                    binding.searchBar.requestFocus()
                    binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_new_24)
                    binding.arrangementFrame.visibility = View.GONE
                    binding.toolbar.setNavigationOnClickListener {
                        binding.searchBar.visibility = View.GONE
                        binding.toolbar.menu.setGroupVisible(R.id.menu_appbar_yourlibrary, true)
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


    private fun setupYourLibraryAdapter(binding: FragmentYourLibraryBinding) {
    }

    private fun observeNavigateToPlaylistDetails() {
        viewModel.navigateToDetailPlaylist.observe(viewLifecycleOwner) {
            if (it!=null){
//                val navOptions = NavOptions.Builder()
//                    .setRestoreState(true)
//                    .setPopUpTo(R.id.playlistDetailsFragment,false)
//                    .build()


                this.findNavController()
                    .navigate(YourLibraryFragmentDirections
                        .actionYourLibraryFragment2ToPlaylistDetailsFragment(it))


                viewModel.displayPlaylistDetailsComplete()
            }
        }
    }

}