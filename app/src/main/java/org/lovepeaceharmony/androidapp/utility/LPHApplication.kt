package org.lovepeaceharmony.androidapp.utility

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Created by Cass Pangell on 06/15/25.
 */

@HiltAndroidApp
class LPHApplication : Application() {

    var isFromProfileFbLogin: Boolean = false
    var isFromMileStoneFbLogin: Boolean = false
    var isFromFavoriteFbLogin: Boolean = false

    override fun onCreate() {
        super.onCreate()
        LPHLog.d("LPHApplication OnCreate")
        
        // Initialize song list
        Helper.setPlayList(this, "songs", ".mp3")
        
        // Set default song selection for new users
        Helper.setDefaultSongSelection(this)
    }
}
