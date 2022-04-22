package com.example.tonezone.utils

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.tonezone.adapter.TrackAdapter
import com.example.tonezone.databinding.ItemTrackManagerBinding
import com.example.tonezone.network.Track
import com.example.tonezone.player.PlayerScreenViewModel
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class CustomItemTouchHelper(private val playerViewModel: PlayerScreenViewModel): ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {

        val viewHolderPos = viewHolder.bindingAdapterPosition
        val currentTrackPos = playerViewModel.currentPlaylist.value!!.indexOf(playerViewModel.currentTrack.value!!)

//         if (viewHolderPos == playerViewModel.currentTrackPos ||
//            viewHolderPos == playerViewModel.currentTrackPos+1){
//            return 0
//        }
        val trackViewHolder = (viewHolder as TrackAdapter.ViewHolder)

        if(trackViewHolder.binding.drag==1 && viewHolderPos != currentTrackPos && trackViewHolder.binding.buttonChoose.visibility!= View.VISIBLE ) {
            val dragFlags = ItemTouchHelper.UP.or(ItemTouchHelper.DOWN)
            val swipeFlags = 0
            return makeMovementFlags(dragFlags, swipeFlags)
        }else{
           return  0
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val startPos = viewHolder.bindingAdapterPosition
        val endPos = target.bindingAdapterPosition
        val currentTrackPos = playerViewModel.currentPlaylist.value?.indexOf(playerViewModel.currentTrack.value)!!

//            when{
//                startPos<playerViewModel.currentTrackPos && endPos<playerViewModel.currentTrackPos -> {
//                }
//
//                startPos>playerViewModel.currentTrackPos && endPos>playerViewModel.currentTrackPos -> {
//                    Collections.swap(playerViewModel.currentPlaylist.value, startPos-1, endPos-1)
//                }
//
//                playerViewModel.currentTrackPos in (endPos + 1) until startPos -> {
//                    Collections.swap(playerViewModel.currentPlaylist.value,startPos-1,endPos)
//                }
//
//                playerViewModel.currentTrackPos in (startPos + 1) until endPos -> {
//                    Collections.swap(playerViewModel.currentPlaylist.value,startPos,endPos-1)
//                }
//            }
        playerViewModel.swapItem(startPos, endPos)
        recyclerView.adapter?.notifyItemMoved(startPos, endPos)
        return false
    }


    private fun keepScreenWhenSwap(startPosition: Int,endPosition: Int,recyclerView: RecyclerView){
        if (endPosition < 6)
            recyclerView.scrollToPosition(0)
        else
            recyclerView.scrollToPosition(startPosition)
    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        (viewHolder as TrackAdapter.ViewHolder).binding.drag = 0
        viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT)
        super.clearView(recyclerView, viewHolder)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }


    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        viewHolder?.let {
            val color = (viewHolder as TrackAdapter.ViewHolder).binding.darkColor!!
                viewHolder?.itemView?.setBackgroundColor(color)
        }
    //                (viewHolder!!.itemView.background as GradientDrawable).setStroke(3, Color.BLACK)
//                    viewHolder!!.itemView.tag = "isSelected"

    }
}

