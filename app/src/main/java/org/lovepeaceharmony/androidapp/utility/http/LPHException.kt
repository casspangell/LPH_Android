package org.lovepeaceharmony.androidapp.utility.http

import android.util.Log

/**
 * Created by Cass Pangell on 06/15/25.
 */
class LPHException : Throwable() {

    private var exception: Throwable? = null
    var responseCode: Int = 0
    var responseMessage: String? = null
    private var tag = "LPH"

    override fun printStackTrace() {
        if (null != exception) {
            Log.e(tag, exception!!.toString())
        } else {
            Log.e(tag, "No network connection.")
        }
    }

}