package org.lovepeaceharmony.androidapp.utility.http

import org.json.JSONException
import org.lovepeaceharmony.androidapp.model.CategoryVo
import org.lovepeaceharmony.androidapp.model.NewsVo
import java.io.IOException
import java.util.*

/**
 * LPHService
 * Created by Naveen Kumar M on 02/01/18.
 */
interface LPHService {

    @Throws(IOException::class, JSONException::class)
    fun login(email: String, password: String, source: String): Response<Any>

    @Throws(IOException::class, JSONException::class)
    fun register(params: HashMap<String, String>): Response<Any>

    @Throws(IOException::class, JSONException::class)
    fun recentNews(pageLimit: Int, pageOffset: Int):  Response<ArrayList<NewsVo>>

    @Throws(IOException::class, JSONException::class)
    fun favoritesNews(pageLimit: Int, pageOffset: Int):  Response<ArrayList<NewsVo>>

    @Throws(IOException::class, JSONException::class)
    fun categories(pageLimit: Int, pageOffset: Int):  Response<ArrayList<CategoryVo>>

    @Throws(IOException::class, JSONException::class)
    fun categoryNews(pageLimit: Int, pageOffset: Int, categoryId: Int):  Response<ArrayList<NewsVo>>

    @Throws(IOException::class, JSONException::class)
    fun markFavorite(params: HashMap<String, String>): Response<Any>

    @Throws(IOException::class, JSONException::class)
    fun markRead(params: HashMap<String, String>): Response<Any>

    @Throws(IOException::class, JSONException::class)
    fun profilePicUpload(params: HashMap<String, String>): Response<Any>

    @Throws(IOException::class, JSONException::class)
    fun getMileStone(): Response<Any>

    @Throws(IOException::class, JSONException::class)
    fun restMileStone(): Response<Any>

    @Throws(IOException::class, JSONException::class)
    fun updateDeviceToken(params: HashMap<String, String>): Response<Any>

    @Throws(IOException::class, JSONException::class)
    fun updateMileStone(params: HashMap<String, String>): Response<Any>

    @Throws(IOException::class, JSONException::class)
    fun logOut(params: HashMap<String, String>): Response<Any>

}