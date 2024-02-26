package org.lovepeaceharmony.android.app.viewmodel

import androidx.lifecycle.*
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.lovepeaceharmony.android.app.model.ChantingMilestone
import org.lovepeaceharmony.android.app.model.StreakMilestone
import org.lovepeaceharmony.android.app.utility.DataState
import org.lovepeaceharmony.android.app.pref.DataStoreManager
import org.lovepeaceharmony.android.app.utility.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    val chantData get() = Firebase.chantData.asLiveData(Dispatchers.Main)

    private val _firebaseDbCleared = MutableLiveData<DataState<Unit>>()
    val firebaseDbCleared: LiveData<DataState<Unit>>
        get() = _firebaseDbCleared


    val isToolTipShown = dataStoreManager.readValue(TOOL_TIP_SHOWN, false)
        .mapLatest { it ?: true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun setToolTipShown(value: Boolean) {
        viewModelScope.launch {
            dataStoreManager.storeValue(TOOL_TIP_SHOWN, value)
        }
    }

    fun updateMilestone(minutes: Float) {
        if (minutes == 0F) return
        val secs = (minutes * 60).toInt().toLong()
        updateChantingMilestone(secs)
        viewModelScope.launch {
            Firebase.chantData.collect { chantDataState ->
                if (chantDataState is DataState.Success) {
                    val timeStamp = getCurrentServerTime()

                    val newMilestone = ChantingMilestone(timeStamp, (minutes * 60).toInt().toLong())
                    Firebase.updateMilestonesState(newMilestone).collect {}

                    val updatedStreak = chantDataState.data.streakMilestone.apply {
                        when (isChantStreakAlive(last_day_chanted)) {
                            in 0..23 -> Unit
                            in 24..48 -> {
                                current_streak++
                                if (current_streak > longest_streak) longest_streak = current_streak
                            }
                            else -> current_streak = 0
                        }
                        last_day_chanted = timeStamp
                    }
                    Firebase.updateStreakState(updatedStreak).collect { updateChantingTotal(secs) }


                }
            }
        }
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
                if (streakState is DataState.Success) {
                    val timeStamp = getCurrentServerTime()
                    val currentStreak = streakState.data
                    val updatedStreak = currentStreak?.apply {
                        when (isChantStreakAlive(last_day_chanted)) {
                            in 0..23 -> {
                            }
                            in 24..48 -> {
                                current_streak++
                                if (current_streak > longest_streak) longest_streak = current_streak
                            }
                            else -> current_streak = 0
                        }
                        last_day_chanted = timeStamp
                    } ?: StreakMilestone(1L, timeStamp, 1L)

                    Firebase.updateStreakState(updatedStreak).collect { updateChantingTotal(secs) }
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

    private fun isChantStreakAlive(timeStamp: String): Long {
        val outPutFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
        val timeStampDate = outPutFormat.parse(timeStamp)

        return timeStampDate?.let {
            val longDiff = Calendar.getInstance().time.time - it.time
            TimeUnit.MILLISECONDS.toHours(longDiff)
        } ?: 0
    }

    fun clearMileStones() {
        viewModelScope.launch(Dispatchers.IO) {
            Firebase.reset().collect {
                _firebaseDbCleared.postValue(it)
            }
        }
    }
}