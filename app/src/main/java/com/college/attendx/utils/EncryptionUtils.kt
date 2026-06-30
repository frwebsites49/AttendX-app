package com.college.attendx.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionUtils {

    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val TAG = "EncryptionUtils"
    private const val PREFS_NAME = "secure_prefs"

    // Fixed encryption key (for demo purposes - in production use proper key management)
    private val FIXED_KEY = byteArrayOf(
        0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
        0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
        0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17,
        0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F
    )

    /**
     * Get EncryptedSharedPreferences for storing sensitive data
     */
    fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
        return try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

            EncryptedSharedPreferences.create(
                PREFS_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get encrypted preferences: ${e.message}", e)
            throw e
        }
    }

    /**
     * Store a sensitive value securely
     */
    fun storeSecureValue(context: Context, key: String, value: String) {
        try {
            val prefs = getEncryptedSharedPreferences(context)
            prefs.edit().putString(key, value).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store secure value: ${e.message}", e)
        }
    }

    /**
     * Retrieve a sensitive value securely
     */
    fun getSecureValue(context: Context, key: String): String? {
        return try {
            val prefs = getEncryptedSharedPreferences(context)
            prefs.getString(key, null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve secure value: ${e.message}", e)
            null
        }
    }

    /**
     * Remove a sensitive value
     */
    fun removeSecureValue(context: Context, key: String) {
        try {
            val prefs = getEncryptedSharedPreferences(context)
            prefs.edit().remove(key).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove secure value: ${e.message}", e)
            null
        }
    }

    /**
     * Encrypt data using AES/GCM
     */
    fun encryptData(data: String): String? {
        return try {
            if (data.isEmpty()) return null

            // Use AES/GCM with fixed key (for demo)
            val cipher = Cipher.getInstance(AES_MODE)
            val keySpec = SecretKeySpec(FIXED_KEY, "AES")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)

            val iv = cipher.iv
            val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

            // Combine IV and encrypted data
            val combined = ByteArray(iv.size + encryptedData.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedData, 0, combined, iv.size, encryptedData.size)

            Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed: ${e.message}", e)
            null
        }
    }

    /**
     * Decrypt data using AES/GCM
     */
    fun decryptData(encryptedData: String): String? {
        return try {
            if (encryptedData.isEmpty()) return null

            val combined = Base64.decode(encryptedData, Base64.DEFAULT)

            val ivSize = 12
            if (combined.size < ivSize) {
                return null
            }

            val iv = combined.sliceArray(0 until ivSize)
            val encrypted = combined.sliceArray(ivSize until combined.size)

            val cipher = Cipher.getInstance(AES_MODE)
            val keySpec = SecretKeySpec(FIXED_KEY, "AES")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec)

            val decryptedData = cipher.doFinal(encrypted)
            String(decryptedData, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed: ${e.message}", e)
            null
        }
    }

    /**
     * Helper to check if encryption is available
     */
    fun isEncryptionAvailable(context: Context): Boolean {
        return try {
            getEncryptedSharedPreferences(context)
            true
        } catch (e: Exception) {
            false
        }
    }
}