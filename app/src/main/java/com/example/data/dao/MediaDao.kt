package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.MediaEntity
import com.example.data.entity.HistoryEntity
import com.example.data.entity.PlaylistEntity
import com.example.data.entity.PlaylistMediaCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items ORDER BY title ASC")
    fun getAllMedia(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE isAudio = 1 ORDER BY title ASC")
    fun getAllAudio(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE isAudio = 0 ORDER BY title ASC")
    fun getAllVideo(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE isFavorite = 1")
    fun getFavorites(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE path = :path")
    suspend fun getMediaByPath(path: String): MediaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: MediaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaList(mediaList: List<MediaEntity>)

    @Update
    suspend fun updateMedia(media: MediaEntity)

    @Query("UPDATE media_items SET isFavorite = :isFavorite WHERE path = :path")
    suspend fun updateFavorite(path: String, isFavorite: Boolean)

    @Query("UPDATE media_items SET playbackPosition = :position WHERE path = :path")
    suspend fun updatePlaybackPosition(path: String, position: Long)

    // Playback History
    @Query("SELECT m.* FROM media_items m INNER JOIN playback_history h ON m.path = h.path ORDER BY h.lastPlayedTime DESC")
    fun getPlaybackHistory(): Flow<List<MediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("DELETE FROM playback_history WHERE path = :path")
    suspend fun deleteHistory(path: String)

    // Playlists
    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE name = :name")
    suspend fun deletePlaylist(name: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistMedia(crossRef: PlaylistMediaCrossRef)

    @Query("DELETE FROM playlist_media_cross_ref WHERE playlistName = :playlistName AND mediaPath = :mediaPath")
    suspend fun deletePlaylistMedia(playlistName: String, mediaPath: String)

    @Query("SELECT m.* FROM media_items m INNER JOIN playlist_media_cross_ref ref ON m.path = ref.mediaPath WHERE ref.playlistName = :playlistName")
    fun getPlaylistMedia(playlistName: String): Flow<List<MediaEntity>>
}
