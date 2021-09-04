package org.lovepeaceharmony.androidapp.repo.remote

import org.lovepeaceharmony.androidapp.repo.response.AuthenticationResponse
import org.lovepeaceharmony.androidapp.repo.response.LogoutResponse
import org.lovepeaceharmony.androidapp.utility.Constants
import org.lovepeaceharmony.androidapp.utility.Helper
import org.lovepeaceharmony.androidapp.utility.http.LPHUrl
import retrofit2.Response
import retrofit2.http.*

/**
 * LPHService
 * Created by Olay G on 08/18/21.
 */
interface LPHServiceRetro {

    @FormUrlEncoded
    @POST(LPHUrl.LOGIN_PATH)
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("source") source: String = Helper.getSource(Constants.LoginType.Email)
    ): Response<AuthenticationResponse>

    @FormUrlEncoded
    @POST(LPHUrl.LOGOUT_PATH)
    suspend fun logout(
        @Field("deviceToken") deviceToken: String, @Header("Authorization") token: String
    ): Response<LogoutResponse>

}