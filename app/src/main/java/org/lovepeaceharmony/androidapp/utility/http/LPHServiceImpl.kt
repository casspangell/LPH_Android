package org.lovepeaceharmony.androidapp.utility.http

import android.content.Context
import org.json.JSONObject
import org.lovepeaceharmony.androidapp.model.CategoryVo
import org.lovepeaceharmony.androidapp.model.NewsVo
import org.lovepeaceharmony.androidapp.utility.Constants
import org.lovepeaceharmony.androidapp.utility.Helper
import java.util.*

/**
 * LPHServiceImpl
 * Created by Naveen Kumar M on 02/01/18.
 */
class LPHServiceImpl(private var _context: Context): LPHService {

   var context: Context = _context

    init {
        this.context = _context
    }

    override fun login(email: String, password: String, source: String): Response<Any> {
        val response: Response<Any>
        val postParams = HashMap<String, String>()
        postParams.put(Constants.API_EMAIL, email)
        postParams.put(Constants.API_PASSWORD, password)
        postParams.put(Constants.API_SOURCE, source)
        val rawResponse = RestClient.httpRequest(_context, false, LPHUrl.loginUrl, postParams, RestClient.HTTPMethod.POST,"")
        response = LPHParser.isSuccess(rawResponse)
        response.setResult(rawResponse)
        return  response
    }

    override fun register(params: HashMap<String, String>): Response<Any> {
        val response: Response<Any>
        val rawResponse = RestClient.httpRequest(_context, false, LPHUrl.registerUrl, params, RestClient.HTTPMethod.POST, "")
        response = LPHParser.isSuccess(rawResponse)
        response.setResult(rawResponse)
        return response
    }

    override fun recentNews(pageLimit: Int, pageOffset: Int):  Response<ArrayList<NewsVo>> {
        val response: Response<ArrayList<NewsVo>>
        val params = HashMap<String, String>()
        params.put(Constants.API_PAGE_LIMIT, pageLimit.toString())
        params.put(Constants.API_PAGE_OFFSET, pageOffset.toString())
        val token = Helper.getStringFromPreference(context, Constants.SHARED_PREF_TOKEN)
        val rawResponse = RestClient.httpRequest(_context, false, LPHUrl.recentNewsUrl, params, RestClient.HTTPMethod.GET, token)
        response = LPHParser.parseNews(rawResponse, context, NewsVo.NewsType.RECENT, 0, pageOffset)
        return response
    }

    override fun categories(pageLimit: Int, pageOffset: Int): Response<ArrayList<CategoryVo>> {
        val response: Response<ArrayList<CategoryVo>>
        val params = HashMap<String, String>()
        params.put(Constants.API_PAGE_LIMIT, pageLimit.toString())
        params.put(Constants.API_PAGE_OFFSET, pageOffset.toString())
        val token = Helper.getStringFromPreference(context, Constants.SHARED_PREF_TOKEN)
        val rawResponse = RestClient.httpRequest(_context, false, LPHUrl.categoriesUrl, params, RestClient.HTTPMethod.GET, token)
        response = LPHParser.parseCategories(rawResponse, context, pageOffset)
        return response
    }

    override fun categoryNews(pageLimit: Int, pageOffset: Int, categoryId: Int): Response<ArrayList<NewsVo>> {
        val response: Response<ArrayList<NewsVo>>
        val params = HashMap<String, String>()
        params.put(Constants.API_PAGE_LIMIT, pageLimit.toString())
        params.put(Constants.API_PAGE_OFFSET, pageOffset.toString())
        params.put(Constants.API_CATEGORY_ID, categoryId.toString())
        val token = Helper.getStringFromPreference(context, Constants.SHARED_PREF_TOKEN)
        val rawResponse = RestClient.httpRequest(_context, false, LPHUrl.catgoryNewsUrl, params, RestClient.HTTPMethod.GET, token)
        response = LPHParser.parseNews(rawResponse, context, NewsVo.NewsType.CATEGORY, categoryId, pageOffset)
        return response
    }

    override fun favoritesNews(pageLimit: Int, pageOffset: Int): Response<ArrayList<NewsVo>> {
        val response: Response<ArrayList<NewsVo>>
        val params = HashMap<String, String>()
        params.put(Constants.API_PAGE_LIMIT, pageLimit.toString())
        params.put(Constants.API_PAGE_OFFSET, pageOffset.toString())
        val token = Helper.getStringFromPreference(context, Constants.SHARED_PREF_TOKEN)
        val rawResponse = RestClient.httpRequest(_context, false, LPHUrl.favoriteNewsUrl, params, RestClient.HTTPMethod.GET, token)
        response = LPHParser.parseNews(rawResponse, context, NewsVo.NewsType.FAVORITE, 0, pageOffset)
        return response
    }

    override fun markFavorite(params: HashMap<String, String>): Response<Any> {
        val response: Response<Any>
        val token = Helper.getStringFromPreference(context, Constants.SHARED_PREF_TOKEN)
        val rawResponse = RestClient.httpRequest(_context, false, LPHUrl.markFavoriteUrl, params, RestClient.HTTPMethod.POST, token)
        response = LPHParser.isSuccess(rawResponse)
        response.setResult(rawResponse)
        return response
    }

    override fun markRead(params: HashMap<String, String>): Response<Any> {
        val response: Response<Any>
        val token = Helper.getStringFromPreference(context, Constants.SHARED_PREF_TOKEN)
        val rawResponse = RestClient.httpRequest(_context, false, LPHUrl.markReadUrl, params, RestClient.HTTPMethod.POST, token)
        response = LPHParser.isSuccess(rawResponse)
        response.setResult(rawResponse)
        return response
    }

    override fun profilePicUpload(params: HashMap<String, String>): Response<Any> {
        val response: Response<Any>
        val token = Helper.getStringFromPreference(context, Constants.SHARED_PREF_TOKEN)
        val rawResponse = RestClient.httpRequest(_context, false, LPHUrl.profilePicUploadUlr, params, RestClient.HTTPMethod.POST, token)
        response = LPHParser.isSuccess(rawResponse)
        response.setResult(rawResponse)
        return response
    }

    override fun getMileStone(): Response<Any> {
        val response: Response<Any>
        val token = Helper.getStringFromPreference(context, Constants.SHARED_PREF_TOKEN)
        val rawResponse = RestClient.httpRequest(_context, false, LPHUrl.getMileStoneUrl, null, RestClient.HTTPMethod.GET, token)
        response = LPHParser.isSuccess(rawResponse)
        if(response.isSuccess()) {
            val res: String = rawResponse
            val jsonObj = JSONObject(res)
            val dataObj = jsonObj.getJSONObject(Constants.PARSE_DATA)
            val chantingStatObj = dataObj.optJSONObject(Constants.PARSE_CHANTING_STATS)
            val minutes = chantingStatObj.optInt(Constants.PARSE_MINUTES)
            val days = chantingStatObj.optInt(Constants.PARSE_DAYS)
            val inviteStatObj = dataObj.optJSONObject(Constants.PARSE_INVITE_STATS)
            val inviteCount = inviteStatObj.optInt(Constants.PARSE_INVITE_COUNT)
            val cache = context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
            val lphConstants = cache?.edit()
            lphConstants?.putInt(Constants.SHARED_PREF_MILESTONE_DAYS, days)
            lphConstants?.putInt(Constants.SHARED_PREF_MILESTONE_MINUTES, minutes)
            lphConstants?.putInt(Constants.SHARED_PREF_MILESTONE_INVITE_COUNT, inviteCount)
            lphConstants?.apply()
        }
        response.setResult(rawResponse)
        return response
    }

    override fun restMileStone(): Response<Any> {
        val response: Response<Any>
        val token = Helper.getStringFromPreference(context, Constants.SHARED_PREF_TOKEN)
        val rawResponse = RestClient.httpRequest(_context, false, LPHUrl.resetMileStoneUrl, null, RestClient.HTTPMethod.DELETE, token)
        response = LPHParser.isSuccess(rawResponse)
        if(response.isSuccess()) {
            val res: String = rawResponse
            val jsonObj = JSONObject(res)
            val dataObj = jsonObj.getJSONObject(Constants.PARSE_DATA)
            val chantingStatObj = dataObj.optJSONObject(Constants.PARSE_CHANTING_STATS)
            val minutes = chantingStatObj.optInt(Constants.PARSE_MINUTES)
            val days = chantingStatObj.optInt(Constants.PARSE_DAYS)
            val inviteStatObj = dataObj.optJSONObject(Constants.PARSE_INVITE_STATS)
            val inviteCount = inviteStatObj.optInt(Constants.PARSE_INVITE_COUNT)
            val cache = context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
            val lphConstants = cache?.edit()
            lphConstants?.putInt(Constants.SHARED_PREF_MILESTONE_DAYS, days)
            lphConstants?.putInt(Constants.SHARED_PREF_MILESTONE_MINUTES, minutes)
            lphConstants?.putInt(Constants.SHARED_PREF_MILESTONE_INVITE_COUNT, inviteCount)
            lphConstants?.putFloat(Constants.SHARED_PREF_PENDING_MINUTES, 0f)
            lphConstants?.apply()
        }
        response.setResult(rawResponse)
        return response
    }

    override fun updateDeviceToken(params: HashMap<String, String>): Response<Any> {
        val response: Response<Any>
        val token = Helper.getStringFromPreference(context, Constants.SHARED_PREF_TOKEN)
        val rawResponse = RestClient.httpRequest(_context, false, LPHUrl.updateDeviceTokenUrl, params, RestClient.HTTPMethod.POST, token)
        response = LPHParser.isSuccess(rawResponse)
        response.setResult(rawResponse)
        return response
    }

    override fun updateMileStone(params: HashMap<String, String>): Response<Any> {
        val response: Response<Any>
        val token = Helper.getStringFromPreference(context, Constants.SHARED_PREF_TOKEN)
        val rawResponse = RestClient.httpRequest(_context, false, LPHUrl.updateMilestoneUrl, params, RestClient.HTTPMethod.POST, token)
        response = LPHParser.isSuccess(rawResponse)
        if(response.isSuccess()) {
            val res: String = rawResponse
            val jsonObj = JSONObject(res)
            val dataObj = jsonObj.getJSONObject(Constants.PARSE_DATA)
            val chantingStatObj = dataObj.optJSONObject(Constants.PARSE_CHANTING_STATS)
            val minutes = chantingStatObj.optInt(Constants.PARSE_MINUTES)
            val days = chantingStatObj.optInt(Constants.PARSE_DAYS)
            val inviteStatObj = dataObj.optJSONObject(Constants.PARSE_INVITE_STATS)
            val inviteCount = inviteStatObj.optInt(Constants.PARSE_INVITE_COUNT)
            val cache = context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
            val lphConstants = cache?.edit()
            lphConstants?.putInt(Constants.SHARED_PREF_MILESTONE_DAYS, days)
            lphConstants?.putInt(Constants.SHARED_PREF_MILESTONE_MINUTES, minutes)
            lphConstants?.putInt(Constants.SHARED_PREF_MILESTONE_INVITE_COUNT, inviteCount)
            lphConstants?.putFloat(Constants.SHARED_PREF_PENDING_MINUTES, 0f)
            lphConstants?.apply()
        }
        response.setResult(rawResponse)
        return response
    }

    override fun logOut(params: HashMap<String, String>): Response<Any> {
        val response: Response<Any>
        val token = Helper.getStringFromPreference(context, Constants.SHARED_PREF_TOKEN)
        val rawResponse = RestClient.httpRequest(_context, false, LPHUrl.logOutUrl, params, RestClient.HTTPMethod.POST, token)
        response = LPHParser.isSuccess(rawResponse)
        response.setResult(rawResponse)
        return response
    }
}