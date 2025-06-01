package org.lovepeaceharmony.androidapp.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object AuthPrefs {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER_ID = "user_id"

    fun getEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveAuthData(context: Context, token: String, userId: String) {
        getEncryptedPrefs(context).edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_USER_ID, userId)
            apply()
        }
    }

    fun getToken(context: Context): String? =
        getEncryptedPrefs(context).getString(KEY_TOKEN, null)

    fun getUserId(context: Context): String? =
        getEncryptedPrefs(context).getString(KEY_USER_ID, null)

    fun clear(context: Context) {
        getEncryptedPrefs(context).edit().clear().apply()
    }
} 