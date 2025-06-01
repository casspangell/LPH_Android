package org.lovepeaceharmony.androidapp.utility

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import java.util.*
import kotlin.collections.LinkedHashMap

object LanguageManager {
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    // Supported languages: code to display name (native + English)
    val supportedLanguages: LinkedHashMap<String, String> = linkedMapOf(
        "en" to "English",
        "es" to "Español (Spanish)",
        "fr" to "Français (French)",
        "de" to "Deutsch (German)"
        // Add more as needed
    )

    fun getCurrentLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, getSystemLanguage()) ?: getSystemLanguage()
    }

    fun setLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
    }

    fun getAvailableLanguages(): List<Pair<String, String>> {
        return supportedLanguages.entries.map { it.key to it.value }
    }

    fun getSystemLanguage(): String {
        return Locale.getDefault().language
    }

    fun applyLanguage(context: Context): Context {
        val langCode = getCurrentLanguage(context)
        return updateResources(context, langCode)
    }

    private fun updateResources(context: Context, language: String): Context {
        var ctx = context
        val locale = Locale(language)
        Locale.setDefault(locale)
        val res = ctx.resources
        val config = Configuration(res.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            ctx = ctx.createConfigurationContext(config)
        } else {
            config.locale = locale
            res.updateConfiguration(config, res.displayMetrics)
        }
        return ctx
    }

    fun wrapContext(activity: Activity) {
        val langCode = getCurrentLanguage(activity)
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        activity.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                applicationContext.createConfigurationContext(config)
            } else {
                resources.updateConfiguration(config, resources.displayMetrics)
            }
        }
    }
} 