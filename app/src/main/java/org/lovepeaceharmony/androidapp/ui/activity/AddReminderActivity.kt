package org.lovepeaceharmony.androidapp.ui.activity

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.model.AlarmModel
import org.lovepeaceharmony.androidapp.model.Repeat
import org.lovepeaceharmony.androidapp.ui.fragment.RepeatDialog
import org.lovepeaceharmony.androidapp.utility.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * AddReminderActivity
 * Created by Naveen Kumar M on 29/11/17.
 */

class AddReminderActivity : AppCompatActivity() {
    private var context: Context? = null
    private var repeatList: List<Repeat>? = null
    private var currentToneUri: Uri? = null
    private var isFromEdit: Boolean = false
    private var savedAlarmModel: AlarmModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        setContentView(R.layout.activity_add_reminder)
        handleIntent()
        initView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleIntent() {
        val bundle = intent.extras
        if (bundle != null) {
            isFromEdit = bundle.getBoolean(Constants.BUNDLE_IS_FROM_EDIT, false)
            val savedAlarmId = bundle.getInt(Constants.BUNDLE_ALARM_ID, 0)
            savedAlarmModel = AlarmModel.getSavedAlarm(context!!, savedAlarmId)
            if (savedAlarmModel != null) {
                LPHLog.d("Time : " + savedAlarmModel!!.uriString)
            }
        }
    }

    private fun initView() {
        val tvTime = findViewById<TextView>(R.id.tv_time)
        val tvAMPM = findViewById<TextView>(R.id.tv_am_pm)
        val tvRepeatText = findViewById<TextView>(R.id.tv_repeat_text)
        val tvTitle = findViewById<TextView>(R.id.tv_title)
        val tvRingTone = findViewById<TextView>(R.id.tv_ringtone)
        val ivRingtone = findViewById<ImageView>(R.id.iv_ringtone)
        val ivRepeat = findViewById<ImageView>(R.id.iv_repeat)
        val btnCancel = findViewById<Button>(R.id.btn_cancel)
        val btnSave = findViewById<Button>(R.id.btn_save)

        currentToneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        repeatList = generateList()

        if (isFromEdit && savedAlarmModel != null) {
            tvTitle.text = context!!.getString(R.string.edit_reminder)
            val c2 = Calendar.getInstance()
            c2.set(c2.get(Calendar.YEAR), c2.get(Calendar.MONTH), c2.get(Calendar.DATE), savedAlarmModel!!.hour, savedAlarmModel!!.minute)
            val mTimeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
            tvTime.text = mTimeFormat.format(c2.time)
            tvAMPM.text = savedAlarmModel!!.amPm
            currentToneUri = Uri.parse(savedAlarmModel!!.uriString)
            if (currentToneUri == null || RingtoneManager.getRingtone(context, currentToneUri) == null)
                currentToneUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION)

            val repeatText = Helper.getRepeatText(context!!, savedAlarmModel!!.repeats)
            tvRepeatText.text = repeatText

            repeatList = getSavedRepeats(savedAlarmModel!!)

            val tvDeleteReminder = findViewById<TextView>(R.id.tv_delete_reminder)
            tvDeleteReminder.visibility = View.VISIBLE
            tvDeleteReminder.setOnClickListener {
                Toast.makeText(context!!, context!!.getString(R.string.reminder_deleted_successfully), Toast.LENGTH_SHORT).show()
                for (pendingIntentId in savedAlarmModel!!.pendingIntentIds)
                    AlarmScheduler.cancelReminder(context!!, AlarmReceiver::class.java, pendingIntentId)
                AlarmModel.deleteAlarmById(context!!, savedAlarmModel!!.id)
                setResult(RESULT_OK)
                onBackPressed()
            }
        }

        val ringtone = RingtoneManager.getRingtone(context!!, currentToneUri)
        val title = ringtone.getTitle(context!!)
        tvRingTone.text = title

        btnCancel.setOnClickListener { onBackPressed() }

        tvTime.setOnClickListener {
            val c = Calendar.getInstance()
            if (tvTime.text != context!!.getString(R.string.default_time_template)) {
                val times = tvTime.text.toString().split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                var hr = Integer.parseInt(times[0])
                val mm = Integer.parseInt(times[1])
                if (tvAMPM.text.toString() == "PM" && hr != 12) {
                    hr += 12
                } else if (tvAMPM.text.toString() == "AM" && hr == 12) {
                    hr = 0
                }
                c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), hr, mm)
            }

            val mHour = c.get(Calendar.HOUR_OF_DAY)
            val mMinute = c.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(context!!, TimePickerDialog.OnTimeSetListener { timePicker, hourOfDay, minute ->
                val amPm: String
                amPm = if (hourOfDay >= 12) {
                    "PM"
                } else {
                    "AM"
                }
                tvAMPM.text = amPm
                try {
                    val c2 = Calendar.getInstance()
                    c2.set(c2.get(Calendar.YEAR), c2.get(Calendar.MONTH), c2.get(Calendar.DATE), hourOfDay, minute)
                    val mTimeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
                    tvTime.text = mTimeFormat.format(c2.time)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, mHour, mMinute, false)
            timePickerDialog.show()
        }

        ivRepeat.setOnClickListener {
            val repeatDialog = RepeatDialog()
            repeatDialog.setRepeatDialog(object : OnRepeatSelect {
                override fun updateList(list: List<Repeat>) {
                    repeatList = list
                    val repeatText = getRepeatText(repeatList)
                    tvRepeatText.text = repeatText

                }
            }, repeatList!!)
            repeatDialog.isCancelable = true
            val fm = supportFragmentManager
            repeatDialog.show(fm, "RepeatDialog")
        }

        btnSave.setOnClickListener {
            val time = tvTime.text.toString().trim { it <= ' ' }
            if (time.equals(context!!.getString(R.string.default_time_template), ignoreCase = true)) {
                Helper.showAlert(context!!, context!!.getString(R.string.please_select_time))
            } else {
                if (isFromEdit && savedAlarmModel != null) {
                    for (pendingIntentId in savedAlarmModel!!.pendingIntentIds)
                        AlarmScheduler.cancelReminder(context!!, AlarmReceiver::class.java, pendingIntentId)
                }
                if (tvRepeatText.text.toString().isEmpty() || tvRepeatText.text.toString().equals(context!!.getString(R.string.off), ignoreCase = true)) {
                    val repeatList = ArrayList<Repeat>()
                    val repeat = Repeat()
                    repeat.id = 8 //Once
                    repeat.isChecked = true
                    repeat.shortName = context!!.getString(R.string.once)
                    val rand = Random()
                    repeat.alarmId = rand.nextInt(9999)
                    repeatList.add(0, repeat)

                    val alarmModel: AlarmModel?
                    alarmModel = if (isFromEdit)
                        savedAlarmModel
                    else
                        AlarmModel()

                    val times = tvTime.text.toString().split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    var hour = Integer.parseInt(times[0])
                    val minute = Integer.parseInt(times[1])
                    if (tvAMPM.text.toString() == "PM" && hour != 12) {
                        hour += 12
                    } else if (tvAMPM.text.toString() == "AM" && hour == 12) {
                        hour = 0
                    }
                    alarmModel!!.hour = hour
                    alarmModel.minute = minute
                    alarmModel.amPm = tvAMPM.text.toString().trim { it <= ' ' }
                    alarmModel.isEnabled = true
                    alarmModel.uriString = currentToneUri!!.toString()
                    val repeatInts = ArrayList<Int>()
                    val pendingIntentInts = ArrayList<Int>()
                    for (repeat1 in repeatList) {
                        repeatInts.add(repeat1.id)
                        pendingIntentInts.add(repeat1.alarmId)
                    }
                    alarmModel.repeats = repeatInts
                    alarmModel.pendingIntentIds = pendingIntentInts

                    setResult(RESULT_OK)

                    val alarmId: Int
                    alarmId = if (isFromEdit) {
                        for (pendingIntentId in savedAlarmModel!!.pendingIntentIds)
                            AlarmScheduler.cancelReminder(context!!, AlarmReceiver::class.java, pendingIntentId)
                        AlarmModel.updateAlarm(context!!, savedAlarmModel!!.id, alarmModel)
                        savedAlarmModel!!.id
                    } else {
                        AlarmModel.insertAlarm(context!!, alarmModel)
                        AlarmModel.getLatestId(context!!)
                    }

                    alarmModel.id = alarmId
                    AlarmScheduler.setReminder(context!!, AlarmReceiver::class.java, alarmModel)
                    onBackPressed()
                } else if (tvRepeatText.text.toString().trim { it <= ' ' }.equals(context!!.getString(R.string.every_day), ignoreCase = true)) {
                    val times = tvTime.text.toString().split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    var hour = Integer.parseInt(times[0])
                    val minute = Integer.parseInt(times[1])
                    if (tvAMPM.text.toString() == "PM" && hour != 12) {
                        hour += 12
                    } else if (tvAMPM.text.toString() == "AM" && hour == 12) {
                        hour = 0
                    }

                    val alarmModel: AlarmModel?
                    alarmModel = if (isFromEdit)
                        savedAlarmModel
                    else
                        AlarmModel()

                    alarmModel!!.hour = hour
                    alarmModel.minute = minute
                    alarmModel.amPm = tvAMPM.text.toString().trim { it <= ' ' }
                    alarmModel.isEnabled = true
                    alarmModel.uriString = currentToneUri!!.toString()
                    val repeatInts = ArrayList<Int>()
                    val pendingIntentInts = ArrayList<Int>()
                    val repeat = repeatList!![0]
                    repeatInts.add(repeat.id)
                    val r = Random()
                    val low = 10000
                    val high = 99999
                    val pendingIntentId = r.nextInt(high - low) + low
                    pendingIntentInts.add(pendingIntentId)
                    /*for(int i = 0; i < 7; i++) {

                        }*/
                    alarmModel.repeats = repeatInts
                    alarmModel.pendingIntentIds = pendingIntentInts

                    setResult(RESULT_OK)

                    if (isFromEdit) {
                        for (savedPendingIntentId in savedAlarmModel!!.pendingIntentIds)
                            AlarmScheduler.cancelReminder(context!!, AlarmReceiver::class.java, savedPendingIntentId)
                        AlarmModel.updateAlarm(context!!, savedAlarmModel!!.id, alarmModel)
                    } else
                        AlarmModel.insertAlarm(context!!, alarmModel)

                    AlarmScheduler.setReminder(context!!, AlarmReceiver::class.java, alarmModel)
                    onBackPressed()
                } else {
                    LPHLog.d("Coming to Else : " + repeatList!!.size)
                    LPHLog.d("Name : " + repeatList!![0].shortName)
                    val times = tvTime.text.toString().split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    var hour = Integer.parseInt(times[0])
                    val minute = Integer.parseInt(times[1])
                    if (tvAMPM.text.toString() == "PM" && hour != 12) {
                        hour += 12
                    } else if (tvAMPM.text.toString() == "AM" && hour == 12) {
                        hour = 0
                    }

                    val alarmModel: AlarmModel? = if (isFromEdit)
                        savedAlarmModel
                    else
                        AlarmModel()

                    alarmModel!!.hour = hour
                    alarmModel.minute = minute
                    alarmModel.amPm = tvAMPM.text.toString().trim { it <= ' ' }
                    alarmModel.isEnabled = true
                    alarmModel.uriString = currentToneUri!!.toString()
                    val repeatInts = ArrayList<Int>()
                    val pendingIntentInts = ArrayList<Int>()
                    val selectedRepeatedList = getSelectedRepeatedList(repeatList)
                    for (repeat in selectedRepeatedList) {
                        LPHLog.d("Week Name : " + repeat.shortName)
                        repeatInts.add(repeat.id)
                        pendingIntentInts.add(repeat.alarmId)
                    }
                    alarmModel.repeats = repeatInts
                    alarmModel.pendingIntentIds = pendingIntentInts
                    setResult(RESULT_OK)

                    if (isFromEdit)
                        AlarmModel.updateAlarm(context!!, savedAlarmModel!!.id, alarmModel)
                    else
                        AlarmModel.insertAlarm(context!!, alarmModel)

                    AlarmScheduler.setReminder(context!!, AlarmReceiver::class.java, alarmModel)
                    onBackPressed()
                }
            }
        }

        ivRingtone.setOnClickListener {
            if (currentToneUri == null || RingtoneManager.getRingtone(context!!, currentToneUri) == null)
                currentToneUri = RingtoneManager.getActualDefaultRingtoneUri(context!!, RingtoneManager.TYPE_NOTIFICATION)
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone")
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentToneUri)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            startActivityForResult(intent, Constants.REQUEST_CODE_TONE_PICKER)
        }


    }

    interface OnRepeatSelect {
        fun updateList(repeatList: List<Repeat>)
    }


    private fun generateList(): MutableList<Repeat> {
        val repeatList = ArrayList<Repeat>()

        var repeat = Repeat()
        repeat.id = 0 //Every Day
        repeat.isChecked = false
        repeat.shortName = context!!.getString(R.string.every_day)
        val rand = Random()
        repeat.alarmId = rand.nextInt(9999)
        repeatList.add(0, repeat)

        repeat = Repeat()
        repeat.id = 1 //Sunday
        repeat.isChecked = false
        repeat.shortName = context!!.getString(R.string.sun)
        repeat.alarmId = rand.nextInt(9999)
        repeatList.add(1, repeat)

        repeat = Repeat()
        repeat.id = 2 //Monday
        repeat.isChecked = false
        repeat.shortName = context!!.getString(R.string.mon)
        repeat.alarmId = rand.nextInt(9999)
        repeatList.add(2, repeat)

        repeat = Repeat()
        repeat.id = 3 //Tuesday
        repeat.isChecked = false
        repeat.shortName = context!!.getString(R.string.tue)
        repeat.alarmId = rand.nextInt(9999)
        repeatList.add(3, repeat)

        repeat = Repeat()
        repeat.id = 4 //Wednesday
        repeat.isChecked = false
        repeat.shortName = context!!.getString(R.string.wed)
        repeat.alarmId = rand.nextInt(9999)
        repeatList.add(4, repeat)

        repeat = Repeat()
        repeat.id = 5 //Thursday
        repeat.isChecked = false
        repeat.shortName = context!!.getString(R.string.thu)
        repeat.alarmId = rand.nextInt(9999)
        repeatList.add(5, repeat)

        repeat = Repeat()
        repeat.id = 6 //Friday
        repeat.isChecked = false
        repeat.shortName = context!!.getString(R.string.fri)
        repeat.alarmId = rand.nextInt(9999)
        repeatList.add(6, repeat)

        repeat = Repeat()
        repeat.id = 7 //Saturday
        repeat.isChecked = false
        repeat.shortName = context!!.getString(R.string.sat)
        repeat.alarmId = rand.nextInt(9999)
        repeatList.add(7, repeat)
        return repeatList
    }


    private fun getSavedRepeats(savedAlarmModel: AlarmModel): List<Repeat> {
        val repeatList = generateList()
        var repeat: Repeat?

        for (i in 0..7) {
            repeat = getSavedRepeat(i, savedAlarmModel)
            if (repeat != null) {
                repeatList[i] = repeat
            }
        }

        LPHLog.d("savedRepeatedList.size : " + repeatList.size)
        return repeatList
    }

    private fun getSavedRepeat(id: Int, savedAlarmModel: AlarmModel): Repeat? {
        var repeat: Repeat? = null
        for (i in savedAlarmModel.repeats.indices) {
            if (id == savedAlarmModel.repeats[i]) {
                repeat = Repeat()
                repeat.id = savedAlarmModel.repeats[i]
                repeat.alarmId = savedAlarmModel.pendingIntentIds[i]
                repeat.shortName = Helper.repeatText(context!!, savedAlarmModel.repeats[i])
                repeat.isChecked = true
                LPHLog.d("repeat.id : " + repeat.id)
                LPHLog.d("repeat.alarmIdd : " + repeat.alarmId)
                LPHLog.d("repeat.shortName : " + repeat.shortName)
                return repeat
            }
        }
        return repeat
    }

    private fun getRepeatText(repeatList: List<Repeat>?): String {

        val stringBuilder = StringBuilder()

        if (repeatList!![0].isChecked) {
            stringBuilder.append(repeatList[0].shortName)
        } else {
            val selectedRepeatedList = getSelectedRepeatedList(repeatList)
            /*for(int i = 1; i < repeatList.size(); i++){
             Repeat repeat = repeatList.get(i);
             if(repeat.isChecked)
                 selectedRepeatedList.add(new Repeat(repeat));

         }*/

            for (i in selectedRepeatedList.indices) {
                val repeat = selectedRepeatedList[i]
                if (repeat.isChecked) {
                    if (i == 0) {
                        stringBuilder.append(repeat.shortName)
                    } else {
                        stringBuilder.append(", ").append(repeat.shortName)
                    }
                }
            }
        }
        return stringBuilder.toString()
    }


    private fun getSelectedRepeatedList(repeatList: List<Repeat>?): List<Repeat> {
        /*val selectedRepeatedList = ArrayList<Repeat>()
        for (i in 1 until repeatList!!.size) {
            val repeat = repeatList[i]
            if (repeat.isChecked)
                selectedRepeatedList.add(Repeat(repeat))

        }*/

        val selectedRepeatedList = (1 until repeatList!!.size)
                .asSequence()
                .map { repeatList[it] }
                .filter { it.isChecked }
                .map { Repeat(it) }
                .toList()

        return selectedRepeatedList
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == Constants.REQUEST_CODE_TONE_PICKER && data != null) {
            currentToneUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (currentToneUri != null) {
                val ringTonePath = currentToneUri!!.toString()
                LPHLog.d("ringtonePAth : " + ringTonePath)
                val ringtone = RingtoneManager.getRingtone(context, currentToneUri)
                val title = ringtone.getTitle(context)
                LPHLog.d("Notification Name : " + title)
                val tvRingTone = findViewById<TextView>(R.id.tv_ringtone)
                tvRingTone.text = title
            }
        }
    }
}
