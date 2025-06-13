package org.lovepeaceharmony.androidapp.ui.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.ui.activity.LocalVideoPlayerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

/**
 * A simple [Fragment] subclass.
 * Use the [TheSongFragment.newInstance] factory method to
 * create an instance of this fragment.
 * Created by Naveen Kumar M on 07/12/17.
 */
class TheSongFragment : Fragment(R.layout.fragment_the_song) {

    private val firebaseVideoUrl = "https://firebasestorage.googleapis.com/v0/b/love-peace-harmony.appspot.com/o/Video%2Fhow_to_change_the_world.mp4?alt=media&token=c926436c-5344-4bc5-a657-065c41a55de0"
    private val firebaseCoverImageUrl = "https://firebasestorage.googleapis.com/v0/b/love-peace-harmony.appspot.com/o/Video%2Fcover_image.png?alt=media&token=929ad2a6-59d7-43b7-a99d-1319084a13a0"

    object ImageCache {
        var coverBitmap: Bitmap? = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupVideoCover(view)
    }

    private fun setupVideoCover(view: View) {
        val coverImageView = view.findViewById<ImageView>(R.id.videoCoverImageView)
        val playButton = view.findViewById<Button>(R.id.playButton)

        try {
            val assetManager = requireContext().assets
            val inputStream = assetManager.open("video/cover_image.png")
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            coverImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            coverImageView.setImageResource(R.drawable.ic_video_placeholder)
        }

        playButton.setOnClickListener {
            playRemoteVideo()
        }
    }

    private fun playRemoteVideo() {
        try {
            val intent = Intent(requireContext(), LocalVideoPlayerActivity::class.java)
            intent.putExtra("video_url", firebaseVideoUrl)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("VideoPlayer", "Error launching video player: ${e.message}")
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment TheSongFragment.
         */
        fun newInstance(): TheSongFragment {
            return TheSongFragment()
        }
    }
}// Required empty public constructor
