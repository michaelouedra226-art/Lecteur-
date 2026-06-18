package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.MediaDao
import com.example.data.entity.MediaEntity
import com.example.data.entity.HistoryEntity
import com.example.data.entity.PlaylistEntity
import com.example.data.entity.PlaylistMediaCrossRef

@Database(
    entities = [
        MediaEntity::class,
        HistoryEntity::class,
        PlaylistEntity::class,
        PlaylistMediaCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PremiumDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao

    companion object {
        @Volatile
        private var INSTANCE: PremiumDatabase? = null

        fun getDatabase(context: Context): PremiumDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PremiumDatabase::class.java,
                    "premium_media_player_db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
