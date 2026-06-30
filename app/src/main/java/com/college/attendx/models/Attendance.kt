package com.college.attendx.models

data class AttendanceRecord(
    val id: String = "",
    val sessionId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val rollNumber: String = "",
    val division: String = "",
    val group: String = "",
    val date: String = "",
    val time: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val distance: Double = 0.0,
    val isVerified: Boolean = true,
    val deviceId: String = ""
)