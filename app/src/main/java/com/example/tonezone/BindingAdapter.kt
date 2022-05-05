package com.example.tonezone

import android.content.res.ColorStateList
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tonezone.adapter.*
import com.example.tonezone.network.*
import com.example.tonezone.player.PlayerScreenViewModel
import com.example.tonezone.utils.Signal
import com.example.tonezone.utils.convertSignalToIcon
import com.example.tonezone.utils.convertSignalToText
import com.example.tonezone.yourlibrary.SortOption
import com.google.android.exoplayer2.Player
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

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

@BindingAdapter("relateArtists")
fun bindArtistsRecyclerview(recyclerView: RecyclerView, list: List<Artist>?){
    if(list!=null){
        val adapter = recyclerView.adapter as ArtistsAdapter
        adapter.submitList(list)
    }
}

@BindingAdapter(value = ["playlistData","artistData","trackData","albumData","userSavedTracksData","sortOption","keyWord"],requireAll = false)
fun bindDataYourLibrary(recyclerView: RecyclerView,
                        playlistData: List<Playlist>?,
                        artistData: List<Artist>?,
                        trackData: List<Track>?,
                        albumData: List<Album>?,
                        userSavedTracksData: List<SavedTrack>?,
                        sortOption: SortOption?,
                        keyWord: String?
                        ){

    val adapter = recyclerView.adapter as LibraryAdapter

    val userSavedTracks =
        if(!userSavedTracksData.isNullOrEmpty())
            listOf(Playlist("UserSavedTrack","liked Songs",
             listOf(Image(null,url="https://picsum.photos/300/300",null)),"User's save songs",Owner(""),
            false,"playlist", listOf()))
        else
            listOf()

    val playlists = if(playlistData!=null) userSavedTracks+playlistData else userSavedTracks
    val tracks = trackData ?: listOf()
    val artists = artistData ?: listOf()
    val albums = albumData ?: listOf()

    adapter.submitYourLibrary(playlists, artists, tracks, albums)
    when(sortOption){
        SortOption.Alphabetical -> adapter.sortByAlphabetical()
        SortOption.Creator -> adapter.sortByCreator()
        SortOption.MostRelate -> keyWord?.let { adapter.sortByMostRelate(it) }
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
                   .error(R.drawable.ic_baseline_album_24)
            )
            .into(imageView)

    }
    if(imageUrl!=null && imageUrl!="")
        Glide.with(imageView.context)
            .load(imageUrl)
            .apply(RequestOptions()
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_baseline_album_24)
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


@BindingAdapter("isNavigatingCurrentPlaylist")
fun bindLayoutVisibility(relativeLayout: RelativeLayout, isShowing: Boolean?){
        when(isShowing){
            null -> relativeLayout.visibility = View.VISIBLE
            false -> hiddenView(relativeLayout)
        }
}

@BindingAdapter("isNavigatingCurrentPlaylist")
fun bindLayoutVisibility(linearLayout: LinearLayout, isShowing: Boolean?){
    when(isShowing){
        false -> hiddenView(linearLayout)
    }
}

private fun hiddenView(view: View){
    val animationDown = AnimationUtils.loadAnimation(view.context,R.anim.bottom_down)
    animationDown.setAnimationListener(object: Animation.AnimationListener{
        override fun onAnimationStart(p0: Animation?) {
            view.visibility = View.VISIBLE
        }

        override fun onAnimationEnd(p0: Animation?) {
            view.visibility = View.GONE

        }

        override fun onAnimationRepeat(p0: Animation?) {
        }
    })
        view.startAnimation(animationDown)
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

@BindingAdapter(value = ["playerState","isPlayerScreen"],requireAll = false)
fun bindStatePlayButton(button: ImageButton, state: PlayerScreenViewModel.PlayerState, isPlayerScreen: Boolean){
    if (isPlayerScreen)
        when(state){
            PlayerScreenViewModel.PlayerState.PLAY -> button.setImageResource(R.drawable.ic_custom_pause)
            PlayerScreenViewModel.PlayerState.PAUSE -> button.setImageResource(R.drawable.ic_custom_play)
            else -> button.setImageResource(R.drawable.ic_custom_play)
        }
    else
        when(state){
            PlayerScreenViewModel.PlayerState.PLAY -> button.setImageResource(R.drawable.ic_pause)
            PlayerScreenViewModel.PlayerState.PAUSE -> button.setImageResource(R.drawable.ic_play_arrow)
            else -> button.setImageResource(R.drawable.ic_play_arrow)
        }
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
    val drawable = ContextCompat.getDrawable(button.context,convertSignalToIcon(signal))!!
    button.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable,null,null,null)
    button.compoundDrawablePadding = 36
}

@BindingAdapter("isVisibility")
fun setupButtonVisibility(imageButton: ImageButton, isOwned: Boolean){
    if(isOwned){
        imageButton.visibility = View.GONE
    }
    else imageButton.visibility = View.VISIBLE
}

@BindingAdapter("isFollowing")
fun bindTextButton(button: Button,isFollowing: Boolean){
    if(isFollowing){
        button.text = "following"
        button.setBackgroundColor(ContextCompat.getColor(button.context,R.color.colorSecondary))
    }else {
        button.text = "follow"
        button.setBackgroundColor(ContextCompat.getColor(button.context,R.color.colorPrimary))

    }
}

@BindingAdapter("profileVisibility")
fun bindPlaylistProfile(linearLayout: LinearLayout,playlistInfo: PlaylistInfo){
    if(playlistInfo.type!="artist")
        linearLayout.visibility = View.VISIBLE
    else
        linearLayout.visibility = View.GONE
}

@BindingAdapter("imageProfileVisibility")
fun bindImageProfile(imageView: ImageView,playlistInfo: PlaylistInfo){
    if(playlistInfo.type!="artist")
        imageView.visibility = View.VISIBLE
    else
        imageView.visibility = View.GONE
}

@BindingAdapter(value = ["repeatMode","lightColor","darkColor"])
fun bindIconRepeatButton(imageButton: ImageButton,repeatMode: Int,lightColor: Int, darkColor: Int){
    var imageResource = when(repeatMode){
        Player.REPEAT_MODE_ALL -> R.drawable.ic_repeat
        Player.REPEAT_MODE_ONE -> R.drawable.ic_repeat_one
        Player.REPEAT_MODE_OFF -> {
            R.drawable.ic_repeat
        }
        else -> R.drawable.ic_repeat
    }

    imageButton.setImageResource(imageResource)
    if (repeatMode==0){
        imageButton.imageTintList = ColorStateList.valueOf(darkColor)
    }else
        imageButton.imageTintList = ColorStateList.valueOf(lightColor)
}

@BindingAdapter(value = ["isChoosing","lightButtonTint","darkButtonTint"])
fun bindColorShuffleButton(imageButton: ImageButton, isChoosing: Boolean, lightButtonTint: Int, darkButtonTint: Int){

        if(isChoosing)
            imageButton.imageTintList = ColorStateList.valueOf(lightButtonTint)
        else
            imageButton.imageTintList = ColorStateList.valueOf(darkButtonTint)

}

@BindingAdapter( "stateButton")
fun bindStateButton(imageButton: ImageButton, isPositive: Boolean){
        if (isPositive)
            imageButton.setColorFilter(
                ContextCompat.getColor(
                    imageButton.context,
                    R.color.colorSecondary
                )
            )
        else
            imageButton.setColorFilter(ContextCompat.getColor(imageButton.context, R.color.gray))
}

@BindingAdapter("backgroundColor")
fun bindBackGroundColor(relativeLayout: RelativeLayout,color: Int){
    relativeLayout.setBackgroundColor(color)
}

@BindingAdapter("dataItems")
fun bindLibraryRecyclerView(recyclerView: RecyclerView,dataItems: List<LibraryAdapter.DataItem>?){
    if (dataItems!=null){
        val adapter = recyclerView.adapter as LibraryAdapter
        adapter.submitListDataItems(dataItems)
        adapter.sortByDefault()
    }
}

@BindingAdapter("isShowAddTracksButton")
fun bindAddTracksButtonVisibility(button: MaterialButton,size: Int?){
    if(size!=null && size != 0){
        button.visibility = View.GONE
    }else
        button.visibility = View.VISIBLE
}
