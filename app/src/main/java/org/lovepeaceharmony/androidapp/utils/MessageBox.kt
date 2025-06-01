import android.app.AlertDialog
import android.content.Context

class MessageBox {
    fun showMessage(context: Context, message: String) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setPositiveButton(context.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    fun showMessageWithTitle(context: Context, title: String, message: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(context.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    fun showConfirmationDialog(context: Context, message: String, positiveButtonText: String, negativeButtonText: String, onPositiveClick: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setPositiveButton(positiveButtonText) { dialog, _ ->
            dialog.dismiss()
            onPositiveClick()
        }
        builder.setNegativeButton(negativeButtonText) { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
} 