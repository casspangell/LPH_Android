package org.lovepeaceharmony.androidapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.lovepeaceharmony.androidapp.utility.Helper
import android.content.Context
import java.lang.ref.WeakReference

@HiltViewModel
class ChantNowViewModel @Inject constructor() : ViewModel() {
    // Add any view model logic here
    
    fun updateMilestone(context: Context, minutes: Float) {
        // Update milestone in Firebase
        Helper.updateMileStonePendingMinutes(context, minutes)
    }
} 