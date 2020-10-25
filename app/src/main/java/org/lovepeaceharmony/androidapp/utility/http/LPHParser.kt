package org.lovepeaceharmony.androidapp.utility.http

import android.content.Context
import org.codehaus.jackson.JsonFactory
import org.codehaus.jackson.map.ObjectMapper
import org.json.JSONException
import org.json.JSONObject
import org.lovepeaceharmony.androidapp.model.*
import org.lovepeaceharmony.androidapp.utility.Constants
import java.util.*

/**
 * LPHParser
 * Created by Naveen Kumar M on 02/01/18.
 */
object LPHParser {

    @Throws(JSONException::class)
    fun isSuccess(rawResponse: String): Response<Any> {
        val response = Response<Any>()
        val jsonObject = JSONObject(rawResponse)
        val status = jsonObject.optBoolean(Constants.PARSE_SUCCESS)
        if (status)
            response.setSuccess(true)
        else {
            val message = jsonObject.optString(Constants.PARSE_MESSAGE)
            response.setSuccess(false)
            response.setServerMessage(message)
        }
        return response
    }


    fun parseNews(rawResponse: String, context: Context, newsType: NewsVo.NewsType, categoryId: Int, pageOffset: Int): Response<ArrayList<NewsVo>> {
        val response: Response<ArrayList<NewsVo>> = Response()
        val response2 = isSuccess(rawResponse)
        if(response2.isSuccess()) {
            if (pageOffset == 0) {
                when (newsType) {
                    NewsVo.NewsType.RECENT -> RecentNewsIndex.clearRecentNewsIndex(context)
                    NewsVo.NewsType.FAVORITE -> FavoriteNewsIndex.clearFavoriteNewsIndex(context)
                    NewsVo.NewsType.CATEGORY -> CategoryNewsIndex.clearCategoryNewsIndex(context)
                }
            }
            val newsArray = ArrayList<NewsVo>()
            val serverResponseJson = JSONObject(rawResponse)
            response.setMetaData(serverResponseJson.optInt(Constants.PARSE_TOTAL_NEWS_COUNT))
            val objectMapper = ObjectMapper()
            val jf = JsonFactory()
            val jp = jf.createJsonParser(serverResponseJson.getJSONArray(Constants.PARSE_DATA).toString())
            val newsVoList = objectMapper.readValue<Array<NewsVo>>(jp, Array<NewsVo>::class.java)
            for (i in newsVoList.indices) {
                val newsVo = newsVoList[i]
                newsArray.add(newsVo)
                NewsVo.insertNews(context, newsVo, newsType, categoryId)
            }
            response.setSuccess(true)
            response.setResult(newsArray)
        } else {
            response.setSuccess(false)
            response.setServerMessage(response2.getServerMessage()!!)
        }
        return response
    }

    fun parseCategories(rawResponse: String, context: Context, pageOffset: Int): Response<ArrayList<CategoryVo>> {
        val response: Response<ArrayList<CategoryVo>> = Response()
        val response2 = isSuccess(rawResponse)
        if(response2.isSuccess()) {
            if(pageOffset == 0)
                CategoryVo.clearCategoryModel(context)
            val categoryArray = ArrayList<CategoryVo>()
            val serverResponseJson = JSONObject(rawResponse)
            response.setMetaData(serverResponseJson.optInt(Constants.PARSE_TOTAL_CATEGORIES_COUNT))
            val objectMapper = ObjectMapper()
            val jf = JsonFactory()
            val jp = jf.createJsonParser(serverResponseJson.getJSONArray(Constants.PARSE_DATA).toString())
            val arrayOfCategoryVos = objectMapper.readValue<Array<CategoryVo>>(jp, Array<CategoryVo>::class.java)
            for (i in arrayOfCategoryVos.indices) {
                val categoryVo = arrayOfCategoryVos[i]
                categoryArray.add(categoryVo)
                CategoryVo.insertCategory(context, categoryVo)
            }
            response.setSuccess(true)
            response.setResult(categoryArray)
        } else {
            response.setSuccess(false)
            response.setServerMessage(response2.getServerMessage()!!)
        }
        return response
    }

}