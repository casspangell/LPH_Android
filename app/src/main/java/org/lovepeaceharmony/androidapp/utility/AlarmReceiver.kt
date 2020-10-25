package org.lovepeaceharmony.androidapp.utility

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.lovepeaceharmony.androidapp.ui.activity.MainActivity

/**
 * AlarmReceiver
 * Created by Naveen Kumar M on 01/12/17.
 */

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        LPHLog.d("AlarmReceiver onReceive called")
        val bundle = intent.extras
        if (bundle != null) {
            val pendingIntentId = bundle.getInt(Constants.BUNDLE_PENDING_INTENT_ID)
            val isAlarmOnce = bundle.getBoolean(Constants.BUNDLE_IS_ALARM_ONCE, false)
            val alarmId = bundle.getInt(Constants.BUNDLE_ALARM_ID, 0)
            LPHLog.d("AlarmReceiver alarmId : " + alarmId)
            val toneUri = bundle.getString(Constants.BUNDLE_TONE_URI)
            LPHLog.d("AlarmReceiver Pending IntentId : " + pendingIntentId)
            AlarmScheduler.showNotification(context, MainActivity::class.java, pendingIntentId, toneUri, isAlarmOnce, alarmId)
        }

    }
}
