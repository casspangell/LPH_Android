package org.lovepeaceharmony.androidapp.model

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import org.lovepeaceharmony.androidapp.db.ContentProviderDb
import org.lovepeaceharmony.androidapp.utility.LPHLog

/**
 * CategoryNewsIndex
 * Created by Naveen Kumar M on 15/12/17.
 */

class CategoryNewsIndex {

    var id: Int = 0

    var newsId: Int = 0

    var categoryId: Int = 0

    companion object {

        val TABLE_CATEGORY_NEWS_INDEX = "CategoryNewsIndex"
        private val INDEX = "_id"
        private val NEWS_ID = "NewsId"
        private val CATEGORY_ID = "CategoryId"


        fun createCategoryNewsIndexTable(): String {
            return "CREATE TABLE " + TABLE_CATEGORY_NEWS_INDEX + " (" + INDEX + " INTEGER PRIMARY KEY," + NEWS_ID + " INTEGER," +
                    CATEGORY_ID + " INTEGER " + ")"
        }

        private fun convertToContentValues(categoryNewsIndex: CategoryNewsIndex): ContentValues {
            val contentValues = ContentValues()
            contentValues.put(NEWS_ID, categoryNewsIndex.newsId)
            contentValues.put(CATEGORY_ID, categoryNewsIndex.categoryId)
            return contentValues
        }

        fun insertCategoryNewsIndex(context: Context?, categoryNewsIndex: CategoryNewsIndex) {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CATEGORY_NEWS_INDEX)
            val initialValues = convertToContentValues(categoryNewsIndex)
            context?.contentResolver?.insert(contentUri, initialValues)
        }

        fun getAllCategoryNewsIndex(context: Context, categoryId: Int): String {
            val newsIdList = StringBuilder()
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CATEGORY_NEWS_INDEX)
            val cursor = context.contentResolver.query(contentUri, null, CATEGORY_ID + "=?", arrayOf(categoryId.toString()), null)
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
                cursor.close()
            }

            LPHLog.d("category news index ============" + newsIdList.toString())
            return newsIdList.toString()
        }

        fun clearCategoryNewsIndex(context: Context?): Int {
            var count = 0
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CATEGORY_NEWS_INDEX)
            if (null != context) {
                count = context.contentResolver.delete(contentUri, null, null)
                LPHLog.d("cleared category news index- count: " + count)
            }
            return count
        }
    }
}
