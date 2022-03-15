package com.example.tonezone.network


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class ArtistsObject(
	val artists: Artists?=Artists()
)

@Parcelize
data class Artists(
	val items: List<Artist>?= listOf()
) : Parcelable

@Parcelize
data class Artist (
	val external_urls : External_urls,
	val href : String,
	val id : String,
	val name : String,
	val type : String,
	val uri : String,
	val followers: Follower?,
	val genres: List<String>?,
	val images: List<Image>?,
	val popularity: Int?,
) : Parcelable

@Parcelize
data class Follower(
	val href: String?,
	val total: Long?
) : Parcelable