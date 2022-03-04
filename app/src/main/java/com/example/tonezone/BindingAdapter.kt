package com.example.tonezone

import android.net.Uri
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tonezone.adapter.GenreAdapter
import com.example.tonezone.network.Topic

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