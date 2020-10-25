package org.lovepeaceharmony.androidapp.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import org.lovepeaceharmony.androidapp.model.AlarmModel
import org.lovepeaceharmony.androidapp.model.CategoryNewsIndex
import org.lovepeaceharmony.androidapp.model.CategoryVo
import org.lovepeaceharmony.androidapp.model.FavoriteNewsIndex
import org.lovepeaceharmony.androidapp.model.NewsVo
import org.lovepeaceharmony.androidapp.model.RecentNewsIndex
import org.lovepeaceharmony.androidapp.model.SongsModel
import org.lovepeaceharmony.androidapp.utility.LPHLog

/**
 * LPHDbHelper
 * Created by Naveen Kumar M on 30/11/17.
 */

class LPHDbHelper(context: Context)/*Environment.getExternalStorageDirectory() +"/"+*/ : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        LPHLog.d("LPHDbHelper OnCreate")
        sqLiteDatabase.execSQL(AlarmModel.createAlarmTable())
        sqLiteDatabase.execSQL(SongsModel.createSongsModelTable())
        sqLiteDatabase.execSQL(NewsVo.createNewsTable())
        sqLiteDatabase.execSQL(RecentNewsIndex.createRecentNewsIndexTable())
        sqLiteDatabase.execSQL(FavoriteNewsIndex.createFavoriteNewsIndexTable())
        sqLiteDatabase.execSQL(CategoryNewsIndex.createCategoryNewsIndexTable())
        sqLiteDatabase.execSQL(CategoryVo.createCategoryTable())
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        //Need to Alter table if we need any change for db changes.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + NewsVo.TABLE_NEWS_MODEL)
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RecentNewsIndex.TABLE_RECENT_NEWS_INDEX)
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoriteNewsIndex.TABLE_FAVORITE_NEWS_INDEX)
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CategoryNewsIndex.TABLE_CATEGORY_NEWS_INDEX)
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CategoryVo.TABLE_CATEGORY_MODEL)


    }

    companion object {

        private val DATABASE_NAME = "lph.sqlite"
        private val DATABASE_VERSION = 1
    }
}
