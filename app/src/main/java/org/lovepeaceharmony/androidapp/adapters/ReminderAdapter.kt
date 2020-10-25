package org.lovepeaceharmony.androidapp.adapters

import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.model.AlarmModel
import org.lovepeaceharmony.androidapp.utility.AlarmReceiver
import org.lovepeaceharmony.androidapp.utility.AlarmScheduler
import org.lovepeaceharmony.androidapp.utility.Helper
import org.lovepeaceharmony.androidapp.utility.LPHLog
import java.text.SimpleDateFormat
import java.util.*

/**
 * ReminderAdapter
 * Created by Naveen Kumar M on 28/12/17.
 */
class ReminderAdapter(private val context: Context, cursor: Cursor?, private val onRemindersCallBack: OnRemindersCallBack): CursorRecyclerViewAdapter<ReminderAdapter.ViewHolder>(null) {


    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var tvTime: TextView
        var tvAmPm: TextView
        var tvRepeats: TextView
        var toggleButton: ToggleButton

        init {
            tvTime = view.findViewById(R.id.tv_time)
            tvAmPm = view.findViewById(R.id.tv_am_pm)
            tvRepeats = view.findViewById(R.id.tv_repeat_text)
            toggleButton = view.findViewById(R.id.toggle_enabled)
        }
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, cursor: Cursor?) {
        val alarmModel = AlarmModel.getValueFromCursor(cursor!!)
        val c2 = Calendar.getInstance()

        if (itemCount > 6 && cursor.position + 1 == itemCount) {
            // set bottom margin to 72dp.
            setBottomMargin(viewHolder.itemView, (72 * Resources.getSystem().displayMetrics.density).toInt())
        } else {
            // reset bottom margin back to zero. (your value may be different)
            setBottomMargin(viewHolder.itemView, 0)
        }

        c2.set(c2.get(Calendar.YEAR), c2.get(Calendar.MONTH), c2.get(Calendar.DATE), alarmModel.hour, alarmModel.minute)
        val mTimeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
        viewHolder.tvTime.text = mTimeFormat.format(c2.time)
        viewHolder.tvAmPm.text = alarmModel.amPm
        viewHolder.tvRepeats.text = Helper.getRepeatText(context, alarmModel.repeats)
        viewHolder.toggleButton.isChecked = alarmModel.isEnabled
        viewHolder.view.tag = alarmModel

        viewHolder.toggleButton.setOnCheckedChangeListener { compoundButton, isChecked ->
            LPHLog.d("onCheckedChanged : " + isChecked)
            if (!isChecked) {
                for (pendingIntentId in alarmModel.pendingIntentIds)
                    AlarmScheduler.cancelReminder(context, AlarmReceiver::class.java, pendingIntentId)
            } else {
                AlarmScheduler.setReminder(context, AlarmReceiver::class.java, alarmModel)
            }
            AlarmModel.updateIsEnabled(context, alarmModel.id, isChecked)
            onRemindersCallBack.onRefresh()
        }

        viewHolder.view.setOnClickListener { view ->
            val alarmModel1 = view.tag as AlarmModel
            onRemindersCallBack.onItemClick(alarmModel1.id)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.reminder_row, parent, false)
        return ViewHolder(itemView)
    }

    interface OnRemindersCallBack {
        fun onRefresh()
        fun onItemClick(_id: Int)
    }

    companion object {

        fun setBottomMargin(view: View, bottomMargin: Int) {
            if (view.layoutParams is ViewGroup.MarginLayoutParams) {
                val params = view.layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, bottomMargin)
                view.requestLayout()
            }
        }
    }
}