package org.lovepeaceharmony.androidapp.api

import org.lovepeaceharmony.androidapp.model.LoginRequest
import org.lovepeaceharmony.androidapp.model.LoginResponse
import org.lovepeaceharmony.androidapp.model.User
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @GET("users/me")
    suspend fun getCurrentUser(): Response<User>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("auth/refresh")
    suspend fun refreshToken(): Response<LoginResponse>

    @POST("users/register")
    suspend fun register(@Body user: User): Response<User>

    @PUT("users/me")
    suspend fun updateProfile(@Body user: User): Response<User>
} 