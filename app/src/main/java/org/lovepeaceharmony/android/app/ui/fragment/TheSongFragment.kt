package org.lovepeaceharmony.android.app.ui.fragment


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import org.lovepeaceharmony.android.app.R
import org.lovepeaceharmony.android.app.databinding.FragmentMovementBinding
import org.lovepeaceharmony.android.app.databinding.FragmentTheSongBinding
import org.lovepeaceharmony.android.app.utility.Helper

/**
 * A simple [Fragment] subclass.
 * Use the [TheSongFragment.newInstance] factory method to
 * create an instance of this fragment.
 * Created by Naveen Kumar M on 07/12/17.
 */
class TheSongFragment : Fragment(), YouTubePlayer.OnInitializedListener {

    private var yPlayer: YouTubePlayer? = null
    private var _binding: FragmentTheSongBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentTheSongBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance()
        val transaction = childFragmentManager.beginTransaction()
        transaction.add(binding.youtubeFragment.id, youTubePlayerFragment).commit()
        youTubePlayerFragment.initialize(DEVELOPER_KEY, this)
    }

    override fun onInitializationSuccess(
        provider: YouTubePlayer.Provider,
        youTubePlayer: YouTubePlayer,
        wasRestored: Boolean
    ) {
        yPlayer = youTubePlayer
        /*
 * Now that this variable yPlayer is global you can access it
 * throughout the activity, and perform all the player actions like
 * play, pause and seeking to a position by code.
 */
        if (!wasRestored) {
            yPlayer!!.cueVideo("EjMsLYlPSFU")
            yPlayer!!.setShowFullscreenButton(false)
        }

        if (wasRestored)
            yPlayer!!.play()
    }

    override fun onInitializationFailure(
        provider: YouTubePlayer.Provider,
        errorReason: YouTubeInitializationResult
    ) {
        if (errorReason.isUserRecoverableError) {
            errorReason.getErrorDialog(requireActivity(), RECOVERY_DIALOG_REQUEST).show()
        } else {
            val errorMessage = String.format(
                "There was an error initializing the YouTubePlayer",
                errorReason.toString()
            )
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            //            this.getYouTubePlayerProvider().initialize(YoutubeDeveloperKey, this);
        }
    }

    companion object {
        const val DEVELOPER_KEY = "AIzaSyDBuql1jOIAAasvzvm14Rvprn4PwCvx8GI"
        private const val RECOVERY_DIALOG_REQUEST = 1

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
