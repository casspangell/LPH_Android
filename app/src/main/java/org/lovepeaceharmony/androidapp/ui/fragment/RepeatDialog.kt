package org.lovepeaceharmony.androidapp.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.*
import android.widget.CheckBox
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.model.Repeat
import org.lovepeaceharmony.androidapp.ui.activity.AddReminderActivity
import java.util.*

/**
 * RepeatDialog
 * Created by Naveen Kumar M on 30/11/17.
 */

class RepeatDialog : DialogFragment() {

    private var onRepeatSelect: AddReminderActivity.OnRepeatSelect? = null
    private val repeatList = ArrayList<Repeat>()

    fun setRepeatDialog(onRepeatSelect: AddReminderActivity.OnRepeatSelect, repeatList: List<Repeat>) {
        this.onRepeatSelect = onRepeatSelect
        for (repeat in repeatList) {
            this.repeatList.add(Repeat(repeat))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        // request a window without the title
        if (dialog.window != null)
            dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null && dialog.window != null) {
            dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window!!.setGravity(Gravity.CENTER_HORIZONTAL)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dailog_repeat_layout, container, false)

        rootView.findViewById<View>(R.id.btn_dialog_cancel).setOnClickListener { dismiss() }

        rootView.findViewById<View>(R.id.btn_dialog_save).setOnClickListener {
            onRepeatSelect!!.updateList(repeatList)
            dismiss()
        }

        val chkEveryDay = rootView.findViewById<CheckBox>(R.id.chk_every_day)
        val chkSunDay = rootView.findViewById<CheckBox>(R.id.chk_sun_day)
        val chkMonday = rootView.findViewById<CheckBox>(R.id.chk_mon_day)
        val chkTuesday = rootView.findViewById<CheckBox>(R.id.chk_tue_day)
        val chkWednesday = rootView.findViewById<CheckBox>(R.id.chk_wed_day)
        val chkThursday = rootView.findViewById<CheckBox>(R.id.chk_thu_day)
        val chkFriday = rootView.findViewById<CheckBox>(R.id.chk_fri_day)
        val chkSatDay = rootView.findViewById<CheckBox>(R.id.chk_sat_day)

        if (repeatList[0].isChecked) {
            chkEveryDay.isChecked = true
            chkSunDay.isEnabled = false
            chkMonday.isEnabled = false
            chkTuesday.isEnabled = false
            chkWednesday.isEnabled = false
            chkThursday.isEnabled = false
            chkFriday.isEnabled = false
            chkSatDay.isEnabled = false
        } else {
            chkSunDay.isEnabled = true
            chkMonday.isEnabled = true
            chkTuesday.isEnabled = true
            chkWednesday.isEnabled = true
            chkThursday.isEnabled = true
            chkFriday.isEnabled = true
            chkSatDay.isEnabled = true
            if (repeatList[1].isChecked) {
                chkSunDay.isChecked = true
            }

            if (repeatList[2].isChecked) {
                chkMonday.isChecked = true
            }

            if (repeatList[3].isChecked) {
                chkTuesday.isChecked = true
            }

            if (repeatList[4].isChecked) {
                chkWednesday.isChecked = true
            }

            if (repeatList[5].isChecked) {
                chkThursday.isChecked = true
            }

            if (repeatList[6].isChecked) {
                chkFriday.isChecked = true
            }

            if (repeatList[7].isChecked) {
                chkSatDay.isChecked = true
            }
        }

        chkEveryDay.setOnCheckedChangeListener { compoundButton, isChecked ->
            chkEveryDay.isChecked = isChecked
            val repeat = repeatList[0]
            repeat.isChecked = isChecked
            repeatList[0] = repeat
            if (isChecked) {
                chkSunDay.isEnabled = false
                chkMonday.isEnabled = false
                chkTuesday.isEnabled = false
                chkWednesday.isEnabled = false
                chkThursday.isEnabled = false
                chkFriday.isEnabled = false
                chkSatDay.isEnabled = false

                chkSunDay.isChecked = false
                chkMonday.isChecked = false
                chkTuesday.isChecked = false
                chkWednesday.isChecked = false
                chkThursday.isChecked = false
                chkFriday.isChecked = false
                chkSatDay.isChecked = false
            } else {
                chkSunDay.isEnabled = true
                chkMonday.isEnabled = true
                chkTuesday.isEnabled = true
                chkWednesday.isEnabled = true
                chkThursday.isEnabled = true
                chkFriday.isEnabled = true
                chkSatDay.isEnabled = true
            }
        }

        chkSunDay.setOnCheckedChangeListener { compoundButton, isChecked ->
            chkSunDay.isChecked = isChecked
            val repeat = repeatList[1]
            repeat.isChecked = isChecked
            repeatList[1] = repeat
            if (isChecked) {
                chkEveryDay.isChecked = false
            }
        }

        chkMonday.setOnCheckedChangeListener { compoundButton, isChecked ->
            chkMonday.isChecked = isChecked
            val repeat = repeatList[2]
            repeat.isChecked = isChecked
            repeatList[2] = repeat
            if (isChecked) {
                chkEveryDay.isChecked = false
            }
        }

        chkTuesday.setOnCheckedChangeListener { compoundButton, isChecked ->
            chkTuesday.isChecked = isChecked
            val repeat = repeatList[3]
            repeat.isChecked = isChecked
            repeatList[3] = repeat
            if (isChecked) {
                chkEveryDay.isChecked = false
            }
        }

        chkWednesday.setOnCheckedChangeListener { compoundButton, isChecked ->
            chkWednesday.isChecked = isChecked
            val repeat = repeatList[4]
            repeat.isChecked = isChecked
            repeatList[4] = repeat
            if (isChecked) {
                chkEveryDay.isChecked = false
            }
        }

        chkThursday.setOnCheckedChangeListener { compoundButton, isChecked ->
            chkThursday.isChecked = isChecked
            val repeat = repeatList[5]
            repeat.isChecked = isChecked
            repeatList[5] = repeat
            if (isChecked) {
                chkEveryDay.isChecked = false
            }
        }

        chkFriday.setOnCheckedChangeListener { compoundButton, isChecked ->
            chkFriday.isChecked = isChecked
            val repeat = repeatList[6]
            repeat.isChecked = isChecked
            repeatList[6] = repeat
            if (isChecked) {
                chkEveryDay.isChecked = false
            }
        }

        chkSatDay.setOnCheckedChangeListener { compoundButton, isChecked ->
            chkSatDay.isChecked = isChecked
            val repeat = repeatList[7]
            repeat.isChecked = isChecked
            repeatList[7] = repeat
            if (isChecked) {
                chkEveryDay.isChecked = false
            }
        }

        dialog.setTitle(null)
        return rootView
    }


}
