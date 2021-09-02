package org.lovepeaceharmony.androidapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import org.lovepeaceharmony.androidapp.repo.DataState
import org.lovepeaceharmony.androidapp.repo.remote.LPHServiceRetro
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val lphService: LPHServiceRetro) : ViewModel() {

    fun loginFlow(email: String, password: String) = flow {
        emit(DataState.Loading)
        val response = lphService.login(email, password)
        val state = response.body()?.let {
            val errorMsg = it.errors?.email?.firstOrNull() ?: it.errors?.password?.firstOrNull()
            ?: "Something went wrong"
            if (it.success) DataState.Success(it) else DataState.Error(errorMsg)
        } ?: DataState.Error(response.message())
        emit(state)
    }.asLiveData(Dispatchers.IO)

}