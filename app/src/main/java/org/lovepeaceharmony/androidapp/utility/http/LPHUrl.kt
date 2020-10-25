package org.lovepeaceharmony.androidapp.utility.http

/**
 * LPHUrl
 * Created by Naveen Kumar M on 02/01/18.
 */
object LPHUrl {
    private val BASE_URL = "https://lovepeaceharmony.org/app/api/"

    var registerUrl = BASE_URL + "user/register"

    val loginUrl = BASE_URL + "user/login"

    val uerUrl = BASE_URL + "user"

    val recentNewsUrl = BASE_URL + "news/recent_news"

    val categoriesUrl = BASE_URL + "news/categories"

    val favoriteNewsUrl = BASE_URL + "news/recent_favorites"

    val catgoryNewsUrl = BASE_URL +"news/category_news"

    val markFavoriteUrl = BASE_URL + "news/mark_favorite"

    val markReadUrl = BASE_URL + "news/mark_read"

    val profilePicUploadUlr = BASE_URL + "user/profile_pic_upload"

    val getMileStoneUrl = BASE_URL + "user/get_milestone"

    val resetMileStoneUrl = BASE_URL + "user/reset_milestone"

    val updateDeviceTokenUrl = BASE_URL + "user/update_device_token"

    val updateMilestoneUrl = BASE_URL + "user/update_milestone"

    val logOutUrl = BASE_URL + "user/logout"


}