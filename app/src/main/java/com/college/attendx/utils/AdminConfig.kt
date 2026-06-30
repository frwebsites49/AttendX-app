package com.college.attendx.utils

import com.college.attendx.models.UserRole
object AdminConfig {

    // List of admin emails - Add all admin email addresses here
    val ADMIN_EMAILS = setOf(
        "lmnop@gmail.com",
        "frcollection3025@gmail.com",
    )

    // List of teacher emails (optional)
    val TEACHER_EMAILS = setOf(
        "abc@gmail.com",
        )

    // Check if email is admin
    fun isAdmin(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        return ADMIN_EMAILS.contains(email.lowercase().trim())
    }

    // Check if email is teacher
    fun isTeacher(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        return TEACHER_EMAILS.contains(email.lowercase().trim())
    }

    // Check if email is admin or teacher
    fun isStaff(email: String?): Boolean {
        return isAdmin(email) || isTeacher(email)
    }

    // Get role for email
    fun getRole(email: String?): UserRole {
        return when {
            isAdmin(email) -> UserRole.ADMIN
            isTeacher(email) -> UserRole.TEACHER
            else -> UserRole.STUDENT
        }
    }
}