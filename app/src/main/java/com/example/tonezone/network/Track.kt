import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Track (
	val artists : List<Artists>,
	val disc_number : Int,
	val duration_ms : Int,
	val explicit : Boolean,
	val external_urls : External_urls,
	val href : String,
	val id : String,
	val is_local : Boolean,
	val is_playable : Boolean,
	val name : String,
	val preview_url : String,
	val track_number : Int,
	val type : String,
	val uri : String
) : Parcelable