package com.example.tonezone.utils

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.tonezone.databinding.ModalBottomSheetContentBinding
import com.example.tonezone.detailplaylist.PlaylistDetailsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ModalBottomSheet(private val objectRequest: ObjectRequest) : BottomSheetDialogFragment() {

    private lateinit var binding: ModalBottomSheetContentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = ModalBottomSheetContentBinding.inflate(inflater)
        setupBottomSheetItems(objectRequest)

        return binding.root

    }

    private fun setupBottomSheetItems(objectRequest: ObjectRequest){
        when(objectRequest){
            ObjectRequest.ARTIST -> createBottomSheetItem(listOf())
            ObjectRequest.PLAYLIST -> createBottomSheetItem(listOf("Like","Save","Dick"))
            ObjectRequest.TRACK -> createBottomSheetItem(listOf())
            ObjectRequest.YOUR_PLAYLIST -> createBottomSheetItem(listOf())
        }
    }

    private fun createBottomSheetItem(itemNames: List<String>){

        itemNames.forEachIndexed { index, s ->
            val textView = TextView(context)
            textView.text = s
            var layoutParams = ViewGroup.MarginLayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(convertDPtoInt(8F),convertDPtoInt(8F),0,8)
            binding.listOption.addView(textView,index)

        }
    }

    private fun convertDPtoInt(value: Float): Int = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value,
            requireContext().resources.displayMetrics
        ).toInt()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val detailPlaylistViewModel = ViewModelProvider(requireActivity()).get(PlaylistDetailsViewModel::class.java)

    }

    companion object{
        const val TAG = "ModalBottomSheet"
    }
}