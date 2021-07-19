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
import java.text.SimpleDateFormat
import java.util.*



/**
 * NewsVo
 * Created by Naveen Kumar M on 15/12/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class NewsVo {

    @JsonProperty("new_id")
    var newsId: Int = 0

    @JsonProperty("news_heading")
    lateinit var newsHeading: String

    @JsonProperty("news_description")
    lateinit var newsDescription: String

    @JsonProperty("news_pic_url")
    var newsPicUrl: String? = ""

    @JsonProperty("news_date")
    lateinit var newsDate: String

    @JsonProperty("is_read")
    var isRead: Boolean = false

    @JsonProperty("is_favorite")
    var isFavorite: Boolean = false

    @JsonProperty("url")
    lateinit var newsDetailUrl: String

    enum class NewsType {
        RECENT, FAVORITE, CATEGORY
    }

    companion object {

        val TABLE_NEWS_MODEL = "TableNewsModel"
        private val NEWS_ID = "_id"
        private val NEWS_HEADING = "news_heading"
        private val NEWS_DESCRIPTION = "news_description"
        private val NEWS_PIC_URL = "news_pic_url"
        private val NEWS_DATE = "news_date"
        private val IS_READ = "is_read"
        private val IS_FAVORITE = "is_favorite"
        private val NEWS_DETAIL_URL = "url"


        fun createNewsTable(): String {
            return "CREATE TABLE " + TABLE_NEWS_MODEL + " (" + NEWS_ID + " INTEGER PRIMARY KEY," + NEWS_HEADING + " TEXT," + NEWS_DESCRIPTION + " TEXT," + NEWS_PIC_URL +
                    " TEXT," + NEWS_DETAIL_URL + " TEXT," +  NEWS_DATE + " TEXT," + IS_READ + " INTEGER," + IS_FAVORITE + " INTEGER" + ")"
        }

        fun insertNews(context: Context, newsVo: NewsVo, newsType: NewsType, categoryId: Int) {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_NEWS_MODEL)
            val initialValues = convertToContentValues(newsVo)
            context.contentResolver.insert(contentUri, initialValues)

            if (newsType == NewsType.FAVORITE || newsVo.isFavorite) {
                val favoriteNewsIndex = FavoriteNewsIndex()
                favoriteNewsIndex.newsId = newsVo.newsId
                FavoriteNewsIndex.insertFavoriteNewsIndex(context, favoriteNewsIndex)
            }

            if (newsType == NewsType.CATEGORY) {
                val categoryNewsIndex = CategoryNewsIndex()
                categoryNewsIndex.newsId = newsVo.newsId
                categoryNewsIndex.categoryId = categoryId
                CategoryNewsIndex.insertCategoryNewsIndex(context, categoryNewsIndex)
            }


            val recentNewsIndex = RecentNewsIndex()
            recentNewsIndex.newsId = newsVo.newsId
            RecentNewsIndex.insertRecentNewsIndex(context, recentNewsIndex)

        }

        private fun convertToContentValues(newsVo: NewsVo): ContentValues {
            val contentValues = ContentValues()
            contentValues.put(NEWS_ID, newsVo.newsId)
            contentValues.put(NEWS_HEADING, newsVo.newsHeading)
            contentValues.put(NEWS_DESCRIPTION, newsVo.newsDescription)
            contentValues.put(NEWS_DETAIL_URL, newsVo.newsDetailUrl)


            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("GMT")
            val date = sdf.parse(newsVo.newsDate)
            val time = date.time

            LPHLog.d("TimeStamp before : " + time)

            contentValues.put(NEWS_DATE, date.time)
            contentValues.put(NEWS_PIC_URL, newsVo.newsPicUrl)
            if (newsVo.isRead)
                contentValues.put(IS_READ, 1)
            else
                contentValues.put(IS_READ, 0)

            if (newsVo.isFavorite)
                contentValues.put(IS_FAVORITE, 1)
            else
                contentValues.put(IS_FAVORITE, 0)

            return contentValues
        }

        fun getValueFromCursor(cursor: Cursor): NewsVo {
            val newsVo = NewsVo()
            newsVo.newsId = cursor.getInt(cursor.getColumnIndex(NEWS_ID))
            newsVo.newsHeading = cursor.getString(cursor.getColumnIndex(NEWS_HEADING))
            newsVo.newsDescription = cursor.getString(cursor.getColumnIndex(NEWS_DESCRIPTION))
            newsVo.newsDetailUrl = cursor.getString(cursor.getColumnIndex(NEWS_DETAIL_URL))
            newsVo.newsPicUrl = cursor.getString(cursor.getColumnIndex(NEWS_PIC_URL))
            newsVo.newsDate = cursor.getString(cursor.getColumnIndex(NEWS_DATE))
            newsVo.isRead = cursor.getInt(cursor.getColumnIndex(IS_READ)) == 1
            newsVo.isFavorite = cursor.getInt(cursor.getColumnIndex(IS_FAVORITE)) == 1
            return newsVo
        }


        fun getCursorLoader(context: Context, newsType: NewsType, categoryId: Int): CursorLoader {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_NEWS_MODEL)
            val newsIdList: String
            when (newsType) {
                NewsType.RECENT -> newsIdList = RecentNewsIndex.getAllRecentNewsIndex(context)
                NewsType.FAVORITE -> newsIdList = FavoriteNewsIndex.getAllFavoriteNewsIndex(context)
                NewsType.CATEGORY -> newsIdList = CategoryNewsIndex.getAllCategoryNewsIndex(context, categoryId)
            }
//            val where = "$NEWS_ID in ($newsIdList) order by rowid"
            val where = "$NEWS_ID in ($newsIdList)"
            return CursorLoader(context, contentUri, null, where, null, NEWS_DATE + " DESC")
        }

        fun getNewsVo(context: Context, newsId: Int): NewsVo? {
            var newsVo: NewsVo? = null
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_NEWS_MODEL)
            val cursor = context.contentResolver.query(contentUri, null, NEWS_ID + "=?", arrayOf(newsId.toString()), NEWS_DATE + " DESC")
            try {
                if (cursor != null && cursor.count > 0) {
                    if (cursor.moveToFirst()) {
                        newsVo = getValueFromCursor(cursor)

                    }
                }
            } catch (e: CursorIndexOutOfBoundsException) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }

            return newsVo
        }

        fun updateIsFavorite(context: Context, id: Int, isFavorite: Boolean) {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_NEWS_MODEL)
            val contentValues = ContentValues()
            if (isFavorite) {
                contentValues.put(IS_FAVORITE, 1)
                val favoriteNewsIndex = FavoriteNewsIndex()
                favoriteNewsIndex.newsId = id
                FavoriteNewsIndex.insertFavoriteNewsIndex(context, favoriteNewsIndex)
            } else {
                contentValues.put(IS_FAVORITE, 0)
                FavoriteNewsIndex.deleteNewsIndex(context, id)
            }
            context.contentResolver.update(contentUri, contentValues, NEWS_ID + "=? ", arrayOf(id.toString()))
        }

        fun updateIsRead(context: Context, id: Int, isRead: Boolean) {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_NEWS_MODEL)
            val contentValues = ContentValues()
            if (isRead)
                contentValues.put(IS_READ, 1)
            else
                contentValues.put(IS_READ, 0)
            context.contentResolver.update(contentUri, contentValues, NEWS_ID + "=? ", arrayOf(id.toString()))
        }

        fun clearNewsModel(context: Context?): Int {
            var count = 0
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_NEWS_MODEL)
            if (null != context) {
                count = context.contentResolver.delete(contentUri, null, null)
                LPHLog.d("cleared news model - count: " + count)
            }
            return count
        }
    }

}
