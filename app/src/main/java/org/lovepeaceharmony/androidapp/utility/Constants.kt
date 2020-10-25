package org.lovepeaceharmony.androidapp.utility

/**
 * Constants
 * Created by Naveen Kumar M on 01/12/17.
 */

object Constants {

    /*Bundle Constants*/
    const val BUNDLE_TONE_URI = "Tone_Uri"
    const val BUNDLE_PENDING_INTENT_ID = "pendingIntentId"
    const val BUNDLE_IS_FROM_EDIT = "isFromEdit"
    const val BUNDLE_ALARM_ID = "AlarmId"
    const val BUNDLE_IS_FROM_PROFILE = "isFromProfile"
    const val BUNDLE_NEWS_ID = "NewsId"
    const val BUNDLE_IS_REFRESH = "IsRefresh"
    const val BUNDLE_CATEGORY_ID = "CategoryId"
    const val BUNDLE_TAB_INDEX = "TabIndex"
    const val BUNDLE_IS_ALARM_ONCE = "IsAlarmOnce"

    /*Loader Ids*/
    const val URL_REMINDERS_LOADER = 1
    const val URL_SONG_LOADER = 2
    const val URL_RECENT_NEWS_LOADER = 3
    const val URL_FAVORITE_NEWS_LOADER = 4

    /*Activity Result Request Codes*/
    const val REQUEST_CODE_REMINDERS = 1
    const val REQUEST_CODE_TONE_PICKER = 2
    const val REQUEST_CODE_RECENT_NEWS = 3
    const val REQUEST_CODE_FAVORITE_NEWS = 4
    const val REQUEST_CODE_CATEGORY_NEWS = 5
    const val REQUEST_CODE_PROFILE_UPLOAD = 6
    const val REQUEST_CODE_CATEGORY_LIST = 7

    /*Shared Pref Name*/
    const val SHARED_PREF_NAME = "LPHSharedPreference"
    const val SHARED_PREF_IS_LOGIN = "Is_Login"
    const val SHARED_PREF_LOGIN_TYPE = "Login_Type"
    const val SHARED_PREF_TOKEN = "Token"
    const val SHARED_PREF_USER_NAME = "Name"
    const val SHARED_PREF_EMAIL = "Email"
    const val SHARED_PREF_PROFILE_PIC_URL = "ProfilePicUrl"
    const val SHARED_PREF_SOURCE = "Source"
    const val SHARED_PREF_ID = "id"
    const val SHARED_PREF_PASSWORD = "Password"
    const val SHARED_PREF_INVITE_COUPON = "InviteCoupon"
    const val SHARED_PREF_DEVICE_TOKEN = "DeviceToken"
    const val SHARED_PREF_INVITED_BY = "InvitedBy"
    const val SHARED_PREF_PENDING_MINUTES = "PendingMinutes" // used to update milestone
    const val SHARED_PREF_PENDING_DATE = "PendingDate" // used to update date
    const val SHARED_PREF_MILESTONE_DAYS = "MileStoneDays"
    const val SHARED_PREF_MILESTONE_MINUTES = "MileStoneMinutes"
    const val SHARED_PREF_MILESTONE_INVITE_COUNT = "MileStoneInviteCount"
    const val SHARED_PREF_IS_TOOL_TIP_SHOWN ="IsToolTipShown"

    /*BroadCast Urls*/
    const val BROADCAST_RECEIVER_VOLUME = "VolumeBroadCastReceiver"
    const val BROADCAST_RECENT_NEWS = "RecentNewsBroadCastReceiver"
    const val BROADCAST_CATEGORIES = "CategoriesBroadCastReceiver"
    const val BROADCAST_FAVORITE_NEWS = "FavoriteNewsBroadCastReceiver"
    const val BROADCAST_REMINDERS = "RemindersBroadCastReceiver"
    const val BROADCAST_MILESTONES = "MileStoneBroadCastReceiver"
    const val BROADCAST_CHANT_NOW_ADAPTER = "BroadCastChantNowAdapter"
    const val BROADCAST_MAIN_BOTTOM_LAYOUT = "BroadMainBottomLayout"
    const val BROADCAST_CLEAR_THREAD = "BroadClearThread"
    const val ALARM_RECEIVER_INTENT_TRIGGER = "ALARM_RECEIVER_INTENT_TRIGGER"

    /*Parsers*/
    const val PARSE_DATA = "data"
    const val PARSE_SUCCESS = "success"
    const val PARSE_MESSAGE = "message"
    const val PARSE_USER = "user"
    const val PARSE_ID = "id"
    const val PARSE_INVITE_COUPON = "invite_code"
    const val PARSE_TOTAL_NEWS_COUNT = "totalNewsCount"
    const val PARSE_CHANTING_STATS = "chanting_stats"
    const val PARSE_DAYS = "days"
    const val PARSE_MINUTES = "minutes"
    const val PARSE_INVITE_STATS = "invites_stats"
    const val PARSE_INVITE_COUNT = "invitees_count"
    const val PARSE_TOTAL_CATEGORIES_COUNT = "totalCategoriesCount"

    /*API Constants*/
    const val API_EMAIL = "email"
    const val API_PASSWORD = "password"
    const val API_NAME = "name"
    const val API_PROFILE_PIC_URL = "profile_pic_url"
    const val API_SOURCE = "source"
    const val API_PAGE_LIMIT = "page_limit"
    const val API_PAGE_OFFSET = "page_offset"
    const val API_CATEGORY_ID = "category_id"
    const val API_NEWS_ID = "news_id"
    const val API_IS_FAVORITE = "is_favorite"
    const val API_IMAGE = "image"
    const val API_TOKEN = "token"
    const val API_IS_READ = "is_read"
    const val API_DEVICE_TOKEN = "device_token"
    const val API_DEVICE_INFO = "device_info"
    const val API_INVITE_TOKEN = "invitation_code"
    const val API_DATE = "date"
    const val API_MINUTES = "minutes"



    enum class LoginType {
        WithoutEmail, Email, Facebook, Google
    }

}
