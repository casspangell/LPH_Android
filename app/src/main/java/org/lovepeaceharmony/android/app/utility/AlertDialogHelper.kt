package org.lovepeaceharmony.android.app.utility

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import org.lovepeaceharmony.android.app.R

@SuppressLint("InflateParams")
class AlertDialogHelper(context: Context, title: CharSequence?, message: CharSequence?) {

    private val dialogView: View by lazyFast {
        LayoutInflater.from(context).inflate(R.layout.dialog_info, null)
    }

    private val builder: AlertDialog.Builder = AlertDialog.Builder(context).setView(dialogView)

    private val title: MaterialTextView by lazyFast {
        dialogView.findViewById(R.id.dialogInfoTitleTextView)
    }

    private val message: MaterialTextView by lazyFast {
        dialogView.findViewById(R.id.dialogInfoMessageTextView)
    }

    private val positiveButton: MaterialButton by lazyFast {
        dialogView.findViewById(R.id.dialogInfoPositiveButton)
    }

    private val negativeButton: MaterialButton by lazyFast {
        dialogView.findViewById(R.id.dialogInfoNegativeButton)
    }

    private var dialog: AlertDialog? = null

    var cancelable: Boolean = true

    init {
        this.title.text = title
        this.message.text = message
    }

    fun positiveButton(@StringRes textResource: Int, func: (() -> Unit)? = null) {
        with(positiveButton) {
            text = builder.context.getString(textResource)
            setClickListenerToDialogButton(func)
        }
    }

    fun positiveButton(text: CharSequence, func: (() -> Unit)? = null) {
        with(positiveButton) {
            this.text = text
            setClickListenerToDialogButton(func)
        }
    }

    fun negativeButton(@StringRes textResource: Int, func: (() -> Unit)? = null) {
        with(negativeButton) {
            text = builder.context.getString(textResource)
            setClickListenerToDialogButton(func)
        }
    }

    fun negativeButton(text: CharSequence, func: (() -> Unit)? = null) {
        with(negativeButton) {
            this.text = text
            setClickListenerToDialogButton(func)
        }
    }

    fun onCancel(func: () -> Unit) {
        builder.setOnCancelListener { func() }
    }

    fun create(): AlertDialog {
        title.goneIfTextEmpty()
        message.goneIfTextEmpty()
        positiveButton.goneIfTextEmpty()
        negativeButton.goneIfTextEmpty()

        dialog = builder
            .setCancelable(cancelable)
            .create()
        return dialog!!
    }

    private fun TextView.goneIfTextEmpty() {
        visibility = if (text.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun Button.setClickListenerToDialogButton(func: (() -> Unit)?) {
        setOnClickListener {
            func?.invoke()
            dialog?.dismiss()
        }
    }
}
