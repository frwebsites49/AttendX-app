package com.college.attendx.models

enum class UserRole {
    ADMIN,
    TEACHER,
    STUDENT
}

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.STUDENT,
    val division: String = "",
    val group: String = "",
    val rollNumber: String = "",
    val isProfileComplete: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)