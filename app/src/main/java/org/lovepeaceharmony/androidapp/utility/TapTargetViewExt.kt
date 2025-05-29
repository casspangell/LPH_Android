package org.lovepeaceharmony.androidapp.utility

import android.app.Activity
import android.util.Log
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import org.lovepeaceharmony.androidapp.R

private fun tapTargetListener(onTargetDismissed: () -> Unit) = object : TapTargetView.Listener() {
    override fun onTargetClick(view: TapTargetView) {
        super.onTargetClick(view)
        LPHLog.d("Tool Tip : Sequence Finished")
    }

    override fun onOuterCircleClick(view: TapTargetView?) {
        super.onOuterCircleClick(view)
        view?.dismiss(true)
        LPHLog.d("Tool Tip : .Sequence Finished")
    }

    override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
        onTargetDismissed.invoke()
    }
}

fun TapTarget.init(
    activity: Activity,
    @ColorRes outerCircleColor: Int,
    onTargetDismissed: () -> Unit
) {
    cancelable(false)
    dimColor(android.R.color.white)
    outerCircleColor(outerCircleColor)
    targetCircleColor(android.R.color.black)
    transparentTarget(true)
    textColor(android.R.color.white)
    titleTextSize(18)
    descriptionTextSize(14)
    textTypeface(ResourcesCompat.getFont(activity, R.font.open_sans_semi_bold))
    descriptionTypeface(ResourcesCompat.getFont(activity, R.font.open_sans_regular))
    id(1)
    tintTarget(false)

    TapTargetView.showFor(activity, this, tapTargetListener(onTargetDismissed))
}