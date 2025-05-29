package org.lovepeaceharmony.androidapp.utility

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONException
import org.json.JSONObject
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.model.*
import org.lovepeaceharmony.androidapp.ui.activity.MainActivity
import org.lovepeaceharmony.androidapp.utility.http.LPHException
import org.lovepeaceharmony.androidapp.utility.http.LPHServiceFactory
import org.lovepeaceharmony.androidapp.utility.http.Response
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper
 * Created by Naveen Kumar M on 01/12/17.
 */

object Helper {

    private const val PREF_NAME = "LPH_PREFS"
    private const val KEY_FIRST_RUN = "is_first_run"
    private const val DEFAULT_FIRST_SONG = "mandarin_soul_language_and_english"

    private var mProgressDialog: ProgressDialog? = null
    const val MINUTE_INTERVAL: Long = 1000 * 60 * 1 //1 minutes

    fun isFirstRun(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_FIRST_RUN, true)
    }

    fun markFirstRunComplete(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply()
    }

    fun setDefaultSongSelection(context: Context) {
        if (isFirstRun(context)) {
            // Get all songs
            val songsList = SongsModel.getSongsModelList(context)
            songsList?.forEach { song ->
                // Set first song to enabled, all others to disabled
                val isEnabled = song.songTitle == DEFAULT_FIRST_SONG
                SongsModel.updateIsEnabled(context, song.songTitle, isEnabled)
            }
            // Mark first run as complete
            markFirstRunComplete(context)
        }
    }

    /**
     * to show alert
     * @param context Context
     * @param message String
     */
    fun showAlert(context: Context, message: String?) {

        if (message != null && !message.trim { it <= ' ' }.isEmpty()) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.getString(R.string.app_name))
            builder.setMessage(message)
            builder.setPositiveButton(android.R.string.yes, null)

            val alert = builder.create()
            alert.show()
        }
    }

    fun getRepeatText(context: Context, repeatsInts: List<Int>): String {
        val stringBuilder = StringBuilder()
        for (i in repeatsInts.indices) {
            val repeatId = repeatsInts[i]
            if (repeatId == 0) {
                stringBuilder.append(context.getString(R.string.everyday))
                break
            } else {
                if (i == 0) {
                    stringBuilder.append(repeatText(context, repeatId))
                } else {
                    stringBuilder.append(", ").append(repeatText(context, repeatId))
                }
            }
        }
        return stringBuilder.toString()
    }

    fun repeatText(context: Context, repeatId: Int): String {
        var repeatText = ""

        when (repeatId) {
            0 -> repeatText = context.getString(R.string.everyday)

            1 -> repeatText = context.getString(R.string.sun)

            2 -> repeatText = context.getString(R.string.mon)

            3 -> repeatText = context.getString(R.string.tue)

            4 -> repeatText = context.getString(R.string.wed)

            5 -> repeatText = context.getString(R.string.thu)

            6 -> repeatText = context.getString(R.string.fri)

            7 -> repeatText = context.getString(R.string.sat)

            8 -> repeatText = context.getString(R.string.off)

            else -> repeatText = ""
        }
        return repeatText
    }

    fun checkExternalStoragePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun convertFromDpToPixel(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }

    fun convertFromSpToPixel(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            dp,
            context.resources.displayMetrics
        )
    }

    /**
     * to validate Email
     *
     * @param email String
     * @return boolean
     */
    fun isNotValidEmail(email: String): Boolean {
        val emailPattern =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        var isValid = false
        if (email.isNotEmpty() && email.matches(emailPattern.toRegex())) {
            isValid = true
        }
        return !isValid
    }

    fun showConfirmationAlertTwoButton(
        context: Context,
        message: String,
        callback: ConfirmationAlertCallback
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.app_name))
        builder.setMessage(message)
        builder.setCancelable(false)
        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            callback.onPositiveButtonClick()
            dialog.dismiss()
        }

        builder.setNegativeButton(android.R.string.no) { dialog, which ->
            callback.onNegativeButtonClick()
            dialog.dismiss()
        }


        val alert = builder.create()
        alert.show()

        val neutralButton = alert.getButton(DialogInterface.BUTTON_NEGATIVE)
        neutralButton.setTextColor(ContextCompat.getColor(context, R.color.dark_grey))
    }

    fun showResetConfirmationAlert(
        context: Context,
        message: String,
        callback: ConfirmationAlertCallback
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.are_you_sure))
        builder.setMessage(message)
        builder.setCancelable(false)
        builder.setPositiveButton(R.string.erase) { dialog, which ->
            callback.onPositiveButtonClick()
            dialog.dismiss()
        }

        builder.setNegativeButton(R.string.cancel) { dialog, which ->
            callback.onNegativeButtonClick()
            dialog.dismiss()
        }

        val alert = builder.create()
        alert.show()
        val neutralButton = alert.getButton(DialogInterface.BUTTON_POSITIVE)
        neutralButton.setTextColor(ContextCompat.getColor(context, R.color.dark_grey))
    }

    fun isConnected(context: Context): Boolean {
        val connMgr = context.getSystemService(Activity.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun hideSoftKeyboard(context: Context?, view: View?) {
        if (null != context && null != view) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun getStringFromPreference(context: Context, key: String): String {
        val sharedPreferences =
            context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, "") ?: ""
    }

    fun getBooleanFromPreference(context: Context, key: String): Boolean {
        val fashdrobePreference =
            context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return fashdrobePreference.getBoolean(key, false)
    }

    fun getIntFromPreference(context: Context, key: String): Int {
        val sharedPreferences =
            context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(key, 0)
    }

    fun getFloatFromPreference(context: Context, key: String): Float {
        val sharedPreferences =
            context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getFloat(key, 0f)
    }

    fun updateMileStonePendingMinutes(context: Context, minutes: Float) {
        LPHLog.d("updateMileStonePendingMinutes $minutes")
        val cache = context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val lphConstants = cache?.edit()
        lphConstants?.putFloat(Constants.SHARED_PREF_PENDING_MINUTES, minutes)
        lphConstants?.putString(Constants.SHARED_PREF_PENDING_DATE, getCurrentGMT())
        lphConstants?.apply()
    }


    fun updateLocalMileStoneMinutes(context: Context, minutes: Float) {
        LPHLog.d("updateLocalMileStoneMinutes $minutes")
        var mileStoneMinutes =
            getIntFromPreference(context, Constants.SHARED_PREF_MILESTONE_MINUTES)
        mileStoneMinutes += minutes.toInt()
        val cache = context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val lphConstants = cache?.edit()
        lphConstants?.putInt(Constants.SHARED_PREF_MILESTONE_MINUTES, mileStoneMinutes)
        lphConstants?.apply()
    }

    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     */
    fun milliSecondsToTimer(milliseconds: Long): String {
        var finalTimerString = ""
        var secondsString = ""

        // Convert total duration into time
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours.toString() + ":"
        }

        // Prepending 0 to seconds if it is one digit
        secondsString = if (seconds < 10) {
            "0" + seconds
        } else {
            "" + seconds
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString

        // return timer string
        return finalTimerString
    }

    /**
     * Function to get Progress percentage
     * @param currentDuration long
     * @param totalDuration long
     */
    fun getProgressPercentage(currentDuration: Long, totalDuration: Long): Int {
        var percentage: Double? = 0.toDouble()

        val currentSeconds = (currentDuration / 1000).toInt().toLong()
        val totalSeconds = (totalDuration / 1000).toInt().toLong()

        // calculating percentage
        percentage = currentSeconds.toDouble() / totalSeconds * 100

        // return percentage
        return percentage.toInt()
    }

    /**
     * Function to change progress to timer
     * @param progress -
     * @param totalDuration
     * returns current duration in milliseconds
     */
    fun progressToTimer(progress: Int, totalDuration: Int): Int {
        var totalDuration = totalDuration
        var currentDuration = 0
        totalDuration = (totalDuration / 1000)
        currentDuration = (progress.toDouble() / 100 * totalDuration).toInt()

        // return current duration in milliseconds
        return currentDuration * 1000
    }

    fun getDisplayWidth(context: Context): Int {
        val displayWidth: Int
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        displayWidth = metrics.widthPixels
        return displayWidth
    }

    fun showProgressDialog(context: Context) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(context)
            mProgressDialog!!.setMessage(context.getString(R.string.loading))
            mProgressDialog!!.isIndeterminate = true
        }

        mProgressDialog!!.show()
    }

    fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }

    @Throws(PackageManager.NameNotFoundException::class)
    fun onInviteClicked(context: Context) {
        val mProgressDialog = ProgressDialog(context)
        mProgressDialog.setMessage(context.getString(R.string.loading))
        mProgressDialog.isIndeterminate = true
        mProgressDialog.show()

        LPHLog.d(
            "Invite Code : " + getStringFromPreference(
                context,
                Constants.SHARED_PREF_INVITE_COUPON
            )
        )
        val link = "https://lovepaceharmony.org/?invitedby=" + getStringFromPreference(
            context,
            Constants.SHARED_PREF_INVITE_COUPON
        )
        val userName = getStringFromPreference(context, Constants.SHARED_PREF_USER_NAME)
        val subject = String.format("%s wants you to chant now!", userName)
        LPHLog.d("Link : " + link + " package : " + context.applicationContext.packageName)
        val manager = context.packageManager
        val info = manager.getPackageInfo(context.packageName, 0)

        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse(link))
            .setDynamicLinkDomain("jf627.app.goo.gl")
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder(context.applicationContext.packageName)
                    .setMinimumVersion(info.versionCode)
                    .build()
            )
            .setIosParameters(
                DynamicLink.IosParameters.Builder("org.lovepeaceharmony.ios.app")
                    .setAppStoreId("1355584112")
                    .setMinimumVersion("1.0")
                    .build()
            )
            .setSocialMetaTagParameters(
                DynamicLink.SocialMetaTagParameters.Builder()
                    .setTitle(subject)
                    .setDescription("Let's chant together! Use Love Peace Harmony")
                    .setImageUrl(Uri.parse("https://lovepeaceharmony.org/content/uploads/2015/10/LPH_1170x440.jpg"))
                    .build()
            )
            .buildShortDynamicLink()
            .addOnSuccessListener { shortDynamicLink ->
                mProgressDialog.hide()
                shortDynamicLink.shortLink?.let { startInvite(it, subject, context) }
            }
    }

    private fun startInvite(mInvitationUrl: Uri, subject: String, context: Context) {


        val invitationLink = mInvitationUrl.toString()
        val msg = "Let's chant together! Use Love Peace Harmony link: " + invitationLink
        val msgHtml = String.format(
            "<p>Let's chant together! Use " + "<a href=\"%s\">Love Peace Harmony </a>!</p>",
            invitationLink
        )

        LPHLog.d("Invitation Link : " + invitationLink)


        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"

        /*val resInfoList: List<ResolveInfo> = context.packageManager.queryIntentActivities(intent, 0)
        if(!resInfoList.isEmpty()){
            for(resInfo: ResolveInfo in resInfoList) {
                val packageName = resInfo.activityInfo.packageName
                LPHLog.d("packageName : " + packageName)
            }
        }*/

        //        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, msg)
        intent.putExtra(Intent.EXTRA_HTML_TEXT, msgHtml)
        context.startActivity(intent)
        /*if (intent.resolveActivity(context.getPackageManager()) != null) {
            startActivity(intent);
        }*/
    }


    fun getSource(loginType: Constants.LoginType): String {

        val source: String

        when (loginType) {
            Constants.LoginType.Facebook -> {
                source = "facebook"
            }

            Constants.LoginType.Email -> {
                source = "email"
            }

            Constants.LoginType.Google -> {
                source = "google"
            }

            Constants.LoginType.WithoutEmail -> {
                source = "without_email"
            }

        }

        return source
    }

    fun isLoggedInUser(context: Context): Boolean {
        var isLoggedInUser = false
        val isLoggedIn =
            Helper.getBooleanFromPreference(context = context, key = Constants.SHARED_PREF_IS_LOGIN)
        val loginType = Helper.getStringFromPreference(
            context = context,
            key = Constants.SHARED_PREF_LOGIN_TYPE
        )
        if (isLoggedIn && loginType != Constants.LoginType.WithoutEmail.name) {
            isLoggedInUser = true
        }
        return Firebase.auth.currentUser != null
    }


    private fun getCurrentGMT(): String {
        val time = Calendar.getInstance().time
        val outPutFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        LPHLog.d("Current Time in India : " + outPutFormat.format(time))
        outPutFormat.timeZone = TimeZone.getTimeZone("GMT")
        LPHLog.d("Current Time in Global : " + outPutFormat.format(time))
        return outPutFormat.format(time)
    }

    private fun getCurrentServerTime(): String {
        val time = Calendar.getInstance().time
        val outPutFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
        LPHLog.d("Current Time in India : " + outPutFormat.format(time))
        outPutFormat.timeZone = TimeZone.getTimeZone("GMT")
        LPHLog.d("Current Time in Global : " + outPutFormat.format(time))
        return outPutFormat.format(time)
    }


    fun callUpdateMileStoneAsync(context: WeakReference<Context>, minutes: Float) {
        if (minutes > 0f && isConnected(context.get()!!)) {
            LPHLog.d("Pending Seconds : ${minutes.div(60).toInt()}")
            var chantingDate =
                getStringFromPreference(context.get()!!, Constants.SHARED_PREF_PENDING_DATE)
            if (chantingDate == "" || chantingDate.isEmpty()) chantingDate = getCurrentServerTime()

            Firebase.auth.currentUser?.let {
                LPHLog.d("callUpdateMileStoneAsync $minutes && user is user:${it.uid} && chantingDate is $chantingDate")
                Firebase.database.reference
                    .child("user:${it.uid}")
                    .child("chanting_milestone")
                    .child(chantingDate).setValue(ChantingMilestone(chantingDate, minutes.div(60).toLong()))
            }
        }
    }

    fun numberWithSuffix(count: Long): String {
        if (count < 1000) return "" + count
        val exp = (Math.log(count.toDouble()) / Math.log(1000.0)).toInt()
        return String.format(
            "%.0f%c",
            count / Math.pow(1000.0, exp.toDouble()),
            "kMGTPE"[exp - 1]
        )
    }

    private fun makeDecimal(`val`: Long, div: Long, sfx: String): String {
        var `val` = `val`
        `val` = `val` / (div / 10)
        val whole = `val` / 10
        val tenths = `val` % 10
        return if (tenths == 0L || whole >= 10) String.format(
            "%d%s",
            whole,
            sfx
        ) else String.format("%d.%d%s", whole, tenths, sfx)
    }


    fun formatNumberToText(`val`: Long): String {
        val THOU = 1000L
        val MILL = 1000000L
        val BILL = 1000000000L
        val TRIL = 1000000000000L
        val QUAD = 1000000000000000L
        val QUIN = 1000000000000000000L

        if (`val` < THOU) return java.lang.Long.toString(`val`)
        if (`val` < MILL) return makeDecimal(`val`, THOU, "k")
        if (`val` < BILL) return makeDecimal(`val`, MILL, "m")
        if (`val` < TRIL) return makeDecimal(`val`, BILL, "b")
        if (`val` < QUAD) return makeDecimal(`val`, TRIL, "t")
        return if (`val` < QUIN) makeDecimal(`val`, QUAD, "q") else makeDecimal(`val`, QUIN, "u")
    }

    fun setPlayList(context: Context, path: String, extension: String) {
        try {
            val files = context.assets.list(path) ?: return
            val filesWithExtension = ArrayList<String>()

            var i = 0
            for (file in files) {
                if (file.endsWith(extension)) {
                    val song = HashMap<String, String>()
                    val songTitle = getFileName(file)

                    if (!SongsModel.isFileExist(context, songTitle)) {
                        val songsModel = SongsModel()
                        songsModel.id = i
                        songsModel.songTitle = songTitle
                        songsModel.songPath = "songs/$file"
                        // Only set default checked state for new songs
                        songsModel.isChecked = songTitle == DEFAULT_FIRST_SONG && isFirstRun(context)
                        songsModel.isToolTip = false
                        SongsModel.insertSong(context, songsModel)
                        i++
                    }
                    song.put("songTitle", file)
                    song.put("songPath", "songs/$file")
                    filesWithExtension.add(file)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getFileName(fileName: String): String {
        var fName = fileName.replace(".mp3", "")
        fName = fName.replace("01_", "")
        fName = fName.replace("02_", "")
        fName = fName.replace("04_", "")
        fName = fName.replace("05_", "")
        fName = fName.replace("06_", "")
        fName = fName.replace("07_", "")
        fName = fName.replace("08_", "")
        fName = fName.replace("09_", "")
        fName = fName.replace("10_", "")
        fName = fName.replace("11_", "")
        return fName
    }

    fun fromHtml(text: String): Spanned {
        val result: Spanned
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
        } else {
            result = Html.fromHtml(text)
        }
        return result
    }


    fun clearAllDbValues(context: Context) {
        CategoryNewsIndex.clearCategoryNewsIndex(context)
        CategoryVo.clearCategoryModel(context)
        FavoriteNewsIndex.clearFavoriteNewsIndex(context)
        NewsVo.clearNewsModel(context)
        RecentNewsIndex.clearRecentNewsIndex(context)
        SongsModel.clearSongModel(context)
        val alarmModelList = AlarmModel.getAlarmList(context)
        for (alarmModel in alarmModelList) {
            for (i in alarmModel.pendingIntentIds.indices) {
                val pendingIntentId = alarmModel.pendingIntentIds[i]
                AlarmScheduler.cancelReminder(context, AlarmReceiver::class.java, pendingIntentId)
            }
        }
        AlarmModel.clearAllAlarms(context)
    }

    class MarkFavoriteAsync internal constructor(
        context: WeakReference<Context>,
        private val newsId: Int, private val isFavorite: Boolean,
        private val onRefreshCallback: OnRefreshCallback, private val categoryId: Int
    ) : AsyncTask<Void, Void, Response<Any>>() {
        private val mWeakContext: WeakReference<Context>? = context

        override fun doInBackground(vararg p0: Void?): Response<Any> {
            var response = Response<Any>()
            try {
                val lphService = LPHServiceFactory.getCALFService(mWeakContext?.get()!!)
                val params = HashMap<String, String>()
                params[Constants.API_NEWS_ID] = newsId.toString()
                val isFavoriteVal: Int = if (isFavorite) 1 else 0
                params[Constants.API_IS_FAVORITE] = isFavoriteVal.toString()
                response = lphService.markFavorite(params)
            } catch (e: LPHException) {
                e.printStackTrace()
                response.setThrowable(e)
            } catch (e: JSONException) {
                e.printStackTrace()
                response.setThrowable(e)
            } catch (e: IOException) {
                e.printStackTrace()
                response.setThrowable(e)
            }

            return response
        }

        override fun onPostExecute(response: Response<Any>?) {
            super.onPostExecute(response)
            if (response?.isSuccess()!!) {
                NewsVo.updateIsFavorite(mWeakContext?.get()!!, newsId, isFavorite)
                if (categoryId != 0) {
                    val categoryVo = CategoryVo.getCategoryVo(mWeakContext?.get()!!, categoryId)
                    if (categoryVo != null) {
                        var favCount = categoryVo.favoriteCount
                        if (isFavorite) {
                            favCount += 1
                            CategoryVo.updateCategoryFavCount(
                                mWeakContext.get()!!,
                                categoryId,
                                favCount
                            )
                        } else if (favCount > 0) {
                            favCount -= 1
                            CategoryVo.updateCategoryFavCount(
                                mWeakContext.get()!!,
                                categoryId,
                                favCount
                            )
                        }
                    }
                }
                onRefreshCallback.onRefresh()
            } else {
                Helper.showAlert(
                    context = mWeakContext?.get()!!,
                    message = response.getServerMessage()
                )
            }
        }
    }

    class MarkReadAsync internal constructor(
        context: WeakReference<Context>,
        private val newsId: Int, private val isRead: Boolean,
        private val onRefreshCallback: OnRefreshCallback, private val categoryId: Int
    ) : AsyncTask<Void, Void, Response<Any>>() {
        private val mWeakContext: WeakReference<Context>? = context

        override fun doInBackground(vararg p0: Void?): Response<Any> {
            var response = Response<Any>()
            try {
                val lphService = LPHServiceFactory.getCALFService(mWeakContext?.get()!!)
                val params = HashMap<String, String>()
                params[Constants.API_NEWS_ID] = newsId.toString()
                val isReadVal: Int = if (isRead) 1 else 0
                params[Constants.API_IS_READ] = isReadVal.toString()
                response = lphService.markRead(params)
            } catch (e: LPHException) {
                e.printStackTrace()
                response.setThrowable(e)
            } catch (e: JSONException) {
                e.printStackTrace()
                response.setThrowable(e)
            } catch (e: IOException) {
                e.printStackTrace()
                response.setThrowable(e)
            }

            return response
        }

        override fun onPostExecute(response: Response<Any>?) {
            super.onPostExecute(response)
            if (response?.isSuccess()!!) {
                if (categoryId != 0) {
                    val categoryVo = CategoryVo.getCategoryVo(mWeakContext?.get()!!, categoryId)
                    if (categoryVo != null) {
                        var unReadCount = categoryVo.unreadCount
                        if (unReadCount > 0)
                            unReadCount -= 1
                        CategoryVo.updateCategoryReadCount(
                            mWeakContext.get()!!,
                            categoryId,
                            unReadCount
                        )
                    }
                }
                NewsVo.updateIsRead(mWeakContext?.get()!!, newsId, isRead)
                onRefreshCallback.onRefresh()
            }
        }
    }

    private class UpdateMileStoneAsync internal constructor(
        context: WeakReference<Context>,
        private val dateString: String,
        private val minutes: String
    ) : AsyncTask<Void, Void, Response<Any>>() {

        private val context: WeakReference<Context>? = context

        override fun doInBackground(vararg p0: Void?): Response<Any> {
            var response = Response<Any>()
            try {
                val lphService = LPHServiceFactory.getCALFService(context?.get()!!)
                val params = HashMap<String, String>()
                FirebaseMessaging.getInstance().token.addOnCompleteListener { fcmToken ->
                    if (fcmToken.isSuccessful.not()) return@addOnCompleteListener
                    params[Constants.API_DEVICE_TOKEN] = fcmToken.result
                    params[Constants.API_DATE] = dateString
                    params[Constants.API_MINUTES] = minutes
                    response = lphService.updateMileStone(params)
                }

            } catch (e: LPHException) {
                e.printStackTrace()
                response.setThrowable(e)
            } catch (e: JSONException) {
                e.printStackTrace()
                response.setThrowable(e)
            } catch (e: IOException) {
                e.printStackTrace()
                response.setThrowable(e)
            }

            return response
        }

        override fun onPostExecute(response: Response<Any>) {
            super.onPostExecute(response)
            if (response.isSuccess()) {
                val cache = context?.get()
                    ?.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                val lphConstants = cache?.edit()
                lphConstants?.putFloat(Constants.SHARED_PREF_PENDING_MINUTES, 0f)
                lphConstants?.apply()
                val intent1 = Intent(Constants.BROADCAST_MILESTONES)
                context?.get()!!.sendBroadcast(intent1)
            }
        }
    }

    fun isUserLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences("org.lovepeaceharmony.androidapp.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_logged_in", false)
    }

    fun formatSecondsToHMS(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

}
