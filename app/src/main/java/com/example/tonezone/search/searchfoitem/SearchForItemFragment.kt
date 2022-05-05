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
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.tonezone.MainViewModel
import com.example.tonezone.R
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.databinding.FragmentSearchForItemBinding
import com.example.tonezone.network.*
import com.example.tonezone.player.PlayerScreenViewModel
import com.example.tonezone.utils.Type
import com.example.tonezone.yourlibrary.TypeItemLibrary
import java.lang.Exception

class SearchForItemFragment : Fragment() {

    private lateinit var binding: FragmentSearchForItemBinding

    private val mainViewModel: MainViewModel by activityViewModels()

    private val viewModel: SearchForItemViewModel by viewModels {
        SearchForItemViewModelFactory(mainViewModel.token)
    }

    private val playerViewModel: PlayerScreenViewModel by activityViewModels()

    private lateinit var adapter: LibraryAdapter

    private val playlistID : String? by lazy {
        try {
            SearchForItemFragmentArgs.fromBundle(requireArguments()).playlistID
        }catch (e: Exception){
            null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchForItemBinding.inflate(inflater,container,false)

        binding.viewModel= viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupSearchbar()
        observeSearchedItems()
        setupFilterType()
        handlePlayingTrack()

        return binding.root
    }


    private fun handlePlayingTrack(){
        if (playerViewModel.playerState.value==PlayerScreenViewModel.PlayerState.NONE) {
            playerViewModel.currentTrack.observe(requireActivity()) {
                if (it != Track()) {
                    playerViewModel.initSeekBar()
                    playerViewModel.onPlay()
                    playerViewModel.initPrimaryColor()
                }
            }
        }
    }

    private fun setupSearchbar(){
        binding.searchBar.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                if (playlistID!=null)
                    viewModel.searchInFirebase(p0,Type.TRACK)
                else
                    viewModel.searchInFirebase(p0)
            }
        })

        binding.searchBar.setOnFocusChangeListener { _, isFocusing ->
            if (isFocusing){
                binding.backSearchButton.setImageResource(R.drawable.ic_clear_text)
                binding.backSearchButton.setOnClickListener {
                    binding.searchBar.text?.clear()
                    binding.searchBar.clearFocus()
                    (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as
                            InputMethodManager).hideSoftInputFromWindow(binding.root.windowToken,0)
                }
            }
            else{
                binding.backSearchButton.setImageResource(R.drawable.ic_arrow_back)
                binding.backSearchButton.setOnClickListener {
                    requireActivity().onBackPressed()
                }
            }
        }


    }

    private fun bindChipGroup(){
        binding.chipGroup.filterTypeChipGroup.isSingleSelection = true
//        binding.chipGroup.playlistData = viewModel.searchedItems.value?. ?: Playlists()
//        binding.chipGroup.artistData = viewModel.searchedItems.value?.artists ?: Artists()
//        binding.chipGroup.trackData = viewModel.searchedItems.value?.tracks ?: Tracks()
//        binding.chipGroup.albumData = viewModel.searchedItems.value?.albums ?: Albums(listOf())
        viewModel.searchedItems.observe(viewLifecycleOwner) {
            if (it != null && playlistID==null) {
                binding.chipGroup.playlistData = Playlists(it.filter { dataItems ->  dataItems.typeName == "playlist" }
                    .map {item -> (item as LibraryAdapter.DataItem.PlaylistItem).playlist })
                binding.chipGroup.artistData = Artists(it.filter {dataItems ->  dataItems.typeName == "artist" }
                    .map {item -> (item as LibraryAdapter.DataItem.ArtistItem).artist })
                binding.chipGroup.albumData = Albums(it.filter {dataItems ->  dataItems.typeName == "album" }
                    .map {item -> (item as LibraryAdapter.DataItem.AlbumItem).album })
                binding.chipGroup.trackData = Tracks(it.filter {dataItems ->  dataItems.typeName == "Track" }
                    .map {item -> (item as LibraryAdapter.DataItem.TrackItem).track })
            }
        }

        binding.chipGroup.filterTypeChipGroup.setOnCheckedChangeListener { _, checkedId ->

            when(checkedId){
                R.id.all_type -> viewModel.filterType(TypeItemLibrary.All)
                R.id.playlist_type -> viewModel.filterType(TypeItemLibrary.Playlist)
                R.id.artist_type -> viewModel.filterType(TypeItemLibrary.Artist)
                R.id.track_type -> viewModel.filterType(TypeItemLibrary.Track)
                R.id.albums_type -> viewModel.filterType(TypeItemLibrary.Album)
                else -> viewModel.filterType(TypeItemLibrary.All)
//                R.id.all_type -> viewModel.searchInFirebase(binding.searchBar.text)
//                R.id.playlist_type -> viewModel.searchInFirebase(binding.searchBar.text,Type.PLAYLIST)
//                R.id.artist_type -> viewModel.searchInFirebase(binding.searchBar.text,Type.ARTIST)
//                R.id.track_type -> viewModel.searchInFirebase(binding.searchBar.text,Type.TRACK)
//                R.id.albums_type -> viewModel.searchInFirebase(binding.searchBar.text,Type.ALBUM)
//                else -> viewModel.searchInFirebase(binding.searchBar.text)

            }
        }
    }

    private fun observeSearchedItems(){
        adapter = LibraryAdapter(LibraryAdapter.OnClickListener { item, buttonId ->
            when(item.typeName!!.lowercase()){
                "artist" -> {
                    findNavController().navigate(
                        SearchForItemFragmentDirections
                            .actionSearchForItemFragmentToArtistDetailsFragment(
                                convertDataItemToObject(item)
                            ))
                }

                "album","playlist" -> {
                    findNavController().navigate(
                        SearchForItemFragmentDirections
                            .actionSearchForItemFragmentToPlaylistDetailsFragment(
                                convertDataItemToObject(item)
                            )
                    )
                }

                "track" -> {
                    val track = (item as LibraryAdapter.DataItem.TrackItem).track
                    if (playlistID!=null ){
                        if (buttonId == R.id.more_option_with_track){
                            FirebaseRepository().addItemToYourPlaylist(playlistID!!, listOf(track.id))
                            viewModel.searchedItems.value?.let { adapter.notifyItemRemoved(it.indexOf(item)) }
                            viewModel.removeItem(item)
                        }else{
                            playerViewModel.onInit(0, listOf(track) )
                        }

                    }else{
                        val tracks = viewModel.searchedItems.value?.filter { it.typeName=="Track" }
                            ?.map { (it as LibraryAdapter.DataItem.TrackItem).track }
                        if (tracks!=null && tracks.isNotEmpty())
                            playerViewModel.onInit(tracks.indexOf(item.track),tracks )
                    }
                }
            }
        }, playlistID!=null)
        binding.searchedItems.adapter = adapter
        viewModel.searchedItems.observe(viewLifecycleOwner){
            if (it!=null)
                bindChipGroup()
        }
    }

    private fun convertDataItemToObject(item: LibraryAdapter.DataItem)=
        PlaylistInfo(
            item.id!!,
            item.name!!,
            item.description!!,
            item.image,
            item.typeName!!
        )


    private fun setupFilterType(){
        viewModel.type.observe(viewLifecycleOwner){
            when(it){
                TypeItemLibrary.All -> adapter.filterType("all")
                TypeItemLibrary.Playlist -> adapter.filterType("playlist")
                TypeItemLibrary.Artist -> adapter.filterType("artist")
                TypeItemLibrary.Album -> adapter.filterType("album")
                else -> adapter.filterType("Track")
            }
        }
    }
}