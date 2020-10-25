package org.lovepeaceharmony.androidapp.db

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri

/**
 * ContentProviderDb
 * Created by Naveen Kumar M on 30/11/17.
 */

class ContentProviderDb : ContentProvider() {

    private var databaseHelper: LPHDbHelper? = null

    override fun onCreate(): Boolean {
        context?.let { databaseHelper = LPHDbHelper(it) }
        return true
    }

    override fun delete(uri: Uri, where: String?, selectionArgs: Array<String>?): Int {
        val table = getPath(uri)
        val dataBase = databaseHelper!!.writableDatabase
        return dataBase.delete(table, where, selectionArgs)
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, contentValues: ContentValues?): Uri? {
        val result: Uri
        val table = getPath(uri)
        val dataBase = databaseHelper!!.writableDatabase
        val value = dataBase.insertWithOnConflict(table, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE)
        result = Uri.withAppendedPath(CONTENT_URI, value.toString())
        return result
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val cursor: Cursor
        val database = databaseHelper!!.readableDatabase
        val table = getPath(uri)
        cursor = database.query(table, projection, selection, selectionArgs, null, null, sortOrder)
        return cursor
    }

    override fun update(uri: Uri, values: ContentValues?, whereClause: String?, whereArgs: Array<String>?): Int {
        val result: Int
        val database = databaseHelper!!.writableDatabase
        val table = getPath(uri)
        result = database.update(table, values, whereClause, whereArgs)
        return result
    }

    companion object {
        private val AUTHORITY = "org.lovepeaceharmony.androidapp.db.contentProviderAuthorities"
        val CONTENT_URI = Uri.parse("content://" + AUTHORITY)!!

        fun getPath(uri: Uri?): String? {
            var value: String? = null
            if (null != uri) {
                value = uri.path
                value = value!!.replace("/", "")
            }
            return value
        }
    }
}
