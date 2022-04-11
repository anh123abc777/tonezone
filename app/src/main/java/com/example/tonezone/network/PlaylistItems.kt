import com.example.tonezone.network.Track

data class DataPlaylistItems(
    val items: List<TrackInPlaylist>
)

data class TrackInPlaylist(
    val added_at: String?,
    val is_local: Boolean?,
    val primary_color: String?,
    val track: Track,
)
