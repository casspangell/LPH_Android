package org.lovepeaceharmony.android.app

import android.app.Application
import com.olayg.network.ConnectivityStateHolder.initNetworkMonitorConfig
import dagger.hilt.android.HiltAndroidApp
import org.lovepeaceharmony.android.app.utility.Helper
import org.lovepeaceharmony.android.app.utility.LPHLog

/**
 * LPHApplication
 * Created by Naveen Kumar M on 13/12/17.
 */

@HiltAndroidApp
class LPHApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LPHLog.d("LPHApplication OnCreate")
        initNetworkMonitorConfig()
        Helper.setPlayList(this, "songs", ".mp3")
    }

}
