package com.example.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "chart_sessions")
data class ChartSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateStr: String, // e.g. "12/06/2026"
    val timeStr: String, // e.g. "14:30"
    val bpm: Int,
    val stressLevel: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val source: String = "Sinh học" // e.g. "Sinh học", "Giọng nói", "Aura AI"
)

@Dao
interface ChartSessionDao {
    @Query("SELECT * FROM chart_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<ChartSession>>
    
    @Query("SELECT COUNT(*) FROM chart_sessions")
    fun getSessionCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChartSession)
}

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)
    
    @Query("DELETE FROM chat_messages")
    suspend fun clearMessages()
}

@Entity(tableName = "favorite_tracks")
data class FavoriteTrack(
    @PrimaryKey val title: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface FavoriteTrackDao {
    @Query("SELECT * FROM favorite_tracks ORDER BY timestamp DESC")
    fun getAllFavoriteTracks(): Flow<List<FavoriteTrack>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(track: FavoriteTrack)
    
    @Query("DELETE FROM favorite_tracks WHERE title = :title")
    suspend fun deleteFavorite(title: String)
}

@Entity(tableName = "gadgetbridge_health_records")
data class GadgetbridgeHealthRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bpm: Int?,
    val spo2: Int?,
    val sleepMinutes: Int?,
    val deepSleepMinutes: Int?,
    val sleepScore: Int?,
    val steps: Int? = null,
    val activeMinutes: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface GadgetbridgeHealthRecordDao {
    @Query("SELECT * FROM gadgetbridge_health_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<GadgetbridgeHealthRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: GadgetbridgeHealthRecord)

    @Query("DELETE FROM gadgetbridge_health_records")
    suspend fun clearAllRecords()
}

@Entity(tableName = "daily_reports")
data class DailyReport(
    @PrimaryKey val dateStr: String, // format "dd/MM/yyyy"
    val avgStress: Int,
    val avgBpm: Int,
    val aiAssessment: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface DailyReportDao {
    @Query("SELECT * FROM daily_reports ORDER BY timestamp DESC")
    fun getAllDailyReports(): Flow<List<DailyReport>>

    @Query("SELECT * FROM daily_reports WHERE dateStr = :dateStr LIMIT 1")
    suspend fun getReportForDate(dateStr: String): DailyReport?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyReport(report: DailyReport)

    @Query("DELETE FROM daily_reports WHERE dateStr = :dateStr")
    suspend fun deleteReportForDate(dateStr: String)
}

class AppRepository(private val db: AppDatabase) {
    val allChartSessions = db.chartSessionDao().getAllSessions()
    val sessionCount = db.chartSessionDao().getSessionCount()
    val allChatMessages = db.chatMessageDao().getAllMessages()
    val allFavoriteTracks = db.favoriteTrackDao().getAllFavoriteTracks()
    val allGadgetbridgeRecords = db.gadgetbridgeHealthRecordDao().getAllRecords()
    val allDailyReports = db.dailyReportDao().getAllDailyReports()

    suspend fun insertChartSession(session: ChartSession) = db.chartSessionDao().insertSession(session)
    
    suspend fun insertChatMessage(message: ChatMessage) = db.chatMessageDao().insertMessage(message)
    suspend fun clearChatMessages() = db.chatMessageDao().clearMessages()
    
    suspend fun insertFavoriteTrack(title: String) = db.favoriteTrackDao().insertFavorite(FavoriteTrack(title))
    suspend fun deleteFavoriteTrack(title: String) = db.favoriteTrackDao().deleteFavorite(title)

    suspend fun insertGadgetbridgeRecord(record: GadgetbridgeHealthRecord) = db.gadgetbridgeHealthRecordDao().insertRecord(record)
    suspend fun clearGadgetbridgeRecords() = db.gadgetbridgeHealthRecordDao().clearAllRecords()

    suspend fun getDailyReportForDate(dateStr: String): DailyReport? = db.dailyReportDao().getReportForDate(dateStr)
    suspend fun insertDailyReport(report: DailyReport) = db.dailyReportDao().insertDailyReport(report)
    suspend fun deleteDailyReportForDate(dateStr: String) = db.dailyReportDao().deleteReportForDate(dateStr)
}
