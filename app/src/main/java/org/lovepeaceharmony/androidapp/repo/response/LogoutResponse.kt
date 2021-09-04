package org.lovepeaceharmony.androidapp.repo.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LogoutResponse(val success: Boolean, val message: String)
