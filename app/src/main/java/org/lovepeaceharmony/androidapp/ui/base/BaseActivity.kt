package org.lovepeaceharmony.androidapp.ui.base

import androidx.appcompat.app.AppCompatActivity
import org.lovepeaceharmony.androidapp.utility.BetterActivityResult


abstract class BaseActivity : AppCompatActivity() {
    val activityLauncher by lazy { BetterActivityResult.registerActivityForResult(this) }

}