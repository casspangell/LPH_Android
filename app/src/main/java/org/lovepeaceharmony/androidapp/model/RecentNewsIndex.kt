package org.lovepeaceharmony.androidapp.model

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import org.lovepeaceharmony.androidapp.db.ContentProviderDb
import org.lovepeaceharmony.androidapp.utility.LPHLog

/**
 * RecentNewsIndex
 * Created by Naveen Kumar M on 15/12/17.
 */

class RecentNewsIndex {

    var newsId: Int = 0

    companion object {

        val TABLE_RECENT_NEWS_INDEX = "RecentNewsIndex"
        private val NEWS_ID = "_id"

        fun createRecentNewsIndexTable(): String {
            return "CREATE TABLE $TABLE_RECENT_NEWS_INDEX ($NEWS_ID INTEGER PRIMARY KEY)"
        }

        private fun convertToContentValues(recentNewsIndex: RecentNewsIndex): ContentValues {
            val contentValues = ContentValues()
            contentValues.put(NEWS_ID, recentNewsIndex.newsId)
            return contentValues
        }

        fun insertRecentNewsIndex(context: Context?, recentNewsIndex: RecentNewsIndex) {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_RECENT_NEWS_INDEX)
            val initialValues = convertToContentValues(recentNewsIndex)
            context?.contentResolver?.insert(contentUri, initialValues)
        }

        fun getAllRecentNewsIndex(context: Context): String {
            val newsIdList = StringBuilder()
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_RECENT_NEWS_INDEX)
            val cursor = context.contentResolver.query(contentUri, null, null, null, null)
            try {
                if (cursor != null && cursor.count > 0) {
                    cursor.moveToFirst()
                    var pos = 1
                    val totalCount = cursor.count
                    do {
                        newsIdList.append("\"" + cursor.getString(cursor.getColumnIndex(NEWS_ID)) + "\"")
                        if (pos != totalCount) {
                            newsIdList.append(",")
                        }
                        pos++
                    } while (cursor.moveToNext())
                }
            } finally {
                cursor?.close()
            }
            LPHLog.d("recent news index ============" + newsIdList.toString())
            return newsIdList.toString()
        }

        fun clearRecentNewsIndex(context: Context?): Int {
            var count = 0
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_RECENT_NEWS_INDEX)
            if (null != context) {
                count = context.contentResolver.delete(contentUri, null, null)
                LPHLog.d("cleared recent news index- count: " + count)
            }
            return count
        }
    }
}
