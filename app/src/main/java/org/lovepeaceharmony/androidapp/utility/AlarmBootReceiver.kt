package org.lovepeaceharmony.androidapp.utility

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import org.lovepeaceharmony.androidapp.model.AlarmModel

/**
 * AlarmBootReceiver
 * Created by Naveen Kumar M on 01/12/17.
 */

class AlarmBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        LPHLog.d("LPHBootReceiver Coming Here")
        val alarmModelList = AlarmModel.getAlarmList(context)
        for (alarmModel in alarmModelList) {
            AlarmScheduler.setReminder(context, AlarmReceiver::class.java, alarmModel)
        }
    }
}
