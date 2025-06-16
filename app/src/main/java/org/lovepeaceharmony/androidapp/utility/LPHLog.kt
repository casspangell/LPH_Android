package org.lovepeaceharmony.androidapp.utility

import android.util.Log

import org.lovepeaceharmony.androidapp.BuildConfig

/**
 * Created by Cass Pangell on 06/15/25.
 */

object LPHLog {
    private const val LOG_TAG = "LPH"

    fun e(message: String) {
        Log.e(LOG_TAG, message)
    }

    fun e(tag: String, message: String) {
        Log.e(tag, message)
    }

    fun d(message: String) {
        Log.d(LOG_TAG, message)
    }

    fun d(`object`: Any, message: String) {
        Log.d(LOG_TAG, message)
    }

    fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    fun v(tag: String, message: String) {
        Log.v(tag, message)
    }

    fun w(message: String) {
        Log.w(LOG_TAG, message)
    }

    fun i(message: String) {
        Log.i(LOG_TAG, message)
    }
}
