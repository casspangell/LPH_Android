package org.lovepeaceharmony.androidapp.utility

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Created by Cass Pangell on 06/15/25.
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