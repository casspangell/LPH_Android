package org.lovepeaceharmony.androidapp.repo.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import org.lovepeaceharmony.androidapp.utility.Constants
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val Context.dataStore by preferencesDataStore(Constants.DATASTORE_NAME)

    private suspend fun <T> DataStore<Preferences>.getFromLocalStorage(
        PreferencesKey: Preferences.Key<T>, defaultValue: T?, func: T.() -> Unit
    ) {
        data.catch {
            if (it is IOException) {
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            it[PreferencesKey] ?: defaultValue
        }.collect {
            it?.let { func.invoke(it as T) }
        }
    }

    suspend fun <T> storeValue(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit {
            it[key] = value
        }
    }

    suspend fun <T> readValue(
        key: Preferences.Key<T>,
        defaultValue: T? = null,
        responseFunc: T.() -> Unit
    ) {
        context.dataStore.getFromLocalStorage(key, defaultValue) {
            responseFunc.invoke(this)
        }
    }
}
