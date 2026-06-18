package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaEntity(
    @PrimaryKey val path: String,
    val title: String,
    val mimeType: String,
    val size: Long,
    val duration: Long,
    val dateAdded: Long,
    val isFavorite: Boolean = false,
    val playbackPosition: Long = 0L,
    val isAudio: Boolean
) {
    val durationText: String
        get() {
            val totalSecs = duration / 1000
            val hours = totalSecs / 3600
            val minutes = (totalSecs % 3600) / 60
            val seconds = totalSecs % 60
            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }

    val sizeText: String
        get() {
            if (size <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB")
            val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
            return String.format("%.2f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
        }
}

@Entity(tableName = "playback_history")
data class HistoryEntity(
    @PrimaryKey val path: String,
    val lastPlayedTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val name: String,
    val thumbnailPath: String? = null
)

@Entity(tableName = "playlist_media_cross_ref", primaryKeys = ["playlistName", "mediaPath"])
data class PlaylistMediaCrossRef(
    val playlistName: String,
    val mediaPath: String
)
