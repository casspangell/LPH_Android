package org.lovepeaceharmony.androidapp.repo.pref

import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import org.lovepeaceharmony.androidapp.repo.DataState
import javax.inject.Inject
import javax.inject.Singleton

data class UserPreferences(
    val toolTipShown: Boolean,
)

/**
 * Class that handles saving and retrieving user preferences
 */
@ExperimentalCoroutinesApi
@Singleton
class UserPreferencesRepository @Inject constructor(private val dataStoreManager: DataStoreManager) {

    private object PreferencesKeys {
        val TOOL_TIP_SHOWN = booleanPreferencesKey("tool_tip_shown")
    }

    private val isToolTipShown: Flow<Boolean> = callbackFlow {
        dataStoreManager.readValue(PreferencesKeys.TOOL_TIP_SHOWN, false) {
            trySend(this).isSuccess
        }
        awaitClose()
    }


    /**
     * Get the user preferences flow.
     */
    val userPreferencesFlow: Flow<DataState<UserPreferences>>
        get() = combine(isToolTipShown) { isToolTipShown ->
            DataState.Success(UserPreferences(isToolTipShown.first())) as DataState<UserPreferences>
        }


    suspend fun updateToolTipShown(shown: Boolean) {
        dataStoreManager.storeValue(PreferencesKeys.TOOL_TIP_SHOWN, shown)
    }
}