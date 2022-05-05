package com.example.tonezone.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.*
import androidx.core.view.allViews
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tonezone.databinding.ItemTrackManagerBinding
import com.example.tonezone.network.Track
import kotlinx.coroutines.*

class TrackAdapter(private val clickListener: OnClickListener) : ListAdapter<TrackAdapter.DataItem, TrackAdapter.ViewHolder>(DiffCallBack){

    private var textColor = 0
    private var darkColor = 0
    private var backgroundChoiceTrack = GradientDrawable()
    private var isShowCheckboxes = false

    fun setBackgroundChoiceTrack(drawable: Drawable){
        backgroundChoiceTrack = drawable as GradientDrawable
        notifyDataSetChanged()
    }

    fun setColor(color: Int){
        textColor = color
        notifyDataSetChanged()
    }

    fun setDarkColor(color: Int){
        darkColor = color
        notifyDataSetChanged()
    }

    fun showCheckboxes(){
        isShowCheckboxes = true
        notifyDataSetChanged()
    }

    fun disappearCheckboxes(){
        isShowCheckboxes = false
        notifyDataSetChanged()
    }


    private val adapterScope = CoroutineScope(Dispatchers.Default)
    fun addHeaderAndSubmitList(list: List<Track>?,currentTrack: Track?,selectedTracks: List<Track>) {
        adapterScope.launch {
//            val currentTrackPos = list?.indexOf(currentTrack)
//            val beforeCurrentTrack =
//                list?.subList(0, currentTrackPos?.plus(1) ?: 0)
//            val afterCurrentTrack =
//                list?.subList(currentTrackPos?.plus(2) ?: 0,list.size)
//            val items = (beforeCurrentTrack?.map { DataItem.TrackItem(it, it == currentTrack) } ?: listOf()) +
//                    (afterCurrentTrack?.map { DataItem.TrackItem(it, false) } ?: listOf())

            val itemsTemp = list!!.map { DataItem.TrackItem(it,it==currentTrack,selectedTracks.contains(it)) }
            withContext(Dispatchers.Main) {
                submitList(itemsTemp)

            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return  ViewHolder.from(parent)
        }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            when(getItem(position)) {
                is DataItem.TrackItem -> {
                    if (isShowCheckboxes) {
                        val trackItem = getItem(position) as DataItem.TrackItem
                        holder.bind(
                            trackItem.track,
                            trackItem.isPlaying,
                            textColor,
                            darkColor,
                            backgroundChoiceTrack,
                            clickListener,
                            isShowCheckboxes,
                            trackItem.isChecked
                        )
                    }else{
                        val trackItem = getItem(position) as DataItem.TrackItem
                        holder.bind(
                            trackItem.track,
                            trackItem.isPlaying,
                            textColor,
                            darkColor,
                            backgroundChoiceTrack,
                            clickListener,
                            isShowCheckboxes,
                            isShowCheckboxes
                        )
                    }
                }
                is DataItem.Header ->
                    holder.bind (
                        Track(),
                        false,
                        textColor,
                        darkColor,
                        backgroundChoiceTrack,
                        null,
                        false,
                        false
                    )
            }
    }

    sealed class DataItem {
        data class TrackItem(val track: Track,val isPlaying: Boolean,var isChecked: Boolean): DataItem() {
            override val id = track.id
            override var isCurrentTrack = isPlaying
            override var isChose = isChecked

        }

        object Header: DataItem() {
            override val id = Int.MIN_VALUE.toString()
            override var isCurrentTrack = false
            override var isChose = false
        }

        abstract val id: String
        abstract var isCurrentTrack: Boolean
        abstract var isChose: Boolean
    }

    class ViewHolder private constructor
        ( val binding: ItemTrackManagerBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(
            track: Track,
            isPlaying: Boolean,
            textColor: Int,
            darkColor: Int,
            backgroundChoiceTrack: GradientDrawable,
            clickListener: OnClickListener?,
            isShowCheckbox: Boolean,
            isCheck: Boolean?
        ){

            binding.textColor = textColor
            binding.track = track
            binding.darkColor = darkColor

            binding.moreOptionWithTrack.setOnLongClickListener {
                binding.drag = 1
                true
            }

            setupCurrentTrackPlay(isPlaying, backgroundChoiceTrack)
            setupGesture(clickListener, track, isPlaying,isShowCheckbox)

            if (isShowCheckbox){
                binding.buttonChoose.visibility = View.VISIBLE
            }else {
                binding.buttonChoose.visibility = View.GONE
            }

            if (clickListener==null){
                setupIfIsNodeText()
            }else{
                if(!isPlaying) {
                    binding.buttonChoose.setOnCheckedChangeListener { _, isCheck ->
                        if (isCheck) {
                            clickListener.check(track)
                        } else
                            clickListener.unCheck(track)
                    }

                    if (isCheck != null) {
                        binding.buttonChoose.isChecked = isCheck
                    }
                }


            }

            binding.executePendingBindings()

        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setupGesture(clickListener: OnClickListener?, track: Track,isPlaying: Boolean,isShowCheckbox: Boolean){
                val gesture = clickListener?.let { createGesture(it,track,isShowCheckbox)}

                itemView.allViews.all {
                    it.setOnTouchListener { view, motionEvent ->
                        if (view != binding.buttonChoose && view != binding.moreOptionWithTrack && !isPlaying) {
                            gesture?.onTouchEvent(motionEvent)
                            true
                        } else
                            false
                    }
                    true
            }
        }

        private fun setupCurrentTrackPlay(isPlaying: Boolean,backgroundChoiceTrack: GradientDrawable){
            if (isPlaying){
                binding.root.background = backgroundChoiceTrack
                binding.root.requestFocus()
                binding.buttonChoose.isClickable = false
                binding.buttonChoose.isChecked = false
            } else{
                binding.root.setBackgroundColor(Color.TRANSPARENT)
                binding.buttonChoose.isClickable = true
            }

        }

        private fun setupIfIsNodeText(){
                binding.artists.visibility = View.GONE
                binding.buttonChoose.visibility = View.GONE
                binding.frameThumbnail.visibility = View.GONE
                binding.thumbnail.visibility = View.GONE
                binding.name.visibility = View.GONE
                binding.moreOptionWithTrack.visibility = View.GONE
                binding.txtNode.visibility = View.VISIBLE

        }

        companion object{
            fun from(parent: ViewGroup) : ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemTrackManagerBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }

        private fun createGesture(clickListener: OnClickListener, track: Track,isShowCheckbox: Boolean) =
            GestureDetector(itemView.context,object : GestureDetector.SimpleOnGestureListener() {

                override fun onLongPress(e: MotionEvent) {
                    if (!isShowCheckbox)
                        clickListener.check(track)
                    clickListener.onLongPress(track)
                }

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    if (isShowCheckbox)
                        if(binding.buttonChoose.isChecked)
                            clickListener.unCheck(track)
                        else
                            clickListener.check(track)
                    else
                        clickListener.onClick(track)
                    return true
                }
            })

    }

    companion object DiffCallBack: DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem.id==newItem.id
        }

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem == newItem
        }

    }

    class OnClickListener(val clickListener : (track: Track, action: Int) -> Unit) {

        fun onClick(track: Track) = clickListener(track,0)
        fun onLongPress(track: Track) = clickListener(track,1)
        fun check(track: Track) = clickListener(track,2)
        fun unCheck(track: Track) = clickListener(track,3)

    }
}
