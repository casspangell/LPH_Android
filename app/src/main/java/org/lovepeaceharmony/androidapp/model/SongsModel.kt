package org.lovepeaceharmony.androidapp.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.net.Uri
import android.widget.Toast
import androidx.loader.content.CursorLoader
import org.lovepeaceharmony.androidapp.R
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

    fun getDisplayName(): String {
        // Remove leading numbers and underscores (e.g., "01_")
        val cleaned = songTitle.replace(Regex("^\\d+_"), "")
        // Replace underscores with spaces
        val withSpaces = cleaned.replace("_", " ")
        // Split into words
        val words = withSpaces.split(" ")
        // Special abbreviations
        val specialCases = setOf("LPH")
        // Build display name
        return words.mapIndexed { i, word ->
            when {
                specialCases.contains(word.uppercase()) -> word.uppercase()
                word.lowercase() == "and" && i != 0 && i != words.lastIndex -> "and"
                else -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        }.joinToString(" ")
    }

    companion object {

        private const val TABLE_SONGS_MODEL = "TableSongsModel"

        private const val ID = "_id"

        private const val SONG_TITLE = "Song_Title"

        private const val SONG_PATH = "Song_Path"

        private const val IS_CHECKED = "Is_Checked"

        private const val IS_TOOL_TIP = "Is_tool_tip"

        private const val DEFAULT_FIRST_SONG = "mandarin_soul_language_and_english"

        fun createSongsModelTable(): String {
            LPHLog.d("CreateSongTable OnCreate")
            return "CREATE TABLE $TABLE_SONGS_MODEL ($ID INTEGER PRIMARY KEY, $SONG_TITLE TEXT NOT NULL UNIQUE, $SONG_PATH TEXT, $IS_CHECKED INTEGER, $IS_TOOL_TIP INTEGER)"
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
            contentValues.put(IS_CHECKED, if (songsModel.isChecked) 1 else 0)
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

            if (songsModelList.isNullOrEmpty()) {
                // Check if the default song exists before trying to enable it
                if (isFileExist(context, DEFAULT_FIRST_SONG)) {
                    enableDefaultSong(context)
                    return getEnabledSongsMadelList(context)
                } else {
                    // Insert the default song, enable it, and return the enabled list
                    val defaultSong = SongsModel().apply {
                        songTitle = DEFAULT_FIRST_SONG
                        songPath = "songs/01_mandarin_soul_language_english.mp3" // Set to new filename
                        isChecked = true
                        isToolTip = false
                    }
                    insertSong(context, defaultSong)
                    enableDefaultSong(context)
                    return getEnabledSongsMadelList(context)
                }
            }

            return songsModelList
        }

        fun isFileExist(context: Context, songTitle: String): Boolean {
            var isFileExist = false
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_SONGS_MODEL)
            val cursor = context.contentResolver.query(contentUri, null, "$SONG_TITLE =? ", arrayOf(songTitle), null)
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

        fun updateIsEnabled(context: Context, songTitle: String, isEnabled: Boolean): Boolean {
            // If trying to disable the last enabled song, prevent it
            if (!isEnabled && isLastEnabledSong(context, songTitle)) {
                Toast.makeText(context, R.string.at_least_one_song_must_be_selected, Toast.LENGTH_SHORT).show()
                return false
            }

            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_SONGS_MODEL)
            val contentValues = ContentValues()
            contentValues.put(IS_CHECKED, if (isEnabled) 1 else 0)
            context.contentResolver.update(contentUri, contentValues, "$SONG_TITLE=? ", arrayOf(songTitle))
            return true
        }

        private fun isLastEnabledSong(context: Context, songTitle: String): Boolean {
            val enabledSongs = getEnabledSongsMadelList(context)
            return enabledSongs?.size == 1 && enabledSongs[0].songTitle == songTitle
        }

        private fun enableDefaultSong(context: Context) {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_SONGS_MODEL)
            // First, check if the default song is already enabled
            val cursor = context.contentResolver.query(contentUri, null, "$SONG_TITLE=? AND $IS_CHECKED=1", arrayOf(DEFAULT_FIRST_SONG), null)
            val alreadyEnabled = cursor?.use { it.moveToFirst() } == true
            if (alreadyEnabled) {
                // Default song is already enabled, do not try to enable again or show toast
                return
            }
            // Not enabled, so enable it
            val contentValues = ContentValues()
            contentValues.put(IS_CHECKED, 1)
            val rows = context.contentResolver.update(contentUri, contentValues, "$SONG_TITLE=? ", arrayOf(DEFAULT_FIRST_SONG))
            if (rows > 0) {
                Toast.makeText(context, R.string.default_song_enabled, Toast.LENGTH_SHORT).show()
            }
        }

        fun updateIsToolTip(context: Context, songId: Int, isEnabled: Boolean) {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_SONGS_MODEL)
            val contentValues = ContentValues()
            contentValues.put(IS_TOOL_TIP, if (isEnabled) 1 else 0)
            context.contentResolver.update(contentUri, contentValues, "$ID=? ", arrayOf(songId.toString()))
        }

        fun clearSongModel(context: Context?): Int {
            var count = 0
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_SONGS_MODEL)
            if (null != context) {
                count = context.contentResolver.delete(contentUri, null, null)
                LPHLog.d("cleared recent news index- count: $count")
            }
            return count
        }
    }
}
