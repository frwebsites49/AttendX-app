package com.college.attendx.repositories

import android.util.Log
import com.college.attendx.models.AttendanceRecord
import com.college.attendx.models.AttendanceSession
import com.college.attendx.models.UserProfile
import com.college.attendx.models.UserRole
import com.college.attendx.utils.AdminConfig
import com.college.attendx.utils.SecurityUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class FirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "FirebaseRepository"

    // ============= USER PROFILE =============

    suspend fun saveUserProfile(profile: UserProfile): Result<Boolean> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            // ✅ SECURITY: Sanitize input before saving
            val sanitizedProfile = profile.copy(
                name = SecurityUtils.sanitizeInput(profile.name),
                division = SecurityUtils.sanitizeInput(profile.division).uppercase(),
                group = SecurityUtils.sanitizeInput(profile.group).uppercase(),
                rollNumber = SecurityUtils.sanitizeInput(profile.rollNumber),
                mobileNumber = SecurityUtils.sanitizeInput(profile.mobileNumber)
            )

            val profileWithId = sanitizedProfile.copy(
                userId = userId,
                isProfileComplete = true,
                email = auth.currentUser?.email ?: "",
                updatedAt = System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(userId)
                .set(profileWithId)
                .await()

            Log.d(TAG, "Profile saved successfully for user: $userId")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving profile: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            val document = try {
                firestore.collection("users")
                    .document(userId)
                    .get(Source.SERVER)
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Server read failed, falling back to cache: ${e.message}")
                firestore.collection("users")
                    .document(userId)
                    .get(Source.CACHE)
                    .await()
            }

            if (document.exists()) {
                val profile = document.toObject(UserProfile::class.java)
                if (profile != null) {
                    Result.success(profile)
                } else {
                    Result.failure(Exception("Profile data is null"))
                }
            } else {
                Result.failure(Exception("Profile not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting profile: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun isProfileComplete(): Boolean {
        return try {
            val result = getUserProfile()
            result.isSuccess && result.getOrNull()?.isProfileComplete == true
        } catch (e: Exception) {
            false
        }
    }

    // ============= ROLE MANAGEMENT =============

    suspend fun getUserRole(): UserRole {
        return try {
            val currentUser = auth.currentUser
            val email = currentUser?.email
            val userId = currentUser?.uid

            // ✅ SECURITY: Check if email is verified before assigning roles
            if (currentUser != null && !currentUser.isEmailVerified) {
                Log.w(TAG, "User email not verified - defaulting to STUDENT")
                return UserRole.STUDENT
            }

            if (email != null) {
                val role = AdminConfig.getRole(email)
                if (role != UserRole.STUDENT) {
                    return role
                }
            }

            if (userId != null) {
                val adminDoc = firestore.collection("admins")
                    .document(userId)
                    .get()
                    .await()

                if (adminDoc.exists()) {
                    return UserRole.ADMIN
                }
            }

            UserRole.STUDENT
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user role: ${e.message}")
            UserRole.STUDENT
        }
    }

    // ============= SESSION MANAGEMENT (ADMIN) =============

    suspend fun createSession(session: AttendanceSession): Result<String> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            // ✅ SECURITY: Only admins/teachers can create sessions
            val role = getUserRole()
            if (role != UserRole.ADMIN && role != UserRole.TEACHER) {
                return Result.failure(Exception("Unauthorized: Only admins can create sessions"))
            }

            val userProfile = getUserProfile().getOrNull()

            // ✅ SECURITY: Validate session data
            if (session.subject.isBlank() || session.division.isBlank() || session.group.isBlank()) {
                return Result.failure(Exception("Invalid session data"))
            }

            val sessionRef = firestore.collection("sessions").document()
            val sessionWithId = session.copy(
                sessionId = sessionRef.id,
                teacherId = userId,
                teacherName = userProfile?.name ?: auth.currentUser?.email ?: "Teacher",
                createdAt = System.currentTimeMillis(),
                isActive = true,
                // ✅ SECURITY: Sanitize input
                subject = SecurityUtils.sanitizeInput(session.subject),
                division = SecurityUtils.sanitizeInput(session.division).uppercase(),
                group = SecurityUtils.sanitizeInput(session.group).uppercase()
            )

            sessionRef.set(sessionWithId).await()

            Log.d(TAG, "Session created: ${sessionWithId.sessionId}")
            Result.success(sessionWithId.sessionId)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating session: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getSession(sessionId: String): Result<AttendanceSession> {
        return try {
            // ✅ SECURITY: Validate session ID
            if (!SecurityUtils.isValidSessionId(sessionId)) {
                return Result.failure(Exception("Invalid session ID format"))
            }

            val document = firestore.collection("sessions")
                .document(sessionId)
                .get()
                .await()

            if (document.exists()) {
                val session = document.toObject(AttendanceSession::class.java)
                if (session != null) {
                    // ✅ SECURITY: Check if session is expired
                    if (System.currentTimeMillis() > session.expiryTime && session.isActive) {
                        // Auto-close expired sessions
                        closeSession(sessionId)
                        Result.success(session.copy(isActive = false))
                    } else {
                        Result.success(session)
                    }
                } else {
                    Result.failure(Exception("Session data is null"))
                }
            } else {
                Result.failure(Exception("Session not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting session: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun closeSession(sessionId: String): Result<Boolean> {
        return try {
            // ✅ SECURITY: Only admins/teachers can close sessions
            val role = getUserRole()
            if (role != UserRole.ADMIN && role != UserRole.TEACHER) {
                return Result.failure(Exception("Unauthorized: Only admins can close sessions"))
            }

            firestore.collection("sessions")
                .document(sessionId)
                .update("isActive", false)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error closing session: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getLiveCount(sessionId: String): Result<Int> {
        return try {
            val query = firestore.collection("attendance")
                .whereEqualTo("sessionId", sessionId)
                .get()
                .await()
            Result.success(query.size())
        } catch (e: Exception) {
            Log.e(TAG, "Error getting live count: ${e.message}")
            Result.failure(e)
        }
    }

    fun listenToAttendanceCount(
        sessionId: String,
        onUpdate: (Int) -> Unit
    ): ListenerRegistration {
        return firestore.collection("attendance")
            .whereEqualTo("sessionId", sessionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Live count listener error: ${error.message}")
                    return@addSnapshotListener
                }
                onUpdate(snapshot?.size() ?: 0)
            }
    }

    // ============= ATTENDANCE (STUDENT) =============

    suspend fun markAttendance(
        sessionId: String,
        studentId: String,
        studentName: String,
        rollNumber: String,
        division: String,
        group: String,
        latitude: Double,
        longitude: Double,
        distance: Double
    ): Result<Boolean> {
        return try {
            // ✅ SECURITY: Validate input
            if (!SecurityUtils.isValidSessionId(sessionId)) {
                return Result.failure(Exception("Invalid session ID"))
            }

            if (!SecurityUtils.isValidRollNumber(rollNumber)) {
                return Result.failure(Exception("Invalid roll number format"))
            }

            // ✅ SECURITY: Rate limiting - check if student can mark attendance
            if (!canMarkAttendance(studentId)) {
                return Result.failure(Exception("You have already marked attendance recently. Please wait before trying again."))
            }

            val sessionResult = getSession(sessionId)
            if (sessionResult.isFailure) {
                return Result.failure(Exception("This QR code does not match any active session."))
            }

            val session = sessionResult.getOrNull()
                ?: return Result.failure(Exception("Session data is null"))

            if (!session.isActive) {
                return Result.failure(Exception("This attendance session has expired. Ask your teacher to start a new one."))
            }

            if (System.currentTimeMillis() > session.expiryTime) {
                return Result.failure(Exception("This attendance session has expired. Ask your teacher to start a new one."))
            }

            if (division != session.division || group != session.group) {
                return Result.failure(
                    Exception(
                        "This session is for Division ${session.division} - Group ${session.group}. " +
                                "Your profile is set to Division $division - Group $group."
                    )
                )
            }

            if (distance > session.radius) {
                return Result.failure(
                    Exception(
                        "You appear to be ${distance.toInt()}m from the classroom. " +
                                "You must be within ${session.radius}m to mark attendance."
                    )
                )
            }

            // ✅ SECURITY: Check for duplicate attendance
            val existingQuery = firestore.collection("attendance")
                .whereEqualTo("sessionId", sessionId)
                .whereEqualTo("studentId", studentId)
                .get()
                .await()

            if (!existingQuery.isEmpty) {
                return Result.failure(Exception("You have already marked attendance for this session."))
            }

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val now = Date()

            val record = AttendanceRecord(
                id = firestore.collection("attendance").document().id,
                sessionId = sessionId,
                studentId = studentId,
                studentName = SecurityUtils.sanitizeInput(studentName),
                rollNumber = SecurityUtils.sanitizeInput(rollNumber),
                division = SecurityUtils.sanitizeInput(division).uppercase(),
                group = SecurityUtils.sanitizeInput(group).uppercase(),
                date = dateFormat.format(now),
                time = timeFormat.format(now),
                timestamp = System.currentTimeMillis(),
                latitude = latitude,
                longitude = longitude,
                distance = distance,
                isVerified = distance <= session.radius
            )

            firestore.collection("attendance")
                .document(record.id)
                .set(record)
                .await()

            Log.d(TAG, "Attendance marked for student: $studentId")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking attendance: ${e.message}")
            Result.failure(e)
        }
    }

    // ✅ SECURITY: Rate limiting for attendance
    suspend fun canMarkAttendance(studentId: String): Boolean {
        return try {
            val oneHourAgo = System.currentTimeMillis() - 3600000 // 1 hour
            val query = firestore.collection("attendance")
                .whereEqualTo("studentId", studentId)
                .whereGreaterThan("timestamp", oneHourAgo)
                .get()
                .await()

            // Max 1 attendance per hour
            query.size() < 1
        } catch (e: Exception) {
            Log.e(TAG, "Error checking rate limit: ${e.message}")
            true // If check fails, allow (better UX)
        }
    }

    suspend fun getAttendanceBySession(sessionId: String): Result<List<AttendanceRecord>> {
        return try {
            val query = firestore.collection("attendance")
                .whereEqualTo("sessionId", sessionId)
                .get()
                .await()

            val records = query.documents
                .mapNotNull { it.toObject(AttendanceRecord::class.java) }
                .sortedBy { it.rollNumber }
            Result.success(records)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting attendance: ${e.message}")
            Result.failure(e)
        }
    }
}