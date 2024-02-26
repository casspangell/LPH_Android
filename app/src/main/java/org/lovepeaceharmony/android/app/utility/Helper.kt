package org.lovepeaceharmony.android.app.utility

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.lovepeaceharmony.android.app.R
import org.lovepeaceharmony.android.app.model.*
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper
 * Created by Naveen Kumar M on 01/12/17.
 */

object Helper {

    const val MINUTE_INTERVAL: Long = 1000 * 60 * 1 //1 minutes


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

    fun checkExternalStoragePermission(context: Context?): Boolean {
        return if (context != null) {
            Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
    }

    fun getStringFromPreference(context: Context, key: String): String {
        val sharedPreferences =
            context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, "") ?: ""
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
        if (hours > 0) finalTimerString = "$hours:"

        // Prepending 0 to seconds if it is one digit
        secondsString = if (seconds < 10) "0$seconds" else seconds.toString()

        finalTimerString = "$finalTimerString$minutes:$secondsString"

        // return timer string
        return finalTimerString
    }

    /**
     * Function to get Progress percentage
     * @param currentDuration long
     * @param totalDuration long
     */
    fun getProgressPercentage(currentDuration: Long, totalDuration: Long): Int {
        val currentSeconds = (currentDuration / 1000).toInt()
        val totalSeconds = (totalDuration / 1000).toInt()

        // return percentage
        return currentSeconds / totalSeconds * 100
    }

    /**
     * Function to change progress to timer
     * @param progress -
     * @param totalDuration
     * returns current duration in milliseconds
     */
    fun progressToTimer(progress: Int, totalDuration: Int): Int {
        var total = totalDuration
        var currentDuration = 0
        total = (total / 1000)
        currentDuration = (progress.toDouble() / 100 * total).toInt()

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
                        songsModel.isChecked = true
                        songsModel.isToolTip = false
                        SongsModel.insertSong(context, songsModel)
                        i++
                    }
                    song.put("songTitle", file)

                    song.put("songPath", "songs/" + file)
                    // Adding each song to SongList
                    filesWithExtension.add(file)
                }
            }

            //			return filesWithExtension.toArray(new String[filesWithExtension.size()]);
        } catch (e: IOException) {
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

}
