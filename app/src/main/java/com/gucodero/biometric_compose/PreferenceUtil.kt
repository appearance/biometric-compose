package com.gucodero.biometric_compose

import android.content.Context
import com.gucodero.biometric_compose.Extensions.toPreservedByteArray
import com.gucodero.biometric_compose.Extensions.toPreservedString


/**
 * Created by Mazhar on 16/07/23
 */
class PreferenceUtil(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    var username: String
        get() = sharedPreferences.getString(USERNAME, "") ?: ""
        set(value) = sharedPreferences.edit().putString(USERNAME, value).apply()

    var encryptedPassword: String
        get() = sharedPreferences.getString(ENCRYPTED_PASSWORD, "") ?: ""
        set(value) = sharedPreferences.edit().putString(ENCRYPTED_PASSWORD, value).apply()

    var cipherIv: String
        get() = sharedPreferences.getString(CIPHER_IV, "") ?: ""
        set(value) = sharedPreferences.edit().putString(CIPHER_IV, value).apply()

    var useFingerprint: Boolean
        get() = sharedPreferences.getBoolean(USE_FINGERPRINT, false)
        set(value) = sharedPreferences.edit().putBoolean(USE_FINGERPRINT, value).apply()

    var cipherText: ByteArray
        get() {
            val str = sharedPreferences.getString(CIPHER_TEXT, "") ?: ""
            return str.toPreservedByteArray
        }
        set(value) {
            val str = value.toPreservedString
            sharedPreferences.edit().putString(CIPHER_TEXT, str).apply()
        }

    var initializationVector: ByteArray
        get() {
            val str = sharedPreferences.getString(IV, "") ?: ""
            return str.toPreservedByteArray
        }
        set(value) {
            val str = value.toPreservedString
            sharedPreferences.edit().putString(IV, str).apply()
        }


    companion object {
        private const val USERNAME = "username"
        private const val ENCRYPTED_PASSWORD = "encrypted_password"
        private const val CIPHER_IV = "cipher_iv"
        private const val USE_FINGERPRINT = "use_fingerprint"
        private const val CIPHER_TEXT = "cipher_text"
        private const val IV = "initialization_vector"
    }
}