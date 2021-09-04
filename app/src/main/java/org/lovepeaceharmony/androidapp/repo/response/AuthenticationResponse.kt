package org.lovepeaceharmony.androidapp.repo.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthenticationResponse(
    val data: Data? = null,
    val success: Boolean,
    val message: String,
    val errors: AuthError? = null
)