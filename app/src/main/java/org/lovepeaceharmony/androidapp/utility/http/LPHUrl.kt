package org.lovepeaceharmony.androidapp.utility.http

/**
 * Created by Cass Pangell on 06/15/25.
 */
object LPHUrl {
    const val BASE_URL = "https://lovepeaceharmony.org/app/api/"
    const val LOGIN_PATH = "user/login"
    const val LOGOUT_PATH = "user/logout"

    var registerUrl = BASE_URL + "user/register"

    const val loginUrl = BASE_URL + "user/login"

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