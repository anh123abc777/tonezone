package com.example.tonezone.network


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Artist (

	val external_urls : External_urls,
	val href : String,
	val id : String,
	val name : String,
	val type : String,
	val uri : String
) : Parcelable