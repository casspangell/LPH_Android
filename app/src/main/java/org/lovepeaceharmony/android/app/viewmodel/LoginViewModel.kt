package org.lovepeaceharmony.android.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.lovepeaceharmony.android.app.pref.DataStoreManager
import org.lovepeaceharmony.android.app.utility.FIRST_ENTRY
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class LoginViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    val firstEntry = dataStoreManager.readValue(FIRST_ENTRY, true)
        .mapLatest { it ?: true }
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5000),
            initialValue = false
        )

    fun setFirstEntry(value: Boolean) {
        viewModelScope.launch {
            dataStoreManager.storeValue(FIRST_ENTRY, value)
        }
    }
}