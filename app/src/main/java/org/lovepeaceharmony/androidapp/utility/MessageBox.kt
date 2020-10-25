package org.lovepeaceharmony.androidapp.utility

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import org.lovepeaceharmony.androidapp.R

/**
 * MessageBox
 * Created by Naveen Kumar M on 02/01/18.
 */
class MessageBox(private val context: Context, private val title: String, private val message: String) {

    private var progressDialog: Dialog? = null

    fun showProgress() {
        progressDialog = Dialog(context)
        if (progressDialog!!.window != null) {
            progressDialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        }
        progressDialog!!.setContentView(R.layout.custom_progress_layout)
        progressDialog!!.setCancelable(false)
        progressDialog!!.setTitle(title)

        if (progressDialog!!.window != null) {
            progressDialog!!.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            progressDialog!!.window!!.setGravity(Gravity.CENTER_HORIZONTAL)
        }

        progressDialog!!.show()
    }

    fun hideProgress() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }
    }

    fun showMessage() {
        val alert = AlertDialog.Builder(context)
        alert.setTitle(title)
        alert.setMessage(message)
        alert.setPositiveButton("OK", null)
        alert.show()
    }

    fun showMessageForRetry(action: DialogInterface.OnClickListener) {
        val alert = android.support.v7.app.AlertDialog.Builder(context!!)
        alert.setTitle(title)
        alert.setMessage(message)
        alert.setCancelable(false)
        alert.setNegativeButton("Close", action)
        alert.setPositiveButton("Retry", action)

        val alertDialog = alert.create()
        alertDialog.show()

    }
}