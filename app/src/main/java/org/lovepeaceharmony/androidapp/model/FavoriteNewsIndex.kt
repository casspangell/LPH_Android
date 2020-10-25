package org.lovepeaceharmony.androidapp.model

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import org.lovepeaceharmony.androidapp.db.ContentProviderDb
import org.lovepeaceharmony.androidapp.utility.LPHLog

/**
 * FavoriteNewsIndex
 * Created by Naveen Kumar M on 15/12/17.
 */

class FavoriteNewsIndex {

    var newsId: Int = 0

    companion object {

        val TABLE_FAVORITE_NEWS_INDEX = "FavoriteNewsIndex"
        private val NEWS_ID = "_id"

        fun createFavoriteNewsIndexTable(): String {
            return "CREATE TABLE $TABLE_FAVORITE_NEWS_INDEX ($NEWS_ID INTEGER PRIMARY KEY)"
        }

        private fun convertToContentValues(favoriteNewsIndex: FavoriteNewsIndex): ContentValues {
            val contentValues = ContentValues()
            contentValues.put(NEWS_ID, favoriteNewsIndex.newsId)
            return contentValues
        }

        fun insertFavoriteNewsIndex(context: Context?, favoriteNewsIndex: FavoriteNewsIndex) {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_FAVORITE_NEWS_INDEX)
            val initialValues = convertToContentValues(favoriteNewsIndex)
            context?.contentResolver?.insert(contentUri, initialValues)
        }

        fun getAllFavoriteNewsIndex(context: Context): String {
            val newsIdList = StringBuilder()
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_FAVORITE_NEWS_INDEX)
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
            LPHLog.d("Favorite news index ============" + newsIdList.toString())

            return newsIdList.toString()
        }

        fun deleteNewsIndex(context: Context?, newsId: Int): Int {
            var count = 0
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_FAVORITE_NEWS_INDEX)
            if (null != context) {
                count = context.contentResolver.delete(contentUri, NEWS_ID + "=? ", arrayOf(newsId.toString()))
                LPHLog.d("cleared favorite news index- count: " + count)
            }
            return count
        }

        fun clearFavoriteNewsIndex(context: Context?): Int {
            var count = 0
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_FAVORITE_NEWS_INDEX)
            if (null != context) {
                count = context.contentResolver.delete(contentUri, null, null)
                LPHLog.d("cleared favorite news index- count: " + count)
            }
            return count
        }
    }
}
