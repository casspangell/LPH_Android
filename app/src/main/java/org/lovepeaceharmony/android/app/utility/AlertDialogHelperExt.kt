package org.lovepeaceharmony.android.app.utility

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

inline fun Activity.alert(title: CharSequence? = null, message: CharSequence? = null, func: AlertDialogHelper.() -> Unit): AlertDialog {
    return AlertDialogHelper(this, title, message).apply {
        func()
    }.create()
}

inline fun Activity.alert(titleResource: Int = 0, messageResource: Int = 0, func: AlertDialogHelper.() -> Unit): AlertDialog {
    val title = if (titleResource == 0) null else getString(titleResource)
    val message = if (messageResource == 0) null else getString(messageResource)
    return AlertDialogHelper(this, title, message).apply {
        func()
    }.create()
}

inline fun Fragment.alert(title: CharSequence? = null, message: CharSequence? = null, func: AlertDialogHelper.() -> Unit): AlertDialog {
    return AlertDialogHelper(requireContext(), title, message).apply {
        func()
    }.create()
}

inline fun Fragment.alert(titleResource: Int = 0, messageResource: Int = 0, func: AlertDialogHelper.() -> Unit): AlertDialog {
    val title = if (titleResource == 0) null else getString(titleResource)
    val message = if (messageResource == 0) null else getString(messageResource)
    return AlertDialogHelper(requireContext(), title, message).apply {
        func()
    }.create()
}

/**
 * Implementation of lazy that is not thread safe. Useful when you know what thread you will be
 * executing on and are not worried about synchronization.
 */
fun <T> lazyFast(operation: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
    operation()
}