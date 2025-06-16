package org.lovepeaceharmony.androidapp.utility

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Created by Cass Pangell on 06/15/25.
 */

class VolumeChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != null && intent.action == "android.media.VOLUME_CHANGED_ACTION") {
            val newVolume = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", 0)
            val oldVolume = intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", 0)
            if (newVolume != oldVolume) {
                val intent1 = Intent(Constants.BROADCAST_RECEIVER_VOLUME)
                context.sendBroadcast(intent1)
            }
        }
    }
}
