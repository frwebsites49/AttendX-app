package com.college.attendx.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object AdminHelper {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "AdminHelper"

    // Call this function when you want to add an admin
    suspend fun addAdmin(email: String, name: String = ""): Result<Boolean> {
        return try {
            // Find user by email
            // Note: This requires Admin SDK, so we'll use a different approach
            // Instead, we'll add using UID if already known

            // For production, use Firebase Admin SDK or Cloud Function
            // This is a simplified version
            Result.failure(Exception("Use Firebase Console to add admins"))
        } catch (e: Exception) {
            Log.e(TAG, "Error adding admin: ${e.message}")
            Result.failure(e)
        }
    }

    // Check if current user is admin
    suspend fun isCurrentUserAdmin(): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false
            val document = firestore.collection("admins")
                .document(userId)
                .get()
                .await()
            document.exists()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking admin: ${e.message}")
            false
        }
    }

    // Get all admins (for debugging)
    suspend fun getAllAdmins(): List<String> {
        return try {
            val snapshot = firestore.collection("admins")
                .get()
                .await()
            snapshot.documents.map { it.id }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting admins: ${e.message}")
            emptyList()
        }
    }
}