package org.lovepeaceharmony.androidapp.utility.http

import android.content.Context

/**
 * LPHServiceFactory
 * Created by Naveen Kumar M on 02/01/18.
 */
object LPHServiceFactory {

    @Throws(LPHException::class)
    fun getCALFService(context: Context): LPHService {
        return LPHServiceImpl(context)
    }
}