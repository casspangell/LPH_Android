package org.lovepeaceharmony.androidapp.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SessionValidationResponse(
    @Json(name = "isValid") val isValid: Boolean
) 