package org.lovepeaceharmony.androidapp.utility

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.util.Log
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.model.AlarmModel
import java.util.*


/**
 * AlarmScheduler
 * Created by Naveen Kumar M on 01/12/17.
 */

object AlarmScheduler {

    fun setReminder(context: Context, cls: Class<*>, alarmModel: AlarmModel) {

        val calendar = Calendar.getInstance()
        LPHLog.d("PendingIntentSize : " + alarmModel.pendingIntentIds.size)
        for (i in alarmModel.pendingIntentIds.indices) {
            val pendingIntentId = alarmModel.pendingIntentIds[i]
            val repeat = alarmModel.repeats[i]
            val setCalendar = Calendar.getInstance()
            setCalendar.set(Calendar.HOUR_OF_DAY, alarmModel.hour)
            setCalendar.set(Calendar.MINUTE, alarmModel.minute)
            setCalendar.set(Calendar.SECOND, 0)

            LPHLog.d("Scheduled Hour : " + alarmModel.hour)
            LPHLog.d("Scheduled Minute: " + alarmModel.minute)

            // cancel already scheduled reminders
            cancelReminder(context, cls, pendingIntentId)

            val intent1 = Intent(context, cls)
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val receiver = AlarmReceiver()
                val intentFilter = IntentFilter(Constants.ALARM_RECEIVER_INTENT_TRIGGER)
                context.registerReceiver(receiver, intentFilter)
                intent1 = Intent(Constants.ALARM_RECEIVER_INTENT_TRIGGER)
                intent1.setClass(context, AlarmReceiver::class.java)
            }*/

            intent1.putExtra(Constants.BUNDLE_PENDING_INTENT_ID, pendingIntentId)
            intent1.putExtra(Constants.BUNDLE_TONE_URI, alarmModel.uriString)
            intent1.putExtra(Constants.BUNDLE_ALARM_ID, alarmModel.id)
            intent1.putExtra(Constants.BUNDLE_IS_ALARM_ONCE, repeat == 8)
            val pendingIntent = PendingIntent.getBroadcast(context, pendingIntentId, intent1, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
            LPHLog.d("Repeat : " + repeat)
            when {
                repeat == 8 -> { //Checking alarm once
                    if (setCalendar.before(calendar))
                        setCalendar.add(Calendar.DATE, 1)

                    alarmManager.set(AlarmManager.RTC_WAKEUP, setCalendar.timeInMillis, pendingIntent)
                }
                repeat != 0 -> { //setting for particular day
                    setCalendar.set(Calendar.DAY_OF_WEEK, repeat)
                    if (setCalendar.timeInMillis < System.currentTimeMillis()) {
                        setCalendar.add(Calendar.DAY_OF_YEAR, 7)
                    }
                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, setCalendar.timeInMillis, AlarmManager.INTERVAL_DAY * 7, pendingIntent)
                }
                else -> { //Setting for every day
                    LPHLog.d("DayOfWeek Today : " + setCalendar.get(Calendar.DAY_OF_WEEK))
                    //                setCalendar.set(Calendar.DAY_OF_WEEK, (i + 1));
                    //                if(setCalendar.getTimeInMillis() < System.currentTimeMillis()) {
                    //                    setCalendar.add(Calendar.DAY_OF_YEAR, 7);
                    //                }
                    if (Calendar.getInstance().after(setCalendar)) {
                        LPHLog.d("Hey, ", "Added a day")
                        setCalendar.add(Calendar.DATE, 1)
                    }

                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, setCalendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
                }
            }

        }

        // Enable a receiver

        //        ComponentName receiver = new ComponentName(context, cls);
        //        PackageManager pm = context.getPackageManager();
        //
        //        pm.setComponentEnabledSetting(receiver,
        //                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        //                PackageManager.DONT_KILL_APP);

    }

    fun cancelReminder(context: Context, cls: Class<*>, pendingIntentId: Int) {
        Log.d("PendingIntentId", ": " + pendingIntentId)
        // Disable a receiver
        /*ComponentName receiver = new ComponentName(context, cls);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);*/

        val intent1 = Intent(context, cls)
        val pendingIntent = PendingIntent.getBroadcast(context, pendingIntentId, intent1, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }


    internal fun showNotification(context: Context, cls: Class<*>, pendingIntentId: Int, toneUri: String?, isAlarmOnce: Boolean, alarmId: Int) {

        val uri: Uri
        if (toneUri == null || RingtoneManager.getRingtone(context, Uri.parse(toneUri)) == null) {
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, uri)
            val title = ringtone.getTitle(context)
            LPHLog.d("AlarmReceiver Notification Name : " + title)
        } else {
            uri = Uri.parse(toneUri)
        }

        val notificationIntent = Intent(context, cls)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(cls)
        stackBuilder.addNextIntent(notificationIntent)

        val pendingIntent = stackBuilder.getPendingIntent(pendingIntentId, PendingIntent.FLAG_UPDATE_CURRENT)

        val channelId = "LPH_CHANNEL_" + pendingIntent!!
        val builder = NotificationCompat.Builder(context, channelId)
        val name = context.getString(R.string.app_name)

        LPHLog.d("AlarmReceiver showNotification alarmId : " + alarmId)
        val notification = builder.setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.reminder_for_chant))
                .setAutoCancel(true)
                .setSound(uri)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setChannelId(channelId)
                .setContentIntent(pendingIntent).build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(channelId, name, importance)
            notificationManager.createNotificationChannel(mChannel)
        }
        notificationManager.notify(pendingIntentId, notification)

        if (isAlarmOnce) {
            AlarmModel.updateIsEnabled(context, alarmId, false)
            val intent1 = Intent(Constants.BROADCAST_REMINDERS)
            context.sendBroadcast(intent1)
        }


    }

    private val mRecentNewsBroadCast = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
//            restartLoader()

        }
    }
}
