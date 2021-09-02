package org.lovepeaceharmony.androidapp.repo.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(

	@Json(name="name")
	val name: String,

	@Json(name="source")
	val source: String,

	@Json(name="invited_by")
	val invitedBy: String? = null,

	@Json(name="invite_code")
	val invitedCode: String? = null,

	@Json(name="id")
	val id: Int,

	@Json(name="profile_pic_url")
	val profilePicUrl: String,

	@Json(name="email")
	val email: String
)