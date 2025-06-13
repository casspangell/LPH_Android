package org.lovepeaceharmony.androidapp.utility

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import android.graphics.BitmapFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.lovepeaceharmony.androidapp.ui.fragment.TheSongFragment.ImageCache
import java.net.URL


/**
 * LPHApplication
 * Created by Naveen Kumar M on 13/12/17.
 */

@HiltAndroidApp
class LPHApplication : Application() {

    var isFromProfileFbLogin: Boolean = false
    var isFromMileStoneFbLogin: Boolean = false
    var isFromFavoriteFbLogin: Boolean = false

    override fun onCreate() {
        super.onCreate()
        LPHLog.d("LPHApplication OnCreate")
        
        // Initialize song list
        Helper.setPlayList(this, "songs", ".mp3")
        
        // Set default song selection for new users
        Helper.setDefaultSongSelection(this)

        // Preload cover image
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "https://firebasestorage.googleapis.com/v0/b/love-peace-harmony.appspot.com/o/Video%2Fcover_image.png?alt=media&token=929ad2a6-59d7-43b7-a99d-1319084a13a0"
                val input = URL(url).openStream()
                val bitmap = BitmapFactory.decodeStream(input)
                input.close()
                ImageCache.coverBitmap = bitmap
                LPHLog.d("LPHApplication", "Cover image preloaded and cached.")
            } catch (e: Exception) {
                LPHLog.e("LPHApplication", "Failed to preload cover image: ${e.message}")
            }
        }
    }

}
