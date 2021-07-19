package org.lovepeaceharmony.androidapp.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.net.Uri
import androidx.loader.content.CursorLoader

import org.codehaus.jackson.annotate.JsonIgnoreProperties
import org.codehaus.jackson.annotate.JsonProperty
import org.lovepeaceharmony.androidapp.db.ContentProviderDb
import org.lovepeaceharmony.androidapp.utility.LPHLog

/**
 * CategoryVo
 * Created by Naveen Kumar M on 18/12/17.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class CategoryVo {

    @JsonProperty("category_id")
    var categoryId: Int = 0

    @JsonProperty("category_name")
    var categoryName: String = ""

    @JsonProperty("num_of_posts")
    var noOfPosts: Int = 0

    @JsonProperty("news_pic_url")
    var newsPicUrl: String? = ""

    @JsonProperty("unread_count")
    var unreadCount: Int = 0

    @JsonProperty("favorites_count")
    var favoriteCount: Int = 0

    companion object {

        val TABLE_CATEGORY_MODEL = "TableCategoryModel"
        private val CATEGORY_ID = "_id"
        private val CATEGORY_NAME = "category_name"
        private val NUM_OF_POSTS = "num_of_posts"
        private val NEWS_PIC_URL = "news_pic_url"
        private val UNREAD_COUNT = "unread_count"
        private val FAVORITE_COUNT = "favorites_count"

        fun createCategoryTable(): String {
            return "CREATE TABLE " + TABLE_CATEGORY_MODEL + " (" + CATEGORY_ID + " INTEGER PRIMARY KEY," + CATEGORY_NAME + " TEXT," + NUM_OF_POSTS + " INTEGER," + NEWS_PIC_URL +
                    " TEXT," + UNREAD_COUNT + " INTEGER," + FAVORITE_COUNT + " INTEGER" + ")"
        }

        fun insertCategory(context: Context, categoryVo: CategoryVo) {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CATEGORY_MODEL)
            val initialValues = convertToContentValues(categoryVo)
            context.contentResolver.insert(contentUri, initialValues)
        }

        private fun convertToContentValues(categoryVo: CategoryVo): ContentValues {
            val contentValues = ContentValues()
            contentValues.put(CATEGORY_ID, categoryVo.categoryId)
            contentValues.put(CATEGORY_NAME, categoryVo.categoryName)
            contentValues.put(NUM_OF_POSTS, categoryVo.noOfPosts)
            contentValues.put(NEWS_PIC_URL, categoryVo.newsPicUrl)
            contentValues.put(UNREAD_COUNT, categoryVo.unreadCount)
            contentValues.put(FAVORITE_COUNT, categoryVo.favoriteCount)
            return contentValues
        }

        fun getValueFromCursor(cursor: Cursor): CategoryVo {
            val categoryVo = CategoryVo()
            categoryVo.categoryId = cursor.getInt(cursor.getColumnIndex(CATEGORY_ID))
            categoryVo.categoryName = cursor.getString(cursor.getColumnIndex(CATEGORY_NAME))
            categoryVo.noOfPosts = cursor.getInt(cursor.getColumnIndex(NUM_OF_POSTS))
            categoryVo.newsPicUrl = cursor.getString(cursor.getColumnIndex(NEWS_PIC_URL))
            categoryVo.unreadCount = cursor.getInt(cursor.getColumnIndex(UNREAD_COUNT))
            categoryVo.favoriteCount = cursor.getInt(cursor.getColumnIndex(FAVORITE_COUNT))
            return categoryVo
        }

        fun getCursorLoader(context: Context): CursorLoader {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CATEGORY_MODEL)
            return CursorLoader(context, contentUri, null, null, null, null)
        }

        fun getCategoryVo(context: Context, categoryId: Int): CategoryVo? {
            var categoryVo: CategoryVo? = null
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CATEGORY_MODEL)
            val cursor = context.contentResolver.query(contentUri, null, CATEGORY_ID + "=?", arrayOf(categoryId.toString()), null)
            try {
                if (cursor != null && cursor.count > 0) {
                    if (cursor.moveToFirst()) {
                        categoryVo = getValueFromCursor(cursor)

                    }
                }
            } catch (e: CursorIndexOutOfBoundsException) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }

            return categoryVo
        }

        fun updateCategoryFavCount(context: Context, id: Int, favoriteCount: Int) {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CATEGORY_MODEL)
            val contentValues = ContentValues()
                contentValues.put(CategoryVo.FAVORITE_COUNT, favoriteCount)
            context.contentResolver.update(contentUri, contentValues, CATEGORY_ID + "=? ", arrayOf(id.toString()))
        }

        fun updateCategoryReadCount(context: Context, id: Int, unReadCount: Int) {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CATEGORY_MODEL)
            val contentValues = ContentValues()
            contentValues.put(CategoryVo.UNREAD_COUNT, unReadCount)
            context.contentResolver.update(contentUri, contentValues, CATEGORY_ID + "=? ", arrayOf(id.toString()))
        }

        fun clearCategoryModel(context: Context?): Int {
            var count = 0
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_CATEGORY_MODEL)
            if (null != context) {
                count = context.contentResolver.delete(contentUri, null, null)
                LPHLog.d("cleared category model - count: " + count)
            }
            return count
        }
    }

}
