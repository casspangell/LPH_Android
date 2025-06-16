package org.lovepeaceharmony.androidapp.utility.http

import android.content.Context

/**
 * Created by Cass Pangell on 06/15/25.
 */
object LPHServiceFactory {

    @Throws(LPHException::class)
    fun getCALFService(context: Context): LPHService {
        return LPHServiceImpl(context)
    }
}