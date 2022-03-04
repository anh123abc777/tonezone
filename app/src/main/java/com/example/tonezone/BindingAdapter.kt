package com.example.tonezone

import android.net.Uri
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tonezone.adapter.GenreAdapter
import com.example.tonezone.adapter.PlaylistAdapter
import com.example.tonezone.network.Topic
import com.example.tonezone.network.UserPlaylists

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

@BindingAdapter("valueGenres")
fun bindDataGenres(recyclerView: RecyclerView, data: Topic?){
    val adapter = recyclerView.adapter as GenreAdapter
    if (data!=null) {
        adapter.submitList(data.genres)
    }
}

@BindingAdapter("valueYourLibrary")
fun bindDataYourLibrary(recyclerView: RecyclerView, data: UserPlaylists?){
    val adapter = recyclerView.adapter as PlaylistAdapter
    if (data!=null) {
        adapter.submitList(data.items)
    }
}

@BindingAdapter("imageUrl")
fun bindImage(imageView: ImageView,imageUrl: String){

    Glide.with(imageView.context)
        .load(imageUrl)
        .apply(RequestOptions()
            .placeholder(R.drawable.ic_baseline_home_24)
            .error(R.drawable.ic_outline_search_24))
        .into(imageView)
}