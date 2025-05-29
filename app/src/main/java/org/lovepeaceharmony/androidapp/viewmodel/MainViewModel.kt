package org.lovepeaceharmony.androidapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.lovepeaceharmony.androidapp.model.ChantingMilestone
import org.lovepeaceharmony.androidapp.model.StreakMilestone
import org.lovepeaceharmony.androidapp.repo.DataState
import org.lovepeaceharmony.androidapp.repo.pref.UserPreferencesRepository
import org.lovepeaceharmony.androidapp.utility.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPrefRepo: UserPreferencesRepository
) : ViewModel() {

    val userPreferences = userPrefRepo.userPreferencesFlow.asLiveData(Dispatchers.IO)

    var isToolTipShown = false
        get() = userPreferences.isToolTipShown
        set(value) {
            viewModelScope.launch { userPrefRepo.updateToolTipShown(value) }
            field = value
        }

    val chantingStreak: LiveData<DataState<StreakMilestone?>>
        get() = Firebase.currentChantingStreak.asLiveData(Dispatchers.IO)

    val chantingMilestone: LiveData<DataState<List<ChantingMilestone>>>
        get() = Firebase.chantingMilestones.asLiveData(Dispatchers.IO)

    fun updateMilestone(minutes: Float) {
        if (minutes == 0F) return
        val secs = (minutes * 60).toInt().toLong()
        updateChantingMilestone(secs)
    }

    private fun updateChantingMilestone(seconds: Long) {
        viewModelScope.launch {
            Firebase.chantingMilestones.collect { milestonesState ->
                if (milestonesState is DataState.Success) {
                    val newMilestone = ChantingMilestone(getCurrentServerTime(), seconds)
                    Firebase.updateMilestonesState(newMilestone).collect {
                        updateChantingStreak(seconds)
                    }
                }
            }
        }
    }

    private fun updateChantingStreak(secs: Long) {
        viewModelScope.launch {
            Firebase.currentChantingStreak.collect { streakState ->
                val timeStamp = getCurrentServerTime()
                if (streakState is DataState.Success) {
                    val currentStreak = streakState.data
                    val updatedStreak = currentStreak?.let {
                        if (isChantStreakAlive(it.last_day_chanted)) {
                            it.current_streak++
                            if (it.current_streak > it.longest_streak) it.longest_streak =
                                it.current_streak
                        } else {
                            it.current_streak = 0
                        }
                        it.last_day_chanted = timeStamp
                        it
                    } ?: StreakMilestone(1L, timeStamp, 1L)

                    Firebase.updateStreakState(updatedStreak).collect {
                        updateChantingTotal(secs)
                    }
                }
            }
        }
    }

    private fun updateChantingTotal(secs: Long) {
        viewModelScope.launch {
            Firebase.totalSeconds.collect { totalSecondsState ->
                if (totalSecondsState is DataState.Success) {
                    Firebase.updateTotalSeconds(totalSecondsState.data.plus(secs)).collect {}
                }
            }
        }
    }

    private fun getCurrentServerTime(): String {
        val time = Calendar.getInstance().time
        val outPutFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
        LPHLog.d("Current Time in India : " + outPutFormat.format(time))
        outPutFormat.timeZone = TimeZone.getTimeZone("GMT")
        LPHLog.d("Current Time in Global : " + outPutFormat.format(time))
        return outPutFormat.format(time)
    }

    private fun isChantStreakAlive(timeStamp: String): Boolean {
        val outPutFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
        val timeStampDate = outPutFormat.parse(timeStamp)

        val timePassedHours = timeStampDate?.let {
            val longDiff = Calendar.getInstance().time.time - it.time
            TimeUnit.MILLISECONDS.toHours(longDiff)
        } ?: 0

        return timePassedHours in 24..48
    }

    // --- Chant Time Sync ---
    fun syncChantTimeToFirebase(context: Context) {
        val localTotal = TimeTracker.getTotalSeconds(context)
        viewModelScope.launch {
            Firebase.totalSeconds.collect { totalSecondsState ->
                if (totalSecondsState is DataState.Success) {
                    if (localTotal > totalSecondsState.data) {
                        Firebase.updateTotalSeconds(localTotal).collect {}
                    }
                }
            }
        }
    }

    fun fetchAndMergeChantTimeFromFirebase(context: Context) {
        viewModelScope.launch {
            Firebase.totalSeconds.collect { totalSecondsState ->
                if (totalSecondsState is DataState.Success) {
                    val localTotal = TimeTracker.getTotalSeconds(context)
                    if (totalSecondsState.data > localTotal) {
                        TimeTracker.setTotalSeconds(context, totalSecondsState.data)
                    }
                }
            }
        }
    }
}