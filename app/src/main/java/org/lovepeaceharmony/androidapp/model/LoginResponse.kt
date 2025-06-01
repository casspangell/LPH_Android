package org.lovepeaceharmony.androidapp.model

data class LoginResponse(
    val token: String,
    val refreshToken: String,
    val user: User
) 