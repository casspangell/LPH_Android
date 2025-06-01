package org.lovepeaceharmony.androidapp.repo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.lovepeaceharmony.androidapp.api.UserApi
import org.lovepeaceharmony.androidapp.model.LoginRequest
import org.lovepeaceharmony.androidapp.model.LoginResponse
import org.lovepeaceharmony.androidapp.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApi: UserApi
) {
    suspend fun login(email: String, password: String): Flow<Result<LoginResponse>> = flow {
        try {
            val response = userApi.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.let { emit(Result.success(it)) }
                    ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Login failed: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getCurrentUser(): Flow<Result<User>> = flow {
        try {
            val response = userApi.getCurrentUser()
            if (response.isSuccessful) {
                response.body()?.let { emit(Result.success(it)) }
                    ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Failed to get user: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun logout(): Flow<Result<Unit>> = flow {
        try {
            val response = userApi.logout()
            if (response.isSuccessful) {
                emit(Result.success(Unit))
            } else {
                emit(Result.failure(Exception("Logout failed: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun refreshToken(): Flow<Result<LoginResponse>> = flow {
        try {
            val response = userApi.refreshToken()
            if (response.isSuccessful) {
                response.body()?.let { emit(Result.success(it)) }
                    ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Token refresh failed: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun register(user: User): Flow<Result<User>> = flow {
        try {
            val response = userApi.register(user)
            if (response.isSuccessful) {
                response.body()?.let { emit(Result.success(it)) }
                    ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Registration failed: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun updateProfile(user: User): Flow<Result<User>> = flow {
        try {
            val response = userApi.updateProfile(user)
            if (response.isSuccessful) {
                response.body()?.let { emit(Result.success(it)) }
                    ?: emit(Result.failure(Exception("Empty response body")))
            } else {
                emit(Result.failure(Exception("Profile update failed: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
} 