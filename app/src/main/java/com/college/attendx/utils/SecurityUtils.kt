package com.college.attendx.utils

import com.college.attendx.models.UserProfile
import java.util.regex.Pattern
import kotlin.random.Random

object SecurityUtils {

    // ============= EMAIL VALIDATION =============

    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val emailRegex = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
        return emailRegex.matcher(email).matches()
    }

    // ============= PASSWORD STRENGTH =============

    fun checkPasswordStrength(password: String): PasswordStrengthResult {
        return when {
            password.length < 8 -> PasswordStrengthResult(
                strength = PasswordStrength.WEAK,
                message = "Password must be at least 8 characters"
            )
            !password.any { it.isDigit() } -> PasswordStrengthResult(
                strength = PasswordStrength.WEAK,
                message = "Password must contain at least one number"
            )
            !password.any { it.isUpperCase() } -> PasswordStrengthResult(
                strength = PasswordStrength.MEDIUM,
                message = "Password must contain at least one uppercase letter"
            )
            !password.any { it.isLowerCase() } -> PasswordStrengthResult(
                strength = PasswordStrength.MEDIUM,
                message = "Password must contain at least one lowercase letter"
            )
            !password.any { it in "!@#$%^&*()_+-=[]{};':\"\\|,.<>/?" } ->
                PasswordStrengthResult(
                    strength = PasswordStrength.STRONG,
                    message = "Password is strong - add special characters for extra security"
                )
            else -> PasswordStrengthResult(
                strength = PasswordStrength.VERY_STRONG,
                message = "Password is very strong"
            )
        }
    }

    // ============= INPUT SANITIZATION =============

    fun sanitizeInput(input: String): String {
        return input
            .trim()
            .replace(Regex("[<>\"'()&;]"), "") // Remove dangerous characters
    }

    // ============= ROLL NUMBER VALIDATION =============

    fun isValidRollNumber(rollNumber: String): Boolean {
        return rollNumber.matches(Regex("^[A-Za-z0-9]{2,10}$"))
    }

    // ============= DIVISION VALIDATION =============

    fun isValidDivision(division: String): Boolean {
        return division.matches(Regex("^[A-Z]{1,2}$"))
    }

    // ============= GROUP VALIDATION =============

    fun isValidGroup(group: String): Boolean {
        return group.matches(Regex("^[A-Z0-9]{1,4}$"))
    }

    // ============= SESSION ID VALIDATION =============

    fun isValidSessionId(sessionId: String): Boolean {
        return sessionId.matches(Regex("^[A-Za-z0-9_-]{5,50}$"))
    }

    // ============= USER PROFILE VALIDATION =============

    fun validateUserProfile(profile: UserProfile): ValidationResult {
        return when {
            profile.name.isBlank() -> ValidationResult(false, "Name is required")
            profile.name.length < 2 -> ValidationResult(false, "Name must be at least 2 characters")
            profile.name.length > 50 -> ValidationResult(false, "Name must be less than 50 characters")
            profile.rollNumber.isBlank() -> ValidationResult(false, "Roll Number is required")
            !isValidRollNumber(profile.rollNumber) -> ValidationResult(false, "Invalid Roll Number format (2-10 alphanumeric)")
            profile.division.isBlank() -> ValidationResult(false, "Division is required")
            !isValidDivision(profile.division) -> ValidationResult(false, "Invalid Division format (e.g., A, B, C)")
            profile.group.isBlank() -> ValidationResult(false, "Group is required")
            !isValidGroup(profile.group) -> ValidationResult(false, "Invalid Group format (e.g., G1, G2)")
            else -> ValidationResult(true, "Valid")
        }
    }

    // ============= SECURE ID GENERATION =============

    fun generateSecureId(): String {
        val timestamp = System.currentTimeMillis().toString(36)
        val random = Random.nextInt(100000, 999999).toString(36)
        return "sess_${timestamp}_${random}"
    }

    fun generateShortId(): String {
        val timestamp = System.currentTimeMillis().toString(36).takeLast(6)
        val random = Random.nextInt(1000, 9999).toString(36)
        return "${timestamp}${random}"
    }

    // ============= PASSWORD STRENGTH HELPER FUNCTIONS =============

    // ✅ FIXED: Explicitly cast to Int using .toInt()
    fun getPasswordStrengthColor(strength: PasswordStrength): Int {
        return when (strength) {
            PasswordStrength.WEAK -> 0xFFFF6B6B.toInt()    // Red
            PasswordStrength.MEDIUM -> 0xFFFFD93D.toInt()  // Yellow
            PasswordStrength.STRONG -> 0xFF6BCB77.toInt()  // Green
            PasswordStrength.VERY_STRONG -> 0xFF4D96FF.toInt() // Blue
        }
    }

    // ✅ Alternative: Using Android Color class (if you want to return Color)
    // fun getPasswordStrengthColor(strength: PasswordStrength): androidx.compose.ui.graphics.Color {
    //     return when (strength) {
    //         PasswordStrength.WEAK -> androidx.compose.ui.graphics.Color(0xFFFF6B6B)
    //         PasswordStrength.MEDIUM -> androidx.compose.ui.graphics.Color(0xFFFFD93D)
    //         PasswordStrength.STRONG -> androidx.compose.ui.graphics.Color(0xFF6BCB77)
    //         PasswordStrength.VERY_STRONG -> androidx.compose.ui.graphics.Color(0xFF4D96FF)
    //     }
    // }

    fun getPasswordStrengthText(strength: PasswordStrength): String {
        return when (strength) {
            PasswordStrength.WEAK -> "Weak"
            PasswordStrength.MEDIUM -> "Medium"
            PasswordStrength.STRONG -> "Strong"
            PasswordStrength.VERY_STRONG -> "Very Strong"
        }
    }

    // ============= RATE LIMITING =============

    fun canPerformAction(lastActionTime: Long, cooldownMinutes: Int = 5): Boolean {
        val cooldownMillis = cooldownMinutes * 60 * 1000L
        return System.currentTimeMillis() - lastActionTime > cooldownMillis
    }

    fun getRemainingCooldownSeconds(lastActionTime: Long, cooldownMinutes: Int = 5): Long {
        val cooldownMillis = cooldownMinutes * 60 * 1000L
        val remaining = cooldownMillis - (System.currentTimeMillis() - lastActionTime)
        return if (remaining > 0) remaining / 1000 else 0
    }
}

// ============= DATA CLASSES =============

data class ValidationResult(
    val isValid: Boolean,
    val message: String
)

data class PasswordStrengthResult(
    val strength: PasswordStrength,
    val message: String
)

// ============= ENUM =============

enum class PasswordStrength {
    WEAK,
    MEDIUM,
    STRONG,
    VERY_STRONG
}