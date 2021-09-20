package org.lovepeaceharmony.android.app.ui.base

import androidx.appcompat.app.AppCompatActivity
import org.lovepeaceharmony.android.app.utility.BetterActivityResult


abstract class BaseActivity : AppCompatActivity() {
    val activityLauncher = BetterActivityResult.registerActivityForResult(this)

}