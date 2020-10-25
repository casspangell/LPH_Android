package org.lovepeaceharmony.androidapp.utility

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * ThreadClearService
 * Created by Naveen Kumar M on 01/02/18.
 */
class ThreadClearService: Service() {

    override fun onBind(p0: Intent?): IBinder {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val intent1 = Intent(Constants.BROADCAST_CLEAR_THREAD)
        sendBroadcast(intent1)
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }
}