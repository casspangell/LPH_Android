package org.lovepeaceharmony.androidapp.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.net.Uri
import androidx.loader.content.CursorLoader
import org.lovepeaceharmony.androidapp.db.ContentProviderDb
import org.lovepeaceharmony.androidapp.utility.LPHLog
import java.util.*

/**
 * SongsModel
 * Created by Naveen Kumar M on 13/12/17.
 */

class SongsModel {
    var id: Int = 0
    var songTitle: String = ""
    var songPath: String = ""
    var isChecked: Boolean = false
    var isToolTip: Boolean = false

    companion object {

        private val TABLE_SONGS_MODEL = "TableSongsModel"

        private val ID = "_id"

        private val SONG_TITLE = "Song_Title"

        private val SONG_PATH = "Song_Path"

        private val IS_CHECKED = "Is_Checked"

        private val IS_TOOL_TIP = "Is_tool_tip"

        fun createSongsModelTable(): String {
            LPHLog.d("CreateSongTable OnCreate")
            return "CREATE TABLE " + TABLE_SONGS_MODEL + " (" + ID + " INTEGER PRIMARY KEY," + SONG_TITLE + " TEXT NOT NULL UNIQUE," + SONG_PATH + " TEXT," + IS_CHECKED +
                    " INTEGER," + IS_TOOL_TIP + " INTEGER"+ ")"
        }

        fun insertSong(context: Context, songsModel: SongsModel) {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_SONGS_MODEL)
            val initialValues = convertToContentValues(songsModel)
            context.contentResolver.insert(contentUri, initialValues)
        }

        private fun convertToContentValues(songsModel: SongsModel): ContentValues {
            val contentValues = ContentValues()
            contentValues.put(ID, songsModel.id)
            contentValues.put(SONG_TITLE, songsModel.songTitle)
            contentValues.put(SONG_PATH, songsModel.songPath)
            contentValues.put(IS_TOOL_TIP, 0)
            if (songsModel.isChecked)
                contentValues.put(IS_CHECKED, 1)
            else
                contentValues.put(IS_CHECKED, 0)

            return contentValues

        }

        fun getValueFromCursor(cursor: Cursor): SongsModel {
            val songsModel = SongsModel()
            songsModel.id = cursor.getInt(cursor.getColumnIndex(ID))
            songsModel.songTitle = cursor.getString(cursor.getColumnIndex(SONG_TITLE))
            songsModel.songPath = cursor.getString(cursor.getColumnIndex(SONG_PATH))
            songsModel.isChecked = cursor.getInt(cursor.getColumnIndex(IS_CHECKED)) == 1
            songsModel.isToolTip = cursor.getInt(cursor.getColumnIndex(IS_TOOL_TIP)) == 1

            return songsModel
        }

        fun getCursorLoader(context: Context): CursorLoader {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_SONGS_MODEL)
            return CursorLoader(context, contentUri, null, null, null, null)
        }

        fun getSongsModelList(context: Context): ArrayList<SongsModel>? {
            val songsModelList = ArrayList<SongsModel>()
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_SONGS_MODEL)
            val cursor = context.contentResolver.query(contentUri, null, null, null, null)
            try {
                if (cursor != null && cursor.count > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            val songsModel = getValueFromCursor(cursor)
                            songsModelList.add(songsModel)
                        } while (cursor.moveToNext())
                    }
                }
            } catch (e: CursorIndexOutOfBoundsException) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
            return songsModelList
        }


        fun getEnabledSongsMadelList(context: Context): ArrayList<SongsModel>? {
            val songsModelList: ArrayList<SongsModel>? = ArrayList()
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_SONGS_MODEL)
            val cursor = context.contentResolver.query(contentUri, null, null, null, null)
            try {
                if (cursor != null && cursor.count > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            val songsModel = getValueFromCursor(cursor)
                            if (songsModel.isChecked)
                                songsModelList?.add(songsModel)
                        } while (cursor.moveToNext())
                    }
                }
            } catch (e: CursorIndexOutOfBoundsException) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
            return songsModelList
        }


        fun isFileExist(context: Context, songTitle: String): Boolean {
            var isFileExist = false
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_SONGS_MODEL)
            val cursor = context.contentResolver.query(contentUri, null, SONG_TITLE + " =? ", arrayOf(songTitle), null)
            try {
                if (cursor != null && cursor.count > 0) {

                    if (cursor.moveToFirst()) {
                        do {
                            val songsModel = getValueFromCursor(cursor)
                            if (songTitle == songsModel.songTitle) {
                                isFileExist = true
                                break
                            }
                        } while (cursor.moveToNext())
                    }
                }
            } catch (e: CursorIndexOutOfBoundsException) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }

            return isFileExist
        }

        fun updateIsEnabled(context: Context, songTitle: String, isEnabled: Boolean) {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_SONGS_MODEL)
            val contentValues = ContentValues()
            if (isEnabled)
                contentValues.put(IS_CHECKED, 1)
            else
                contentValues.put(IS_CHECKED, 0)
            context.contentResolver.update(contentUri, contentValues, SONG_TITLE + "=? ", arrayOf(songTitle))
        }

        fun updateIsToolTip(context: Context, songId: Int, isEnabled: Boolean) {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_SONGS_MODEL)
            val contentValues = ContentValues()
            if (isEnabled)
                contentValues.put(IS_TOOL_TIP, 1)
            else
                contentValues.put(IS_TOOL_TIP, 0)
            context.contentResolver.update(contentUri, contentValues, ID + "=? ", arrayOf(songId.toString()))
        }

        fun clearSongModel(context: Context?): Int {
            var count = 0
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_SONGS_MODEL)
            if (null != context) {
                count = context.contentResolver.delete(contentUri, null, null)
                LPHLog.d("cleared recent news index- count: " + count)
            }
            return count
        }
    }
}
