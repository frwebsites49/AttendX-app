package com.college.attendx.models

data class AttendanceSession(
    val sessionId: String = "",
    val subject: String = "",
    val division: String = "",
    val group: String = "",
    val teacherId: String = "",
    val teacherName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radius: Int = 40,
    val createdAt: Long = System.currentTimeMillis(),
    val expiryTime: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val qrCode: String = "",
    val totalStudents: Int = 0,
    val presentCount: Int = 0
)