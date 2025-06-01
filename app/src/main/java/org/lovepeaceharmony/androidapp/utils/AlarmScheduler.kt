import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*

class AlarmScheduler {

    fun scheduleReminder(context: Context, alarmModel: AlarmModel) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, alarmModel.hour)
        calendar.set(Calendar.MINUTE, alarmModel.minute)
        calendar.set(Calendar.SECOND, 0)

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(context.getString(R.string.alarm_id), alarmModel.id)
        intent.putExtra(context.getString(R.string.alarm_hour), alarmModel.hour)
        intent.putExtra(context.getString(R.string.alarm_minute), alarmModel.minute)
        intent.putExtra(context.getString(R.string.alarm_uri), alarmModel.uriString)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmModel.pendingIntentIds[0],
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun cancelReminder(context: Context, pendingIntentId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            pendingIntentId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
} 