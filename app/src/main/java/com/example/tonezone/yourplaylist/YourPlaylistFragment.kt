package com.example.tonezone.yourplaylist

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.tonezone.MainViewModel
import com.example.tonezone.R
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.databinding.FragmentYourPlaylistBinding
import com.example.tonezone.network.FirebaseRepository
import com.example.tonezone.yourlibrary.YourLibraryViewModel
import com.example.tonezone.yourlibrary.YourLibraryViewModelFactory

class YourPlaylistFragment : Fragment() {

    private lateinit var binding: FragmentYourPlaylistBinding

    private val mainViewModel: MainViewModel by activityViewModels()

    private val yourLibraryViewModel: YourLibraryViewModel by viewModels {
        YourLibraryViewModelFactory(mainViewModel.token,mainViewModel.user.value!!)
    }

    private val trackUris : String by lazy {
        YourPlaylistFragmentArgs.fromBundle(requireArguments()).trackID
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentYourPlaylistBinding.inflate(inflater)
        binding.viewModel = yourLibraryViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupYourPlaylistsAdapter()
        submitListYourPlaylists()
        setupMenuAppbarOnClick()
        setupSearchBar()
        handleRequestToCreatePlaylist()

        return binding.root
    }

    private fun setupYourPlaylistsAdapter(){
        val adapter = LibraryAdapter(LibraryAdapter.OnClickListener{ dataItem, _ ->
            addItemToPlaylist(dataItem.id!!,trackUris)
            requireActivity().onBackPressed()
        })
        binding.listYourPlaylist.adapter = adapter
    }

    private fun addItemToPlaylist(playlistID: String, trackIDs: String){
        val firebaseRepo = FirebaseRepository()
        firebaseRepo.addItemToYourPlaylist(playlistID, listOf(trackIDs))
//        Log.i("addItemToYourPlaylist","playlistID: $playlistID \n trackUris: $trackUris")
//        uiScope.launch {
//            try {
//                ToneApi.retrofitService2.addItemsToPlaylist(
//                    "Bearer $token",
//                    playlistID,
//                    trackUris
//                    ).enqueue(object: Callback<String>{
//                    override fun onResponse(call: Call<String>, response: Response<String>) {
//                        Log.i("addItemToYourPlaylist","success ${response.body()}")
//                    }
//
//                    override fun onFailure(call: Call<String>, t: Throwable) {
//                        Log.i("addItemToYourPlaylist","Failure s $t")
//
//                    }
//                })
//            }catch (e: Exception){
//                Log.i("addItemToYourPlaylist","Failure $e")
//            }
//        }
    }

    private fun submitListYourPlaylists(){
        yourLibraryViewModel.userPlaylists.observe(viewLifecycleOwner){ playlists ->
            if(playlists!=null){
                val listYourPlaylists = playlists?.filter { it.owner!!.id == yourLibraryViewModel.user.id }
                val adapter = (binding.listYourPlaylist.adapter as LibraryAdapter)
                adapter.apply {
                    submitYourLibrary(listYourPlaylists, null, null,null)
                    sortByDefault()
                }

                if(listYourPlaylists?.isEmpty() == true){
                    yourLibraryViewModel.requestToCreatePlaylist()
                }
            }
        }
    }

    private fun handleRequestToCreatePlaylist(){
        yourLibraryViewModel.isCreatingPlaylist.observe(viewLifecycleOwner){
            if (it){

                val alert = AlertDialog.Builder(context)
                val input = EditText(context)
                input.inputType = InputType.TYPE_CLASS_TEXT
                input.setBackgroundColor(Color.WHITE)
                input.gravity = Gravity.CENTER

                alert.setView(input)

                alert.setPositiveButton("Skip"
                ) { dialog, _ ->
                    yourLibraryViewModel.createPlaylist(input.text.toString())
                    dialog.dismiss()
                }

                alert.setNegativeButton("Cancel"){ dialog, _ ->
                    dialog.cancel()
                }

                alert.show()

                input.requestFocus()
                yourLibraryViewModel.requestToCreatePlaylistComplete()
            }
        }
    }

    private fun setupMenuAppbarOnClick() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.search_your_playlist -> {
                    binding.toolbar.menu.setGroupVisible(R.id.menu_appbar_yourplaylist, false)
                    binding.searchBar.visibility = View.VISIBLE
                    binding.searchBar.requestFocus()
                    binding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_new_24)
                    binding.toolbar.setNavigationOnClickListener {
                        binding.searchBar.visibility = View.GONE
                        binding.toolbar.menu.setGroupVisible(R.id.menu_appbar_yourplaylist, true)
                        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
                        binding.toolbar.setNavigationOnClickListener {
                            requireActivity().onBackPressed()
                        }
                        binding.searchBar.clearFocus()
                        binding.searchBar.text.clear()
                        (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as
                                InputMethodManager).hideSoftInputFromWindow(binding.root.windowToken,0)

                    }
                }
            }
            true
        }
    }

    private fun setupSearchBar() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(query: CharSequence?, p1: Int, p2: Int, p3: Int) {
                (binding.listYourPlaylist.adapter as LibraryAdapter).filterQuery(query.toString())

            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

}