package org.lovepeaceharmony.androidapp.utility

import android.content.Context

object TimeTracker {
    private const val PREFS_NAME = "chant_time_prefs"
    private const val KEY_TOTAL_SECONDS = "total_chant_seconds"

    fun getTotalSeconds(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_TOTAL_SECONDS, 0L)
    }

    fun addSessionSeconds(context: Context, seconds: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val total = prefs.getLong(KEY_TOTAL_SECONDS, 0L) + seconds
        prefs.edit().putLong(KEY_TOTAL_SECONDS, total).apply()
    }

    fun setTotalSeconds(context: Context, seconds: Long) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putLong(KEY_TOTAL_SECONDS, seconds).apply()
    }
} 