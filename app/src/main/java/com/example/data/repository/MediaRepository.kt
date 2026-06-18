package com.example.data.repository

import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import com.example.data.dao.MediaDao
import com.example.data.entity.MediaEntity
import com.example.data.entity.HistoryEntity
import com.example.data.entity.PlaylistEntity
import com.example.data.entity.PlaylistMediaCrossRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MediaRepository(
    private val context: Context,
    private val mediaDao: MediaDao
) {
    val allMedia: Flow<List<MediaEntity>> = mediaDao.getAllMedia()
    val allAudio: Flow<List<MediaEntity>> = mediaDao.getAllAudio()
    val allVideo: Flow<List<MediaEntity>> = mediaDao.getAllVideo()
    val favorites: Flow<List<MediaEntity>> = mediaDao.getFavorites()
    val playbackHistory: Flow<List<MediaEntity>> = mediaDao.getPlaybackHistory()
    val playlists: Flow<List<PlaylistEntity>> = mediaDao.getAllPlaylists()

    // Seeds high-quality preview streaming tracks for the streaming simulator
    private val audioSeeds = listOf(
        MediaEntity(
            path = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            title = "Ambient Echoes - Soundhelix Track 1",
            mimeType = "audio/mpeg",
            size = 8520000L,
            duration = 372000L,
            dateAdded = System.currentTimeMillis(),
            isAudio = true
        ),
        MediaEntity(
            path = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            title = "Sunset Synthwave - Soundhelix Track 4",
            mimeType = "audio/mpeg",
            size = 6210000L,
            duration = 302000L,
            dateAdded = System.currentTimeMillis() - 100000,
            isAudio = true
        ),
        MediaEntity(
            path = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            title = "Deep Woods Resonance - Soundhelix Track 8",
            mimeType = "audio/mpeg",
            size = 7650000L,
            duration = 318000L,
            dateAdded = System.currentTimeMillis() - 200000,
            isAudio = true
        ),
        MediaEntity(
            path = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3",
            title = "Acoustic Whispers - Soundhelix Track 12",
            mimeType = "audio/mpeg",
            size = 6120000L,
            duration = 294000L,
            dateAdded = System.currentTimeMillis() - 300000,
            isAudio = true
        )
    )

    private val videoSeeds = listOf(
        MediaEntity(
            path = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            title = "Big Buck Bunny HD 60fps (MP4)",
            mimeType = "video/mp4",
            size = 75100000L,
            duration = 596000L,
            dateAdded = System.currentTimeMillis() + 8000,
            isAudio = false
        ),
        MediaEntity(
            path = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            title = "Elephants Dream Cinematic - HDR (MKV)",
            mimeType = "video/mp4",
            size = 98400000L,
            duration = 653000L,
            dateAdded = System.currentTimeMillis() + 7000,
            isAudio = false
        ),
        MediaEntity(
            path = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            title = "Tears of Steel Official - 4K Dolby Digital (AVI)",
            mimeType = "video/mp4",
            size = 120500000L,
            duration = 734000L,
            dateAdded = System.currentTimeMillis() + 6000,
            isAudio = false
        ),
        MediaEntity(
            path = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            title = "Symphonic Fire FX Promo (WEBM)",
            mimeType = "video/mp4",
            size = 12000000L,
            duration = 15000L,
            dateAdded = System.currentTimeMillis() + 5000,
            isAudio = false
        )
    )

    suspend fun toggleFavorite(path: String, isFavorite: Boolean) {
        mediaDao.updateFavorite(path, isFavorite)
    }

    suspend fun savePlaybackPosition(path: String, position: Long) {
        mediaDao.updatePlaybackPosition(path, position)
    }

    suspend fun addToHistory(path: String) {
        mediaDao.insertHistory(HistoryEntity(path, System.currentTimeMillis()))
    }

    suspend fun deleteFromHistory(path: String) {
        mediaDao.deleteHistory(path)
    }

    // Playlist CRUD methods
    suspend fun createPlaylist(name: String) {
        mediaDao.insertPlaylist(PlaylistEntity(name))
    }

    suspend fun deletePlaylist(name: String) {
        mediaDao.deletePlaylist(name)
    }

    suspend fun addMediaToPlaylist(playlistName: String, mediaPath: String) {
        mediaDao.insertPlaylistMedia(PlaylistMediaCrossRef(playlistName, mediaPath))
    }

    suspend fun removeMediaFromPlaylist(playlistName: String, mediaPath: String) {
        mediaDao.deletePlaylistMedia(playlistName, mediaPath)
    }

    fun getPlaylistMedia(playlistName: String): Flow<List<MediaEntity>> {
        return mediaDao.getPlaylistMedia(playlistName)
    }

    suspend fun scanStorage(): Int = withContext(Dispatchers.IO) {
        var foundCount = 0
        val scannedItems = mutableListOf<MediaEntity>()

        val contentResolver: ContentResolver = context.contentResolver

        // 1. Scan Videos
        val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val videoProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_ADDED
        )

        try {
            contentResolver.query(videoUri, videoProjection, null, null, null)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(pathColumn)
                    val title = cursor.getString(nameColumn) ?: "Video File"
                    val mime = cursor.getString(mimeColumn) ?: "video/mp4"
                    val size = cursor.getLong(sizeColumn)
                    val duration = cursor.getLong(durationColumn)
                    val dateAdded = cursor.getLong(dateColumn) * 1000 // Convert sec to ms

                    scannedItems.add(
                        MediaEntity(
                            path = path,
                            title = title,
                            mimeType = mime,
                            size = size,
                            duration = duration,
                            dateAdded = dateAdded,
                            isAudio = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Scan Audios
        val audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val audioProjection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED
        )

        try {
            contentResolver.query(audioUri, audioProjection, null, null, null)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(pathColumn)
                    val title = cursor.getString(nameColumn) ?: "Audio File"
                    val mime = cursor.getString(mimeColumn) ?: "audio/mpeg"
                    val size = cursor.getLong(sizeColumn)
                    val duration = cursor.getLong(durationColumn)
                    val dateAdded = cursor.getLong(dateColumn) * 1000

                    scannedItems.add(
                        MediaEntity(
                            path = path,
                            title = title,
                            mimeType = mime,
                            size = size,
                            duration = duration,
                            dateAdded = dateAdded,
                            isAudio = true
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        foundCount = scannedItems.size

        // Fallback or setup mock media if database has absolutely no scanning results or custom additions
        if (scannedItems.isEmpty()) {
            mediaDao.insertMediaList(audioSeeds)
            mediaDao.insertMediaList(videoSeeds)
            foundCount = audioSeeds.size + videoSeeds.size
        } else {
            mediaDao.insertMediaList(scannedItems)
        }

        // Initialize default premium playlists if none exists
        mediaDao.insertPlaylist(PlaylistEntity("Mes Préférés Video", null))
        mediaDao.insertPlaylist(PlaylistEntity("Chill & Lo-Fi Audio", null))

        foundCount
    }
}
