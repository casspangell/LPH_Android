package org.lovepeaceharmony.androidapp.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.net.Uri
import androidx.loader.content.CursorLoader
import org.lovepeaceharmony.androidapp.db.ContentProviderDb
import java.util.*

/**
 * AlarmModel
 * Created by Naveen Kumar M on 01/12/17.
 */

class AlarmModel {
    var id: Int = 0

    var hour: Int = 0

    var minute: Int = 0

    lateinit var amPm: String

    lateinit var repeats: MutableList<Int>

    var isEnabled: Boolean = false

    lateinit var pendingIntentIds: MutableList<Int>

    lateinit var uriString: String

    companion object {

        private val TABLE_ALARM_MODEL = "TableAlarmModel"

        private val ID = "_id"
        private val HOUR = "Hour"
        private val MINUTE = "Minute"
        private val AM_PM = "AM_PM"
        private val REPEATS = "Repeats"
        private val IS_ENABLED = "Is_Enabled"
        private val PENDING_INTENT_IDS = "Pending_Intent_Ids"
        private val URI_STRING = "Uri_String"


        fun createAlarmTable(): String {
            return ("CREATE TABLE " + TABLE_ALARM_MODEL + " (" + ID +
                    " INTEGER PRIMARY KEY AUTOINCREMENT," + HOUR + " INTEGER," + MINUTE + " INTEGER," + AM_PM +
                    " TEXT," + REPEATS + " TEXT," + IS_ENABLED + " INTEGER," + PENDING_INTENT_IDS + " TEXT," + URI_STRING + " TEXT"
                    + ")")
        }

        fun insertAlarm(context: Context, alarmModel: AlarmModel) {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_ALARM_MODEL)
            val initialValues = convertToContentValues(alarmModel)
            context.contentResolver.insert(contentUri, initialValues)
        }

        private fun convertToContentValues(alarmModel: AlarmModel): ContentValues {
            val contentValues = ContentValues()
            contentValues.put(HOUR, alarmModel.hour)
            contentValues.put(MINUTE, alarmModel.minute)
            contentValues.put(AM_PM, alarmModel.amPm)
            contentValues.put(URI_STRING, alarmModel.uriString)

            val strRepeats = StringBuilder()
            val size = alarmModel.repeats.size - 1
            for ((i, repeatId) in alarmModel.repeats.withIndex()) {
                strRepeats.append(repeatId.toString())
                if (i != size) {
                    strRepeats.append(",")
                }
            }
            contentValues.put(REPEATS, strRepeats.toString())

            if (alarmModel.isEnabled)
                contentValues.put(IS_ENABLED, 1)
            else
                contentValues.put(IS_ENABLED, 0)

            val strPendingIntentIds = StringBuilder()
            val sizeIntents = alarmModel.pendingIntentIds.size - 1

            for ((j, pId) in alarmModel.pendingIntentIds.withIndex()) {
                strPendingIntentIds.append(pId.toString())
                if (j != sizeIntents) {
                    strPendingIntentIds.append(",")
                }
            }
            contentValues.put(PENDING_INTENT_IDS, strPendingIntentIds.toString())

            return contentValues
        }

        fun getCursorLoader(context: Context): CursorLoader {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_ALARM_MODEL)
            return CursorLoader(context, contentUri, null, null, null, null)
        }

        fun getAlarmList(context: Context): List<AlarmModel> {
            val alarmModelList = ArrayList<AlarmModel>()
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_ALARM_MODEL)
            val cursor = context.contentResolver.query(contentUri, null, null, null, null)
            try {
                if (cursor != null && cursor.count > 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            val alarmModel = getValueFromCursor(cursor)
                            alarmModelList.add(alarmModel)
                        } while (cursor.moveToNext())
                    }
                }
            } catch (e: CursorIndexOutOfBoundsException) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
            return alarmModelList
        }

        fun getSavedAlarm(context: Context, _id: Int): AlarmModel? {
            var alarmModel: AlarmModel? = null
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_ALARM_MODEL)
            val cursor = context.contentResolver.query(contentUri, null, ID + "=?", arrayOf(_id.toString()), null)
            try {
                if (cursor != null && cursor.count > 0) {
                    if (cursor.moveToFirst()) {
                        alarmModel = getValueFromCursor(cursor)

                    }
                }
            } catch (e: CursorIndexOutOfBoundsException) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }

            return alarmModel
        }


        fun getLatestId(context: Context): Int {
            var id = 0
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_ALARM_MODEL)
            val cursor = context.contentResolver.query(contentUri, null, null, null, ID + " DESC LIMIT 1")
            try {
                if (cursor != null && cursor.count > 0) {
                    if (cursor.moveToFirst()) {
                        val alarmModel = getValueFromCursor(cursor)
                        id = alarmModel.id
                    }
                }
            } catch (e: CursorIndexOutOfBoundsException) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }

            return id
        }

        fun getValueFromCursor(cursor: Cursor): AlarmModel {
            val alarmModel = AlarmModel()
            alarmModel.id = cursor.getInt(cursor.getColumnIndex(ID))
            alarmModel.hour = cursor.getInt(cursor.getColumnIndex(HOUR))
            alarmModel.minute = cursor.getInt(cursor.getColumnIndex(MINUTE))
            alarmModel.amPm = cursor.getString(cursor.getColumnIndex(AM_PM))
            alarmModel.uriString = cursor.getString(cursor.getColumnIndex(URI_STRING))
            alarmModel.repeats = ArrayList()
            alarmModel.pendingIntentIds = ArrayList()
            val repeats = cursor.getString(cursor.getColumnIndex(REPEATS))
            if (repeats.contains(",")) {
                val tokenizer = StringTokenizer(repeats, ",")
                while (tokenizer.hasMoreTokens()) {
                    alarmModel.repeats.add(Integer.parseInt(tokenizer.nextToken()))
                }
            } else {
                alarmModel.repeats.add(Integer.parseInt(repeats))
            }

            alarmModel.isEnabled = cursor.getInt(cursor.getColumnIndex(IS_ENABLED)) == 1

            val pendingIntentIds = cursor.getString(cursor.getColumnIndex(PENDING_INTENT_IDS))
            if (pendingIntentIds.contains(",")) {
                val tokenizer2 = StringTokenizer(cursor.getString(cursor.getColumnIndex(PENDING_INTENT_IDS)), ",")
                while (tokenizer2.hasMoreTokens()) {
                    alarmModel.pendingIntentIds.add(Integer.parseInt(tokenizer2.nextToken()))
                }
            } else {
                alarmModel.pendingIntentIds.add(Integer.parseInt(pendingIntentIds))
            }
            return alarmModel
        }

        fun updateAlarm(context: Context, id: Int, alarmModel: AlarmModel) {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_ALARM_MODEL)
            val contentValues = convertToContentValues(alarmModel)
            context.contentResolver.update(contentUri, contentValues, ID + "=? ", arrayOf(id.toString()))
        }

        fun updateIsEnabled(context: Context, id: Int, isEnabled: Boolean) {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_ALARM_MODEL)
            val contentValues = ContentValues()
            if (isEnabled)
                contentValues.put(IS_ENABLED, 1)
            else
                contentValues.put(IS_ENABLED, 0)
            context.contentResolver.update(contentUri, contentValues, ID + "=? ", arrayOf(id.toString()))
        }


        fun deleteAlarmById(context: Context, id: Int) {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_ALARM_MODEL)
            context.contentResolver.delete(contentUri, ID + "=? ", arrayOf(id.toString()))
        }

        fun clearAllAlarms(context: Context) {
            val contentUri = Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, TABLE_ALARM_MODEL)
            context.contentResolver.delete(contentUri, null, null)
        }
    }

}
