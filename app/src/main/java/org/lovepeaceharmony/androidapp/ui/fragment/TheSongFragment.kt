package org.lovepeaceharmony.androidapp.ui.fragment


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.utility.Helper

/**
 * A simple [Fragment] subclass.
 * Use the [TheSongFragment.newInstance] factory method to
 * create an instance of this fragment.
 * Created by Naveen Kumar M on 07/12/17.
 */
class TheSongFragment : Fragment(), YouTubePlayer.OnInitializedListener {

    private var yPlayer: YouTubePlayer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_the_song, container, false)
        //        YouTubePlayerSupportFragment youTubePlayerFragment = (YouTubePlayerSupportFragment) view.findFragmentById(R.id.youtube_fragment);

        val youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance()
        val transaction = childFragmentManager.beginTransaction()
        transaction.add(R.id.youtube_fragment, youTubePlayerFragment).commit()
        youTubePlayerFragment.initialize(DEVELOPER_KEY, this)

        val tvContent = view.findViewById<TextView>(R.id.tv_content)
        val songText = "<B>Join together with 1.5 billion people to chant <i>Love, Peace and Harmony</i> by 2020.</B> <br><br>" +
                "        When many people join hearts and souls together to chant and meditate, this automatically creates a powerful field. We become what we chant, so as we chant to create world <i>love, peace and harmony</i>, each of us is transforming the message we carry within.<br><br>" +
                "<i>Love, Peace and Harmony</i>, a song received by Dr. and Master Sha from the Divine, carries the high frequency and vibration of love, forgiveness, compassion and light. Your body naturally resonates with this elevated frequency, and responds by activating <i>love, peace and harmony</i> throughout your being.<br><br>" +
                "By 2020, we hope to inspire 1.5 billion people to sing <i>Love, Peace and Harmony</i> for 15 minutes every day. Your participation is absolutely essential to create world <i>love, peace and harmony</i>.<br><br>" +
                "Together, we’ll celebrate the power of the human spirit and its ability to transcend, transform and triumph to create miracles. Imagine a world where everyone holds the message of <i>love, peace and harmony</i>.<br>"
        tvContent.text = Helper.fromHtml(songText)
        return view
    }

    override fun onInitializationSuccess(provider: YouTubePlayer.Provider, youTubePlayer: YouTubePlayer, wasRestored: Boolean) {
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

    override fun onInitializationFailure(provider: YouTubePlayer.Provider, errorReason: YouTubeInitializationResult) {
        if (errorReason.isUserRecoverableError) {
            errorReason.getErrorDialog(activity!!, RECOVERY_DIALOG_REQUEST).show()
        } else {
            val errorMessage = String.format(
                    "There was an error initializing the YouTubePlayer",
                    errorReason.toString())
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
        val DEVELOPER_KEY = "AIzaSyDBuql1jOIAAasvzvm14Rvprn4PwCvx8GI"
        private val RECOVERY_DIALOG_REQUEST = 1

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
