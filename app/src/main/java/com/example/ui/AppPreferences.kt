package com.example.ui

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

object AppPreferences {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        if (!this::prefs.isInitialized) {
            prefs = context.getSharedPreferences("aura_settings", Context.MODE_PRIVATE)
        }
    }

    var geminiKey: String
        get() = prefs.getString("gemini_key", BuildConfig.GEMINI_API_KEY) ?: BuildConfig.GEMINI_API_KEY
        set(value) = prefs.edit().putString("gemini_key", value).apply()

    var blynkToken: String
        get() = prefs.getString("blynk_token", BuildConfig.BLYNK_TOKEN).let { if (it.isNullOrBlank()) BuildConfig.BLYNK_TOKEN else it }
        set(value) = prefs.edit().putString("blynk_token", value).apply()

    var spotifyToken: String
        get() = prefs.getString("spotify_token", "") ?: ""
        set(value) = prefs.edit().putString("spotify_token", value).apply()

    var blynkPinBpm: String
        get() = prefs.getString("blynk_pin_bpm", "V0") ?: "V0"
        set(value) = prefs.edit().putString("blynk_pin_bpm", value).apply()

    var blynkPinSpo2: String
        get() = prefs.getString("blynk_pin_spo2", "V1") ?: "V1"
        set(value) = prefs.edit().putString("blynk_pin_spo2", value).apply()

    var favoriteTracks: Set<String>
        get() = prefs.getStringSet("favorite_tracks", null) ?: setOf("Ocean Waves — Deep Healing")
        set(value) = prefs.edit().putStringSet("favorite_tracks", value).apply()

    var customTracks: Set<String>
        get() = prefs.getStringSet("custom_tracks", null) ?: emptySet()
        set(value) = prefs.edit().putStringSet("custom_tracks", value).apply()

    var reminderEnabled: Boolean
        get() = prefs.getBoolean("reminder_enabled", false)
        set(value) = prefs.edit().putBoolean("reminder_enabled", value).apply()
        
    var reminderHour: Int
        get() = prefs.getInt("reminder_hour", 8)
        set(value) = prefs.edit().putInt("reminder_hour", value).apply()
        
    var reminderMinute: Int
        get() = prefs.getInt("reminder_minute", 0)
        set(value) = prefs.edit().putInt("reminder_minute", value).apply()

    var muteMusicOnSpeech: Boolean
        get() = prefs.getBoolean("mute_music_on_speech", true)
        set(value) = prefs.edit().putBoolean("mute_music_on_speech", value).apply()

    var loopMusic: Boolean
        get() = prefs.getBoolean("loop_music", false)
        set(value) = prefs.edit().putBoolean("loop_music", value).apply()

    var lastNotifiedTrendDate: String
        get() = prefs.getString("last_notified_trend_date", "") ?: ""
        set(value) = prefs.edit().putString("last_notified_trend_date", value).apply()

    var webhookUrl: String
        get() = prefs.getString("webhook_url", BuildConfig.WEBHOOK_URL) ?: BuildConfig.WEBHOOK_URL
        set(value) = prefs.edit().putString("webhook_url", value).apply()

    // Physical Profile & Gadgetbridge DB Settings
    var userGender: String
        get() = prefs.getString("user_gender", "Nam") ?: "Nam"
        set(value) = prefs.edit().putString("user_gender", value).apply()

    var userWeight: Float
        get() = prefs.getFloat("user_weight", 65.0f)
        set(value) = prefs.edit().putFloat("user_weight", value).apply()

    var userHeight: Float
        get() = prefs.getFloat("user_height", 170.0f)
        set(value) = prefs.edit().putFloat("user_height", value).apply()

    var gadgetbridgeDbUri: String
        get() = prefs.getString("gadgetbridge_db_uri", "") ?: ""
        set(value) = prefs.edit().putString("gadgetbridge_db_uri", value).apply()

    var gadgetbridgeLastSync: Long
        get() = prefs.getLong("gadgetbridge_last_sync", 0L)
        set(value) = prefs.edit().putLong("gadgetbridge_last_sync", value).apply()

    var gadgetbridgeSyncIntervalMinutes: Int
        get() = prefs.getInt("gadgetbridge_sync_interval_minutes", 15)
        set(value) = prefs.edit().putInt("gadgetbridge_sync_interval_minutes", value).apply()

    var headsetModeEnabled: Boolean
        get() = prefs.getBoolean("headset_mode_enabled", true)
        set(value) = prefs.edit().putBoolean("headset_mode_enabled", value).apply()

    var headsetBeepFeedback: Boolean
        get() = prefs.getBoolean("headset_beep_feedback", true)
        set(value) = prefs.edit().putBoolean("headset_beep_feedback", value).apply()
        
    var emergencyPhone: String
        get() = prefs.getString("emergency_phone", "") ?: ""
        set(value) = prefs.edit().putString("emergency_phone", value).apply()
}
