package org.lovepeaceharmony.androidapp.utility

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import org.lovepeaceharmony.androidapp.repo.DataState
import org.lovepeaceharmony.androidapp.repo.pref.UserPreferences

val LiveData<DataState<UserPreferences>>.isToolTipShown: Boolean
    get() = map {
        Log.d("isToolTipShown", "State is $it")
        if (it is DataState.Success) it.data.toolTipShown else false
    }.value ?: false