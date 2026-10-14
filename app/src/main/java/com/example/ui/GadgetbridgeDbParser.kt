package com.example.ui

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import java.io.File
import java.util.Calendar

object GadgetbridgeDbParser {
    private const val TAG = "GadgetbridgeDbParser"

    data class SyncResult(
        val success: Boolean,
        val heartRate: Int? = null,
        val spo2: Int? = null,
        val sleepMinutes: Int? = null,
        val deepSleepMinutes: Int? = null,
        val lightSleepMinutes: Int? = null,
        val sleepScore: Int? = null,
        val steps: Int? = null,
        val activeMinutes: Int? = null,
        val message: String
    )

    fun syncFromDatabase(context: Context, dbUriString: String): SyncResult {
        if (dbUriString.isBlank()) {
            return SyncResult(false, message = "Chưa cấu hình đường dẫn cơ sở dữ liệu Gadgetbridge.")
        }

        val uri = Uri.parse(dbUriString)
        val tempFile = File(context.cacheDir, "gadgetbridge_import.db")

        // 1. Copy Content Provider InputStream to Local File
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return SyncResult(false, message = "Không thể mở file từ liên kết đã chọn.")
        } catch (e: SecurityException) {
            Log.e(TAG, "Quyền truy cập file bị từ chối", e)
            return SyncResult(false, message = "Lỗi bảo mật: Hãy chọn lại file Gadgetbridge.db để cập nhật quyền truy cập.")
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi sao chép cơ sở dữ liệu", e)
            return SyncResult(false, message = "Lỗi đọc file: ${e.localizedMessage}")
        }

        // 2. Open and Query SQLite Database
        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openDatabase(tempFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            
            // Log available tables for debugging
            val tables = mutableListOf<String>()
            db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null).use { cursor ->
                while (cursor.moveToNext()) {
                    tables.add(cursor.getString(0))
                }
            }
            Log.d(TAG, "Tables found in Gadgetbridge DB: $tables")

            // Identify correct activity sample table (RAW_ACTIVITY_SAMPLE is standard)
            var activityTable: String? = null
            var hrColumn: String? = null
            var stepsColumn: String? = null
            var timestampColumn = "TIMESTAMP"
            var kindColumn = "RAW_KIND"
            var maxRowCount = 0

            Log.d(TAG, "Starting robust table and column discovery in: $tables")

            // Scan all tables to find activity samples with heart rate, and choose the one with actual data (largest row count)
            for (t in tables) {
                if (t.contains("ACTIVITY_SAMPLE", ignoreCase = true) || 
                    t.contains("ACTIVITY", ignoreCase = true) ||
                    t.contains("HEART_RATE_SAMPLE", ignoreCase = true)
                ) {
                    val cols = mutableListOf<String>()
                    try {
                        db.rawQuery("PRAGMA table_info($t)", null).use { cursor ->
                            while (cursor.moveToNext()) {
                                cols.add(cursor.getString(1)) // Column name is at index 1
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error querying table_info for $t", e)
                    }

                    val foundHrCol = cols.firstOrNull { 
                        it.equals("HEART_RATE", ignoreCase = true) || 
                        it.equals("HEARTRATE", ignoreCase = true) || 
                        it.equals("BPM", ignoreCase = true) ||
                        it.equals("HEART_RATE_BPM", ignoreCase = true)
                    }
                    if (foundHrCol != null) {
                        var rowCount = 0
                        try {
                            db.rawQuery("SELECT COUNT(*) FROM $t", null).use { cursor ->
                                if (cursor.moveToFirst()) {
                                    rowCount = cursor.getInt(0)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error counting rows for $t", e)
                        }

                        Log.d(TAG, "Candidate table $t has $rowCount rows. columns: $cols")

                        if (rowCount > maxRowCount || (activityTable == null && rowCount >= 0)) {
                            activityTable = t
                            hrColumn = foundHrCol
                            maxRowCount = rowCount
                            
                            val foundTimeCol = cols.firstOrNull { 
                                it.equals("TIMESTAMP", ignoreCase = true) || 
                                it.equals("TIME", ignoreCase = true) ||
                                it.equals("UTC_TIMESTAMP", ignoreCase = true)
                            }
                            if (foundTimeCol != null) {
                                timestampColumn = foundTimeCol
                            }
                            
                            val foundKindCol = cols.firstOrNull {
                                it.equals("RAW_KIND", ignoreCase = true) ||
                                it.equals("ACTIVITY_KIND", ignoreCase = true) ||
                                it.equals("KIND", ignoreCase = true)
                            }
                            if (foundKindCol != null) {
                                kindColumn = foundKindCol
                            }

                            val foundStepsCol = cols.firstOrNull {
                                it.equals("STEPS", ignoreCase = true) ||
                                it.equals("STEP", ignoreCase = true) ||
                                it.equals("STEPS_COUNT", ignoreCase = true)
                            }
                            if (foundStepsCol != null) {
                                stepsColumn = foundStepsCol
                            }
                        }
                    }
                }
            }

            // Fallback: If no matches with specific names, look for RAW_ACTIVITY_SAMPLE or HUAMI_ACTIVITY_SAMPLE
            if (activityTable == null) {
                activityTable = when {
                    tables.contains("RAW_ACTIVITY_SAMPLE") -> "RAW_ACTIVITY_SAMPLE"
                    tables.contains("HUAMI_ACTIVITY_SAMPLE") -> "HUAMI_ACTIVITY_SAMPLE"
                    tables.contains("BASE_ACTIVITY_SAMPLE") -> "BASE_ACTIVITY_SAMPLE"
                    else -> tables.firstOrNull { it.contains("ACTIVITY_SAMPLE", ignoreCase = true) }
                }
                hrColumn = "HEART_RATE"
                Log.d(TAG, "Fallback activity sample table chosen: $activityTable")
            }

            if (activityTable == null) {
                return SyncResult(false, message = "Không tìm thấy bảng dữ liệu mẫu hoạt động phù hợp.")
            }

            Log.d(TAG, "Selected activity table: $activityTable, hrColumn: $hrColumn, rowCount: $maxRowCount")

            var parsedHeartRate: Int? = null
            var parsedSpo2: Int? = null
            
            // 3. Query Latest Heart Rate
            if (hrColumn != null) {
                try {
                    db.rawQuery(
                        "SELECT $hrColumn FROM $activityTable WHERE $hrColumn > 30 AND $hrColumn < 220 ORDER BY $timestampColumn DESC LIMIT 1",
                        null
                    ).use { cursor ->
                        if (cursor.moveToFirst()) {
                            parsedHeartRate = cursor.getInt(0)
                            Log.d(TAG, "Latest parsed heart rate from $activityTable ($hrColumn): $parsedHeartRate")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Lỗi đọc nhịp tim từ $activityTable ($hrColumn)", e)
                }
            }

            // 4. Query Latest SpO2 (From SPO2_SAMPLE or similar tables)
            var spo2Table: String? = null
            var spo2Column: String? = null
            var spo2TimestampCol = "TIMESTAMP"
            var maxSpo2RowCount = 0

            for (t in tables) {
                if (t.contains("SPO2", ignoreCase = true) || t.contains("SPO_2", ignoreCase = true)) {
                    val cols = mutableListOf<String>()
                    try {
                        db.rawQuery("PRAGMA table_info($t)", null).use { cursor ->
                            while (cursor.moveToNext()) {
                                cols.add(cursor.getString(1))
                            }
                        }
                    } catch (e: Exception) {
                        // ignore
                    }
                    val foundSpo2Col = cols.firstOrNull { 
                        it.equals("SPO2", ignoreCase = true) || 
                        it.equals("VALUE", ignoreCase = true) || 
                        it.equals("SPO2_PERCENT", ignoreCase = true) ||
                        it.equals("PERCENT", ignoreCase = true) ||
                        it.equals("SPO", ignoreCase = true)
                    }
                    if (foundSpo2Col != null) {
                        var rowCount = 0
                        try {
                            db.rawQuery("SELECT COUNT(*) FROM $t", null).use { cursor ->
                                if (cursor.moveToFirst()) {
                                    rowCount = cursor.getInt(0)
                                }
                            }
                        } catch (e: Exception) {
                            // ignore
                        }
                        
                        Log.d(TAG, "SPO2 candidate table $t has $rowCount rows.")
                        
                        if (rowCount > maxSpo2RowCount || (spo2Table == null && rowCount >= 0)) {
                            spo2Table = t
                            spo2Column = foundSpo2Col
                            maxSpo2RowCount = rowCount
                            val foundTimeCol = cols.firstOrNull { 
                                it.equals("TIMESTAMP", ignoreCase = true) || 
                                it.equals("TIME", ignoreCase = true) 
                            }
                            if (foundTimeCol != null) {
                                spo2TimestampCol = foundTimeCol
                            }
                        }
                    }
                }
            }

            if (spo2Table != null && spo2Column != null) {
                try {
                    db.rawQuery(
                        "SELECT $spo2Column FROM $spo2Table WHERE $spo2Column >= 50 AND $spo2Column <= 100 ORDER BY $spo2TimestampCol DESC LIMIT 1",
                        null
                    ).use { cursor ->
                        if (cursor.moveToFirst()) {
                            parsedSpo2 = cursor.getInt(0)
                            Log.d(TAG, "Latest parsed SpO2 from $spo2Table ($spo2Column): $parsedSpo2")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Lỗi đọc SpO2 từ $spo2Table ($spo2Column)", e)
                }
            }

            // Fallback check for SpO2 column inside activityTable
            if (parsedSpo2 == null && activityTable != null) {
                val cols = mutableListOf<String>()
                try {
                    db.rawQuery("PRAGMA table_info($activityTable)", null).use { cursor ->
                        while (cursor.moveToNext()) {
                            cols.add(cursor.getString(1))
                        }
                    }
                } catch (e: Exception) {
                    // ignore
                }
                val foundSpo2Col = cols.firstOrNull { 
                    it.equals("SPO2", ignoreCase = true) || 
                    it.equals("SPO2_PERCENT", ignoreCase = true) ||
                    it.equals("SPO", ignoreCase = true)
                }
                if (foundSpo2Col != null) {
                    try {
                        db.rawQuery(
                            "SELECT $foundSpo2Col FROM $activityTable WHERE $foundSpo2Col >= 50 AND $foundSpo2Col <= 100 ORDER BY $timestampColumn DESC LIMIT 1",
                            null
                        ).use { cursor ->
                            if (cursor.moveToFirst()) {
                                parsedSpo2 = cursor.getInt(0)
                                Log.d(TAG, "Latest parsed SpO2 fallback from $activityTable ($foundSpo2Col): $parsedSpo2")
                            }
                        }
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }

            // 5. Query Sleep Data for Last 24 Hours
            var deepSleepMinutes = 0
            var lightSleepMinutes = 0
            var sleepScore = 0
            var totalSleepMinutes = 0

            val calendar = Calendar.getInstance().apply {
                add(Calendar.HOUR_OF_DAY, -24)
            }
            val secondsAgo = calendar.timeInMillis / 1000

            try {
                // Gadgetbridge stores activity kind / sleep state in RAW_KIND
                // 112/4: Light Sleep, 122/5: Deep Sleep, 121: REM, 123: Awake/Other
                db.rawQuery(
                    "SELECT $kindColumn, COUNT(*) as minutes FROM $activityTable WHERE $timestampColumn >= ? GROUP BY $kindColumn",
                    arrayOf(secondsAgo.toString())
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        val kind = cursor.getInt(0)
                        val mins = cursor.getInt(1)
                        when (kind) {
                            122, 5 -> deepSleepMinutes += mins
                            112, 4 -> lightSleepMinutes += mins
                            121 -> lightSleepMinutes += mins // Count REM as light/restful sleep for simplicity
                        }
                    }
                }

                totalSleepMinutes = deepSleepMinutes + lightSleepMinutes
                if (totalSleepMinutes > 0) {
                    // Calculate a sleep score out of 100
                    // Ideal sleep: 480 minutes (8 hours)
                    // Ideal deep sleep ratio: ~20-25% of total sleep
                    val durationScore = (totalSleepMinutes / 480f * 70f).coerceAtMost(70f)
                    val ratioScore = if (totalSleepMinutes > 0) {
                        val deepRatio = deepSleepMinutes.toFloat() / totalSleepMinutes
                        if (deepRatio in 0.15f..0.30f) 30f else 15f
                    } else {
                        0f
                    }
                    sleepScore = (durationScore + ratioScore).toInt().coerceIn(10, 100)
                    Log.d(TAG, "Calculated Sleep: Total $totalSleepMinutes min, Deep $deepSleepMinutes min, Score $sleepScore")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi tính toán giấc ngủ", e)
            }

            var parsedSteps: Int? = null
            var parsedActiveMinutes: Int? = null

            try {
                if (stepsColumn != null) {
                    db.rawQuery(
                        "SELECT SUM($stepsColumn) FROM $activityTable WHERE $timestampColumn >= ?",
                        arrayOf(secondsAgo.toString())
                    ).use { cursor ->
                        if (cursor.moveToFirst()) {
                            val sum = cursor.getInt(0)
                            if (sum > 0) parsedSteps = sum
                        }
                    }
                    
                    db.rawQuery(
                        "SELECT COUNT(*) FROM $activityTable WHERE $timestampColumn >= ? AND ($stepsColumn > 0 OR $kindColumn = 1)",
                        arrayOf(secondsAgo.toString())
                    ).use { cursor ->
                        if (cursor.moveToFirst()) {
                            val count = cursor.getInt(0)
                            if (count > 0) parsedActiveMinutes = count
                        }
                    }
                } else {
                    // Fallback to RAW_KIND = 1 minutes
                    db.rawQuery(
                        "SELECT COUNT(*) FROM $activityTable WHERE $timestampColumn >= ? AND $kindColumn = 1",
                        arrayOf(secondsAgo.toString())
                    ).use { cursor ->
                        if (cursor.moveToFirst()) {
                            val count = cursor.getInt(0)
                            if (count > 0) parsedActiveMinutes = count
                        }
                    }
                }
                Log.d(TAG, "Calculated Activity: Steps $parsedSteps, Active Minutes $parsedActiveMinutes")
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi tính toán hoạt động tích cực", e)
            }

            return SyncResult(
                success = true,
                heartRate = parsedHeartRate,
                spo2 = parsedSpo2,
                sleepMinutes = if (totalSleepMinutes > 0) totalSleepMinutes else null,
                deepSleepMinutes = if (deepSleepMinutes > 0) deepSleepMinutes else null,
                lightSleepMinutes = if (lightSleepMinutes > 0) lightSleepMinutes else null,
                sleepScore = if (sleepScore > 0) sleepScore else null,
                steps = parsedSteps,
                activeMinutes = parsedActiveMinutes,
                message = "Đồng bộ thành công dữ liệu từ Gadgetbridge!"
            )

        } catch (e: Exception) {
            Log.e(TAG, "Lỗi phân tích cơ sở dữ liệu", e)
            return SyncResult(false, message = "Lỗi kết nối CSDL: ${e.localizedMessage}")
        } finally {
            try {
                db?.close()
                if (tempFile.exists()) {
                    tempFile.delete()
                }
            } catch (e: Exception) {
                // Ignore closing exceptions
            }
        }
    }
}
