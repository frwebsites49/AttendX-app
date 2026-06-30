package com.college.attendx.utils

import android.content.Context
import android.content.SharedPreferences
import java.util.Date

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    private val SESSION_TIMEOUT_MS = 30 * 60 * 1000 // 30 minutes

    fun startSession(userId: String) {
        prefs.edit().apply {
            putString("user_id", userId)
            putLong("session_start", System.currentTimeMillis())
            apply()
        }
    }

    fun isSessionValid(): Boolean {
        val startTime = prefs.getLong("session_start", 0)
        val userId = prefs.getString("user_id", null)

        if (userId == null) return false

        val elapsed = System.currentTimeMillis() - startTime
        return elapsed < SESSION_TIMEOUT_MS
    }

    fun endSession() {
        prefs.edit().clear().apply()
    }

    fun getUserId(): String? {
        return prefs.getString("user_id", null)
    }
}