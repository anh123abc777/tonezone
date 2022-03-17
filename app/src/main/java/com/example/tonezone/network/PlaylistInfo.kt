package com.example.tonezone.network

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class PlaylistInfo(
    val id: String,
    val name: String,
    val description: String,
    val image: String?,
    val uri: String,
    val type: String,
) : Parcelable