package org.lovepeaceharmony.androidapp.utility

import android.util.Log

import org.lovepeaceharmony.androidapp.BuildConfig

/**
 * LPHLog
 * Created by Naveen Kumar M on 04/12/17.
 */

object LPHLog {
    private val LOG_TAG = "LovePeaceHarmony"

    fun e(message: String) {
        Log.e(LOG_TAG, message)
    }

    fun e(tag: String, message: String) {
        Log.e(tag, message)
    }


    fun d(message: String) {
        if (BuildConfig.DEBUG)
            Log.d(LOG_TAG, message)
    }

    fun d(`object`: Any, message: String) {
        if (BuildConfig.DEBUG)
            Log.d(LOG_TAG, `object`.javaClass.simpleName + ": " + message)
    }

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG)
            Log.d(tag, message)
    }

    fun v(tag: String, message: String) {
        if (BuildConfig.DEBUG)
            Log.v(tag, message)
    }

    fun w(message: String) {
        Log.w(LOG_TAG, message)
    }

    fun i(message: String) {
        Log.d(LOG_TAG, message)
    }
}
