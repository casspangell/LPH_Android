package org.lovepeaceharmony.android.app.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.lovepeaceharmony.android.app.utility.Constants
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val Context.dataStore by preferencesDataStore(Constants.DATASTORE_NAME)

    private fun <T> DataStore<Preferences>.getFromLocalStorage(
        PreferencesKey: Preferences.Key<T>, defaultValue: T?
    ) = data.catch {
        if (it is IOException) {
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map {
        it[PreferencesKey] ?: defaultValue
    }

    suspend fun <T> storeValue(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit {
            it[key] = value
        }
    }

    fun <T> readValue(
        key: Preferences.Key<T>,
        defaultValue: T? = null,
    ) = context.dataStore.getFromLocalStorage(key, defaultValue)
}
