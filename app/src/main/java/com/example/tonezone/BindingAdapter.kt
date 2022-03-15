package com.example.tonezone

import android.graphics.BlurMaskFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.request.RequestOptions
import com.example.tonezone.adapter.GenreAdapter
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.adapter.TrackAdapter
import com.example.tonezone.network.*
import com.example.tonezone.player.PlayerScreenViewModel
import com.google.android.material.chip.Chip
import jp.wasabeef.blurry.Blurry
import jp.wasabeef.glide.transformations.BlurTransformation

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

@BindingAdapter(value = ["playlistData","artistData"],requireAll = false)
fun bindDataYourLibrary(recyclerView: RecyclerView, playlistData: List<Playlist>?, artistData: List<Artist>?){
    val adapter = recyclerView.adapter as LibraryAdapter
    if (playlistData != null) {
        if (artistData != null) {
            if (playlistData.isNotEmpty() || artistData.isNotEmpty()) {
                adapter.submitYourLibrary(playlistData,artistData)
            }
        }
    }
}

@BindingAdapter("playlist")
fun bindDataPlaylist(recyclerView: RecyclerView,data: List<Track>?){
    val adapter = recyclerView.adapter as TrackAdapter
    if(data!=null){
        adapter.submitList(data)
    }
}

@BindingAdapter(value = ["imageUrl","listImageUrl","blur"],requireAll = false)
fun bindImage(imageView: ImageView,imageUrl: String?,listImageUrl: List<Image>?,blur: Int?){
var gidoblur = 0
    if (blur == null)
        gidoblur=1
    else
        gidoblur = blur
    if(listImageUrl?.size!=0) {
        Glide.with(imageView.context)
            .load(listImageUrl?.get(0)?.url)
            .apply(
               RequestOptions.bitmapTransform(BlurTransformation(gidoblur,2))
            )
            .into(imageView)

    }
    if(imageUrl!=null)
        Glide.with(imageView.context)
            .load(imageUrl)
            .apply(RequestOptions()
                .placeholder(R.drawable.ic_baseline_home_24)
                .error(R.drawable.ic_custom_play)
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
    }
}

@BindingAdapter("isShuffling")
fun bindColorShuffleButton(imageView: ImageView, isShuffling: Boolean){
    if(isShuffling)
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