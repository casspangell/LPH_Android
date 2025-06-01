package org.lovepeaceharmony.androidapp.auth

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.lovepeaceharmony.androidapp.repo.DataState
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun validateSession(): DataState<Boolean> = withContext(Dispatchers.IO) {
        val user = firebaseAuth.currentUser
        return@withContext if (user != null) {
            DataState.Success(true)
        } else {
            AuthPrefs.clear(context)
            DataState.Error("Session invalid or expired")
        }
    }

    fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    fun logout() {
        AuthPrefs.clear(context)
        firebaseAuth.signOut()
    }
} 