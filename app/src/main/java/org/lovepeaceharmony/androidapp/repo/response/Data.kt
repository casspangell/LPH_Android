package org.lovepeaceharmony.androidapp.repo.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Data(

	@Json(name="user")
	val user: User,

	@Json(name="token")
	val token: String
)