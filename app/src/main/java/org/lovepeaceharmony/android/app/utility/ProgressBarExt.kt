package org.lovepeaceharmony.android.app.utility

import android.app.Activity
import android.view.WindowManager
import androidx.core.view.isVisible
import com.google.android.material.progressindicator.CircularProgressIndicator

fun CircularProgressIndicator.loading(show: Boolean) {
    isVisible = show
    if (show) (context as Activity).window.setFlags(
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
    ) else (context as Activity).window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
}
