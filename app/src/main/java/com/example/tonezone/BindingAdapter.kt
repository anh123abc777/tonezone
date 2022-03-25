package com.example.tonezone

import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tonezone.adapter.*
import com.example.tonezone.network.*
import com.example.tonezone.player.PlayerScreenViewModel
import com.example.tonezone.utils.Signal
import com.example.tonezone.utils.convertDPtoInt
import com.example.tonezone.utils.convertSignalToIcon
import com.example.tonezone.utils.convertSignalToText
import com.example.tonezone.yourlibrary.SortOption
import com.google.android.material.chip.Chip

//@BindingAdapter("imageUrl")
//fun bindImage(imgView : ImageView, imgUrl : String?){
//    imgUrl?.let {
//        val imgUri : Uri = ewImgUrl.toUri().buildUpon().scheme("https").build()
//
//        Glide.with(imgView.context)
//            .load(imgUri)
//            .apply(
//                RequestOptions())
//            .into(imgView)
//    }
//}

//@BindingAdapter("valueGenres")
//fun bindDataGenres(recyclerView: RecyclerView, data: Topic?){
//    val adapter = recyclerView.adapter as GenreAdapter
//    if (data!=null) {
//        adapter.submitList(data.genres)
//    }
//}

@BindingAdapter("groupPlaylists")
fun bindGroupPlaylistsRecyclerview(recyclerView: RecyclerView, list: List<GroupPlaylist>?){
    if (list!=null){
        val adapter = recyclerView.adapter as GroupPlaylistAdapter
        adapter.submitList(list)
    }
}

@BindingAdapter("playlistInGridData")
fun bindPlaylistRecyclerview(recyclerView: RecyclerView, list: List<Playlist>?){
    if(list!=null){
        val adapter = recyclerView.adapter as PlaylistAdapter
        adapter.submitList(list)
    }
}

@BindingAdapter(value = ["playlistData","artistData","trackData","userSavedTracksData","sortOption"],requireAll = false)
fun bindDataYourLibrary(recyclerView: RecyclerView,
                        playlistData: List<Playlist>?,
                        artistData: List<Artist>?,
                        trackData: List<Track>?,
                        userSavedTracksData: List<SavedTrack>?,
                        sortOption: SortOption?){

    val adapter = recyclerView.adapter as LibraryAdapter

    val userSavedTracks =
        if(!userSavedTracksData.isNullOrEmpty())
            listOf(Playlist("userSavedTrack","liked Songs",
            "", listOf(Image(null,url="https://picsum.photos/300/300",null)),"User's save songs",Owner(""),
            0,false,"playlist",""))
        else
            listOf()

    Log.i("bindAdapter",userSavedTracks.toString())

    val playlists = if(playlistData!=null) userSavedTracks+playlistData else userSavedTracks

    Log.i("bindAdapterPlaylist",playlists.toString())
    val tracks = trackData ?: listOf()
    val artists = artistData ?: listOf()

    Log.i("bindAdapterPlaylist",tracks.toString())

    adapter.submitYourLibrary(playlists, artists, tracks)
    when(sortOption){
        SortOption.Alphabetical -> adapter.sortByAlphabetical()
        SortOption.Creator -> adapter.sortByCreator()
        null -> adapter.sortByDefault()
    }

}

@BindingAdapter(value = ["imageUrl","listImageUrl"],requireAll = false)
fun bindImage(imageView: ImageView,imageUrl: String?,listImageUrl: List<Image>?){
//var numBlur = 0
//    if (blur == null)
//        numBlur=1
//    else
//        numBlur = blur

    if(listImageUrl?.size!=0) {
        Glide.with(imageView.context)
            .load(listImageUrl?.get(0)?.url)
            .apply(
               RequestOptions()
                   .placeholder(R.drawable.loading_animation)
                   .error(R.drawable.ic_connection_error)
            )
            .into(imageView)

    }
    if(imageUrl!=null)
        Glide.with(imageView.context)
            .load(imageUrl)
            .apply(RequestOptions()
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_connection_error)
                )
            .into(imageView)
}
//
//@BindingAdapter("backgroundUri")
//fun bindBackgroundLayout(imageView: ImageView, listImageUrl: List<Image>?){
//    if (listImageUrl?.size!=0){
//
//        Blurry.with(imageView.context)
//            .radius(10)
//            .sampling(8)
//            .async()
//            .capture(imageView)
//            .into(imageView)
//    }
//}

@BindingAdapter("layoutVisibility")
fun bindLayoutVisibility(relativeLayout: RelativeLayout, track: Track?){

    relativeLayout.visibility = if(track!=Track() && track!=null) View.VISIBLE else View.GONE
}

@BindingAdapter("formatTime")
fun bindTime(textView: TextView, timeInt : Long?){
    timeInt?.let {
        textView.text = DateUtils.formatElapsedTime(timeInt/1000)
    }
}

@BindingAdapter("artists")
fun bindTextView(textView: TextView, list: List<Artist>){
    var artists = ""
    list.forEachIndexed { index, artist ->
        artists += artist.name+ if(index!=list.size-1) ", " else ""
    }
    textView.text = artists
}

@BindingAdapter("playerState")
fun bindStatePlayButton(button: AppCompatButton, state: PlayerScreenViewModel.PlayerState){
    when(state){
        PlayerScreenViewModel.PlayerState.PLAY -> button.setBackgroundResource(R.drawable.ic_custom_pause)
        PlayerScreenViewModel.PlayerState.PAUSE -> button.setBackgroundResource(R.drawable.ic_custom_play)
        else -> button.setBackgroundResource(R.drawable.ic_custom_play)
    }
}

@BindingAdapter("isChoose")
fun bindColorShuffleButton(imageView: ImageView, isChoose: Boolean){
    if(isChoose)
        imageView.setColorFilter(ContextCompat.getColor(imageView.context,R.color.colorSecondary))
    else
        imageView.setColorFilter(ContextCompat.getColor(imageView.context,R.color.gray))
}


@BindingAdapter("sizeList")
fun bindChip(chip: Chip, sizeList: Int?){
    if(sizeList!=0 && sizeList!=null)
        chip.visibility = View.VISIBLE
    else
        chip.visibility = View.GONE
}

@BindingAdapter("sizeSearchedItems")
fun bindContentSearchForItem(textView: TextView,size: Int?){
    when(size){
        0,null -> textView.visibility = View.VISIBLE
        else -> textView.visibility = View.GONE
    }
}

@BindingAdapter("signal")
fun setTextBottomSheetItem(button: Button, signal: Signal){
    button.text = convertSignalToText(signal)
}

@BindingAdapter("signalIcon")
fun setIconBottomSheetItem(button: Button,signal: Signal){
    val drawable = ContextCompat.getDrawable(button.context,convertSignalToIcon(signal))
    button.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable,null,null,null)
    button.compoundDrawablePadding = 36
}
