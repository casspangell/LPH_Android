package org.lovepeaceharmony.androidapp.ui.fragment

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.lovepeaceharmony.androidapp.R

/**
 * A simple [Fragment] subclass.
 * Use the [TheSongFragment.newInstance] factory method to
 * create a new instance of this fragment.
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

        // Load and set the cover image from assets
        try {
            val inputStream = requireContext().assets.open("video/cover_image_500.png")
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            coverImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            // Fallback to placeholder if image loading fails
            coverImageView.setImageResource(R.drawable.ic_video_placeholder)
        }

        playButton.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Video is not available at this time",
                Toast.LENGTH_SHORT
            ).show()
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
}
