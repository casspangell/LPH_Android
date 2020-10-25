package org.lovepeaceharmony.androidapp.utility

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import io.fabric.sdk.android.Fabric
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
        if(!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
            Fabric.with(this, Answers())
        }
        LPHLog.d("LPHApplication OnCreate")
        Helper.setPlayList(this, "songs", ".mp3")
    }

}
