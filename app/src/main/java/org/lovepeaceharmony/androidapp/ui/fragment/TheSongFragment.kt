package org.lovepeaceharmony.androidapp.ui.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.utility.Helper
import org.lovepeaceharmony.androidapp.ui.activity.LocalVideoPlayerActivity

/**
 * A simple [Fragment] subclass.
 * Use the [TheSongFragment.newInstance] factory method to
 * create an instance of this fragment.
 * Created by Naveen Kumar M on 07/12/17.
 */
class TheSongFragment : Fragment(R.layout.fragment_the_song) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupVideoCover(view)
    }

    private fun setupVideoCover(view: View) {
        val coverImageView = view.findViewById<ImageView>(R.id.videoCoverImageView)
        val playButton = view.findViewById<Button>(R.id.playButton)

        // Get first frame as cover
        val coverBitmap = getFirstFrameFromVideo()
        coverImageView.setImageBitmap(coverBitmap)

        // Set play button click listener
        playButton.setOnClickListener {
            playLocalVideo()
        }
    }

    private fun getFirstFrameFromVideo(): Bitmap? {
        return try {
            val afd = requireContext().assets.openFd("video/how_to_change_the_world.mp4")
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()
            afd.close()
            bitmap
        } catch (e: Exception) {
            Log.e("VideoPlayer", "Error getting video frame: ${e.message}")
            null
        }
    }

    private fun playLocalVideo() {
        try {
            // Use a custom VideoPlayerActivity for best UX, or fallback to VideoView dialog
            val intent = Intent(requireContext(), LocalVideoPlayerActivity::class.java)
            intent.putExtra("video_asset_path", "video/how_to_change_the_world.mp4")
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
