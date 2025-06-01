package org.lovepeaceharmony.androidapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.lovepeaceharmony.androidapp.auth.SessionManager
import org.lovepeaceharmony.androidapp.repo.DataState
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAutoLogin()
    }

    private fun checkAutoLogin() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            if (!sessionManager.isLoggedIn()) {
                _authState.value = AuthState.NotLoggedIn
                return@launch
            }

            when (val result = sessionManager.validateSession()) {
                is DataState.Success -> {
                    _authState.value = AuthState.LoggedIn
                }
                is DataState.Error -> {
                    _authState.value = AuthState.Error(result.msg)
                }
                else -> {
                    _authState.value = AuthState.Error("Unknown error occurred")
                }
            }
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object LoggedIn : AuthState()
    object NotLoggedIn : AuthState()
    data class Error(val message: String) : AuthState()
} 