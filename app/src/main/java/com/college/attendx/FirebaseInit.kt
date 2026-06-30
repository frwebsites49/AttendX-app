// app/src/main/java/com/college/attendx/FirebaseInit.kt
package com.college.attendx

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class FirebaseInit : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this)
            Log.d("FirebaseInit", "Firebase initialized successfully!")

            // Configure Firestore with persistence
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

            FirebaseFirestore.getInstance().firestoreSettings = settings

            Log.d("FirebaseInit", "Firestore configured with persistence")

        } catch (e: Exception) {
            Log.e("FirebaseInit", "Firebase initialization failed: ${e.message}", e)
        }
    }
}