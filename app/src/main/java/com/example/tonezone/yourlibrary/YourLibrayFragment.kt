package com.example.tonezone.yourlibrary

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.ResultReceiver
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.tonezone.MainViewModel
import com.example.tonezone.MainViewModelFactory
import com.example.tonezone.R
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.databinding.FragmentYourLibraryBinding
import com.example.tonezone.network.Albums
import com.example.tonezone.network.Artists
import com.example.tonezone.network.Playlists
import com.example.tonezone.utils.ModalBottomSheet
import com.example.tonezone.utils.ModalBottomSheetViewModel
import com.example.tonezone.utils.ObjectRequest
import com.example.tonezone.utils.convertSignalToText
import com.google.android.material.dialog.MaterialAlertDialogBuilder


@Suppress("DEPRECATION")
class YourLibraryFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels{
        MainViewModelFactory(requireActivity())
    }
    private val viewModel: YourLibraryViewModel by activityViewModels {
        YourLibraryViewModelFactory( mainViewModel.firebaseUser.value!!)
    }

    private val modalBottomSheetViewModel : ModalBottomSheetViewModel by activityViewModels()

    private val ARTIST = 2
    private val PLAYLIST = 1

    private lateinit var adapter : LibraryAdapter

    private lateinit var binding: FragmentYourLibraryBinding

    private lateinit var modalBottomSheet: ModalBottomSheet

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentYourLibraryBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

//        viewModel.getDataUserPlaylists()

        setupYourLibraryAdapter()
        observeNavigateToPlaylistDetails()
        setupMenuAppbarOnClick()
        bindChipGroup()
        setupSearchBar()
        setupSortOption()
        setupFilterType()
        handleRequestToCreatePlaylist()
        setupBottomSheet()
        handleSignalFromBottomSheet()

        return binding.root
    }

        private fun setupFilterType() {
        viewModel.type.observe(viewLifecycleOwner) {
            when (it) {
                TypeItemLibrary.All -> adapter.filterType("all")
                TypeItemLibrary.Playlist -> adapter.filterType("playlist")
                TypeItemLibrary.Artist -> adapter.filterType("artist")
                TypeItemLibrary.Album -> adapter.filterType("album")
                else -> throw IllegalArgumentException("unknown value")
            }
        }
    }

    private fun setupSortOption() {

        binding.displayOption.viewModel = viewModel

        viewModel.dataItems.observe(viewLifecycleOwner) { value ->
            val dataItems = viewModel.dataItems.value!!.sortedBy { it.name }
            adapter.submitListDataItems(dataItems.sortedByDescending { it.pin })

//            viewModel.yourPlaylistNum= dataItems!!.filter { it.typeName=="playlist" }
//                .map { (it as LibraryAdapter.DataItem.PlaylistItem).playlist }
//                .filter { it.owner!!.id==viewModel.firebaseUser.id }.size+1

            viewModel.sortOption.observe(viewLifecycleOwner) {
                if (value != null)
                    when (it) {
                        SortOption.Alphabetical -> {
                            val dataItems = viewModel.dataItems.value!!.sortedBy { it.name }
                            adapter.submitListDataItems(dataItems.sortedByDescending { it.pin })
                        }
                        SortOption.Creator -> {
                            val dataItems = viewModel.dataItems.value!!.sortedBy { it.type }
                            adapter.submitListDataItems(dataItems.sortedByDescending { it.pin })
                        }
                        else -> {
                            adapter.submitListDataItems(viewModel.dataItems.value!!.sortedBy { it.pin })
                        }
                    }
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun bindChipGroup() {
        binding.chipGroup.filterTypeChipGroup.isSingleSelection = true
        binding.chipGroup.allType.isChecked = true
        binding.chipGroup.filterTypeChipGroup.isSelectionRequired = true

        viewModel.dataItems.observe(viewLifecycleOwner) {
            binding.chipGroup.playlistData = Playlists(it.filter { it.typeName == "playlist" }.map { (it as LibraryAdapter.DataItem.PlaylistItem).playlist })
            binding.chipGroup.artistData = Artists(it.filter { it.typeName == "artist" }.map { (it as LibraryAdapter.DataItem.ArtistItem).artist })
            binding.chipGroup.albumData = Albums(it.filter { it.typeName == "album" }.map { (it as LibraryAdapter.DataItem.AlbumItem).album })

        }

        binding.chipGroup.filterTypeChipGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.all_type -> {
                    viewModel.filterType(TypeItemLibrary.All)

                }
                R.id.playlist_type -> {
                    viewModel.filterType(TypeItemLibrary.Playlist)
                }
                R.id.artist_type -> {
                    viewModel.filterType(TypeItemLibrary.Artist)
                }
                R.id.albums_type -> {
                    viewModel.filterType(TypeItemLibrary.Album)
                }
                else -> {
                    viewModel.filterType(TypeItemLibrary.All)
                }
            }
        }
    }

    private fun setupSearchBar() {
        var initSearchBar = 0
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(query: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(query!=null && query.toString()!="") {
                        initSearchBar = 1
                    (binding.yourLibraryList.adapter as LibraryAdapter).filterQuery(query.toString())
                }else
                    if (initSearchBar!=0)
                        (binding.yourLibraryList.adapter as LibraryAdapter).filterQuery(query.toString())

            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun setupMenuAppbarOnClick() {
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.search -> {
                    binding.toolbar.menu.setGroupVisible(R.id.menu_appbar_yourlibrary, false)
                    binding.searchBar.visibility = View.VISIBLE
                    binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_new_24)
                    binding.displayOption.arrangementFrame.visibility = View.GONE

                    val imm = (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as
                            InputMethodManager)
                    binding.searchBar.postDelayed(Runnable {
                        binding.searchBar.requestFocus()
                        imm.showSoftInput(binding.searchBar,0)
                    },100)


                    binding.toolbar.setNavigationOnClickListener {
                        binding.searchBar.visibility = View.GONE
                        binding.toolbar.menu.setGroupVisible(R.id.menu_appbar_yourlibrary, true)
                        binding.toolbar.navigationIcon = null
                        binding.searchBar.clearFocus()
                        binding.searchBar.text?.clear()
                        binding.displayOption.arrangementFrame.visibility = View.VISIBLE
                        hideKeyboardFrom(binding.root.context,binding.root)
                    }
                }

                R.id.add_playlist -> {
                    viewModel.requestToCreatePlaylist()
                }
            }
            true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleRequestToCreatePlaylist(){
        viewModel.isCreatingPlaylist.observe(viewLifecycleOwner){
            if (it){

                val alert = MaterialAlertDialogBuilder(requireContext())
                val input = EditText(context)
                input.inputType = InputType.TYPE_CLASS_TEXT
                input.gravity = Gravity.CENTER
                input.setText("")
                alert.setView(input)

                alert.setPositiveButton("Ok"
                ) { dialog, _ ->
                    viewModel.createPlaylist(input.text.toString())
                    dialog.dismiss()
                }

                alert.setNegativeButton("Cancel"){ dialog, _ ->
                    dialog.cancel()
                }

                alert.show()


                val imm = (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as
                        InputMethodManager)
                input.postDelayed(Runnable {
                    input.requestFocus()
                    imm.showSoftInput(input,0)
                },100)


                viewModel.requestToCreatePlaylistComplete()
            }
        }
    }

    private fun setupYourLibraryAdapter() {

        adapter = LibraryAdapter(LibraryAdapter.OnClickListener { item, idButton ->
            when(idButton){
                null -> viewModel.displayPlaylistDetails(item)
                Int.MIN_VALUE -> {
                    if (item.id!="liked_track")
                        viewModel.showBottomSheet(getObjectRequestBaseOnTypeItem(item), item.id!!)
                }
                else -> Log.i("libraryAdapter","Nothing")
                }
        })

        binding.yourLibraryList.adapter = adapter
    }

    private fun getObjectRequestBaseOnTypeItem(dataItem: LibraryAdapter.DataItem): ObjectRequest
        = when(dataItem.type){
            ARTIST -> ObjectRequest.ARTIST_FROM_LIBRARY
            PLAYLIST -> {
                val playlist = (dataItem as LibraryAdapter.DataItem.PlaylistItem).playlist
                if(playlist.owner!!.id==viewModel.firebaseUser.id)
                    ObjectRequest.OWNER_PLAYLIST_FROM_LIBRARY
                else
                    ObjectRequest.PLAYLIST_FROM_LIBRARY
            }
            else -> ObjectRequest.PLAYLIST_FROM_LIBRARY
        }


    private fun handleSignalFromBottomSheet(){

        modalBottomSheetViewModel.signal.observe(viewLifecycleOwner){
            when(it){
                null -> Log.i("receivedSignal","unknown value")
                else -> {
                    viewModel.receiveSignal(it)
                    Log.i("receivedSignal", convertSignalToText(it))
                }
            }
        }

        viewModel.receivedSignal.observe(viewLifecycleOwner){
            if (it!=null) {
                modalBottomSheet.dismiss()
                viewModel.handleSignal()
                viewModel.handleSignalComplete()
            }
        }
    }

    private fun setupBottomSheet(){
        viewModel.objectShowBottomSheet.observe(viewLifecycleOwner){
            when(it){
                null -> Log.i("setupBottomSheet","objectRequest null")
                else -> {
                    modalBottomSheet = ModalBottomSheet(it.first,null,
                        viewModel.dataItems.value!!.find { item -> item.id!! == it.second }!!.pin)
                    modalBottomSheet.show(
                        requireActivity().supportFragmentManager,
                        ModalBottomSheet.TAG
                    )
                }

            }
        }
    }

    private fun observeNavigateToPlaylistDetails() {
        viewModel.navigateToDetailPlaylist.observe(viewLifecycleOwner) {
            if (it!=null){
                when(it.type) {
                    "playlist","album" -> {

                        if (findNavController().currentDestination?.id == R.id.yourLibraryFragment) {

                            hideKeyboardFrom(binding.root.context,binding.root)

                            findNavController()
                                .navigate(
                                    YourLibraryFragmentDirections
                                        .actionYourLibraryFragmentToPlaylistDetailsFragment(it)
                                )
                        }
                    }
                    "artist" -> {
                        if (findNavController().currentDestination?.id == R.id.yourLibraryFragment)
                            this.findNavController()
                                .navigate(
                                    YourLibraryFragmentDirections
                                        .actionYourLibraryFragmentToArtistDetailsFragment(it)
                                )
                    }

                }
                viewModel.displayPlaylistDetailsComplete()
            }
        }
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.rootView.windowToken, 0)
        view.clearFocus()
    }

}