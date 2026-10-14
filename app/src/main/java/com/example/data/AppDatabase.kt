package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ChartSession::class, ChatMessage::class, FavoriteTrack::class, GadgetbridgeHealthRecord::class, DailyReport::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chartSessionDao(): ChartSessionDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun favoriteTrackDao(): FavoriteTrackDao
    abstract fun gadgetbridgeHealthRecordDao(): GadgetbridgeHealthRecordDao
    abstract fun dailyReportDao(): DailyReportDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aura_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
