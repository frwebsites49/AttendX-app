package com.college.attendx.models

import com.google.firebase.firestore.PropertyName

/**
 * FIX (the real, final bug): Firestore documents in this project have a
 * field literally named "profileComplete" (visible in Firebase Console),
 * but this Kotlin property is named "isProfileComplete". The Firestore
 * Android SDK's reflection-based toObject() mapper does NOT reliably
 * strip/restore the "is" prefix on Boolean properties by itself - it
 * depends on how the document was originally written and which code
 * path wrote it. Since this property had no explicit @PropertyName,
 * deserialization silently fell back to the Kotlin default (false)
 * instead of throwing an error, which is why:
 *   - Firebase Console correctly showed profileComplete: true
 *   - The app's toObject(UserProfile::class.java) call still produced
 *     an object with isProfileComplete = false
 *   - Every check based on that property failed, even though the raw
 *     data was completely correct.
 *
 * @PropertyName explicitly tells Firestore which document field this
 * property reads from and writes to, removing the ambiguity entirely.
 */
data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val division: String = "",
    val group: String = "",
    val rollNumber: String = "",
    val mobileNumber: String = "",
    val academicYear: String = "",
    val course: String = "",
    val email: String = "",

    @get:PropertyName("profileComplete")
    @set:PropertyName("profileComplete")
    var isProfileComplete: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)