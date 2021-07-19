package org.lovepeaceharmony.androidapp.utility

import android.app.Application
import org.lovepeaceharmony.androidapp.BuildConfig


/**
 * LPHApplication
 * Created by Naveen Kumar M on 13/12/17.
 */

class LPHApplication : Application() {

    var isFromProfileFbLogin: Boolean = false
    var isFromMileStoneFbLogin: Boolean = false
    var isFromFavoriteFbLogin: Boolean = false

    override fun onCreate() {
        super.onCreate()
        LPHLog.d("LPHApplication OnCreate")
        Helper.setPlayList(this, "songs", ".mp3")
    }

}
