package org.lovepeaceharmony.androidapp.repo.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthError(
    val email: List<String>? = null,
    val password: List<String>? = null
)