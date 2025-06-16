package org.lovepeaceharmony.androidapp.ui.fragment

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.database.Cursor
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.adapters.SongsAdapter
import org.lovepeaceharmony.androidapp.model.SongsModel
import org.lovepeaceharmony.androidapp.utility.Constants
import org.lovepeaceharmony.androidapp.utility.Helper
import org.lovepeaceharmony.androidapp.utility.LPHLog
import org.lovepeaceharmony.androidapp.utility.TimeTracker
import org.lovepeaceharmony.androidapp.viewmodel.MainViewModel
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the [ChantNowFragment.newInstance] factory method to
 * create an instance of this fragment.
 * Created by Naveen Kumar M on 09/11/17.
 */
@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ChantNowFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor>,
    MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener,
    AudioManager.OnAudioFocusChangeListener {

    private val viewModel by activityViewModels<MainViewModel>()

    // Media Player
    private var mp: MediaPlayer? = null

    // Handler to update UI timer, progress bar etc,.
    private val mHandler = Handler()
    private val minuteHandler = Handler()
    private var rootView: View? = null

    private val seekForwardTime = 5000 // 5000 milliseconds
    private val seekBackwardTime = 5000 // 5000 milliseconds
    private var currentSongIndex = 0
    private var isShuffle = false
    private var isRepeat = false
    private var isSongPlay = false
    private var enabledSongModelList: ArrayList<SongsModel>? = null
    private var btnPlay: ImageView? = null
    private var songCurrentDurationLabel: TextView? = null
    private var songTotalDurationLabel: TextView? = null
    private var tvNowPlaying: TextView? = null
    private var songProgressBar: SeekBar? = null
    private var volumeSeekBar: SeekBar? = null
    private var songsAdapter: SongsAdapter? = null
    private var countDownTimer: CountDownTimer? = null
    private var minutes: Float? = 0f
    private var updatingMinutes: Float? = 0f
    private lateinit var mAudioManager: AudioManager
    private lateinit var mFocusRequest: AudioFocusRequest
    private lateinit var mPlaybackAttributes: AudioAttributes
    private var shuffledSongModelList: ArrayList<SongsModel>? = null

    // Time tracking
    private var sessionStartTime: Long = 0L
    private var isSessionActive: Boolean = false
    private var sessionAccumulated: Long = 0L // seconds

    private val chantTimeHandler = Handler()
    private val chantTimeRunnable = object : Runnable {
        override fun run() {
            if (isSessionActive) {
                // Optionally, update UI via a callback or LiveData
                // For now, just keep running
                chantTimeHandler.postDelayed(this, 1000)
            }
        }
    }

    // Add this property at the class level
    private var correctDuration: Long = 0L

    /**
     * Background Runnable thread
     */
    private val mUpdateTimeTask = object : Runnable {
        override fun run() {
            if (mp != null) {
                val totalDuration = if (correctDuration > 0) correctDuration else mp!!.duration.toLong()
                val currentDuration = mp!!.currentPosition.toLong()

                val songDuration = "" + Helper.milliSecondsToTimer(totalDuration)
                val currentDur = "" + Helper.milliSecondsToTimer(currentDuration)

                if (currentDuration <= totalDuration) {
                    // Displaying Total Duration time
                    songTotalDurationLabel!!.text = songDuration
                    // Displaying time completed playing
                    songCurrentDurationLabel!!.text = currentDur

                    // Updating progress bar
                    val progress = Helper.getProgressPercentage(currentDuration, totalDuration)
                    songProgressBar!!.progress = progress
                }

                // Running this thread after 100 milliseconds
                mHandler.postDelayed(this, 100)
            }
        }
    }


    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "custom-event-name" is broad casted.
    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            updateVolume()

        }
    }


    private val mNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (mp != null && mp!!.isPlaying) {
                mp!!.pause()
                btnPlay!!.setImageResource(R.drawable.ic_play_button)
            }
        }
    }

    private val toolTipReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            LPHLog.d("Tool Tip : Tool Tip Receiver ChantNowFragment")
            SongsModel.updateIsToolTip(context, 0, true)
            restartLoader()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAudioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        enabledSongModelList = SongsModel.getEnabledSongsMadelList(requireContext())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mHandler = Handler()
            mPlaybackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            mFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(mPlaybackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setWillPauseWhenDucked(true)
                .setOnAudioFocusChangeListener(this, mHandler)
                .build()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_chant_now, container, false)
        LPHLog.d("ChantNow : InitView callled ")
        initView()
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        requireActivity().registerReceiver(
            mMessageReceiver,
            IntentFilter(Constants.BROADCAST_RECEIVER_VOLUME),
            Context.RECEIVER_NOT_EXPORTED
        )
        if (!viewModel.isToolTipShown) {
            requireContext().registerReceiver(
                toolTipReceiver,
                IntentFilter(Constants.BROADCAST_CHANT_NOW_ADAPTER),
                Context.RECEIVER_NOT_EXPORTED
            )
        }
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        requireActivity().registerReceiver(
            mNoisyReceiver,
            filter,
            Context.RECEIVER_NOT_EXPORTED
        )
        super.onActivityCreated(savedInstanceState)
    }


    override fun onDetach() {
        super.onDetach()
        requireActivity().unregisterReceiver(mMessageReceiver)
        requireActivity().unregisterReceiver(mNoisyReceiver)
        requireActivity().unregisterReceiver(toolTipReceiver)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        songProgressBar = rootView!!.findViewById(R.id.songProgressBar)
        volumeSeekBar = rootView!!.findViewById(R.id.volume_progress)
        val colorAscent = ContextCompat.getColor(requireContext(), R.color.top_bar_orange)
        songProgressBar!!.progressTintList = ColorStateList.valueOf(colorAscent)
        songProgressBar!!.thumbTintList = ColorStateList.valueOf(colorAscent)
        val recyclerView = rootView!!.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        songsAdapter =
            SongsAdapter(requireActivity(), requireContext(), object : SongsAdapter.OnSongRefresh {
                override fun onRefresh() {
                    enabledSongModelList!!.clear()
                    enabledSongModelList = SongsModel.getEnabledSongsMadelList(requireContext())
                    restartLoader()
                }

                override fun onItemClick(songTitle: String, index: Int) {
                    val songList = SongsModel.getSongsModelList(requireContext())
                    val songsModel = songList?.get(index)
                    if (songsModel!!.isChecked)
                        playSongByTitle(songTitle)
                    else
                        Toast.makeText(
                            context,
                            resources.getString(R.string.please_enable_your_song_and_play),
                            Toast.LENGTH_SHORT
                        ).show()
                }

                override fun onDisableSong(
                    songTitle: String,
                    isChecked: Boolean,
                    songsModel: SongsModel
                ) {
                    var nowPlaying = tvNowPlaying!!.text.toString().trim()
                    nowPlaying = nowPlaying.replace("Now playing: ", "")
                    if (mp != null) {
                        if (isShuffle) {
                            if (!isChecked) {
                                if (shuffledSongModelList!!.size > 0) {
                                    var selectedIndex = 0
                                    for (i in shuffledSongModelList!!.indices) {
                                        val songsModel1 = shuffledSongModelList!![i]
                                        if (songTitle == songsModel1.songTitle) {
                                            selectedIndex = i
                                        }
                                    }

                                    LPHLog.d("Selected Index : " + selectedIndex)
                                    shuffledSongModelList?.removeAt(selectedIndex)
                                }
                            } else {
                                shuffledSongModelList?.add(songsModel)
                            }

                        }
                        if (mp!!.isPlaying && !isChecked && songTitle == nowPlaying) {
                            if (Helper.isLoggedInUser(requireContext())) {
                                minutes?.let { viewModel.updateMilestone(it) }
                                updatingMinutes = 0f
                                minutes = 0f
                            }
                            mp!!.seekTo(0)
                            mp!!.stop()
                            btnPlay!!.setImageResource(R.drawable.ic_play_button)
                            currentSongIndex = 0
                            isSongPlay = false
                        }

                        setDefaultSongTitle()
                    }
                }
            })
        recyclerView.adapter = songsAdapter

        val selectedColor = ContextCompat.getColor(requireContext(), R.color.top_bar_orange)
        val greyColor = ContextCompat.getColor(requireContext(), R.color.bottom_icon_color)
        btnPlay = rootView!!.findViewById(R.id.iv_play)
        val btnForward = rootView!!.findViewById<ImageView>(R.id.iv_forward)
        val btnRepeat = rootView!!.findViewById<ImageView>(R.id.iv_repeat)
        val btnBackward = rootView!!.findViewById<ImageView>(R.id.iv_rewind)
        val btnShuffle = rootView!!.findViewById<ImageView>(R.id.iv_shuffle)
        songCurrentDurationLabel = rootView!!.findViewById(R.id.songCurrentDurationLabel)
        songTotalDurationLabel = rootView!!.findViewById(R.id.songTotalDurationLabel)
        tvNowPlaying = rootView!!.findViewById(R.id.tv_now_playing)

        // Media player
        if (mp == null) {
            mp = MediaPlayer()
            if (enabledSongModelList!!.size > 0) {
                val songName =
                    resources.getString(R.string.now_playing) + " " + enabledSongModelList!![currentSongIndex].getDisplayName()
                tvNowPlaying!!.text = songName
                tvNowPlaying!!.visibility = View.VISIBLE
            }
        } else {
            LPHLog.d("MP is not null")
            LPHLog.d("isSongPlay : " + isSongPlay)
            LPHLog.d("currentSongIndex : " + currentSongIndex)

            if (isSongPlay && shuffledSongModelList != null && isShuffle && shuffledSongModelList!!.size > 0) {
                val songName =
                    resources.getString(R.string.now_playing) + " " + shuffledSongModelList!![currentSongIndex].getDisplayName()
                tvNowPlaying!!.text = songName
                tvNowPlaying!!.visibility = View.VISIBLE
            } else if (isSongPlay && enabledSongModelList != null && enabledSongModelList!!.size > 0) {
                val songName =
                    resources.getString(R.string.now_playing) + " " + enabledSongModelList!![currentSongIndex].getDisplayName()
                tvNowPlaying!!.text = songName
                tvNowPlaying!!.visibility = View.VISIBLE
            } else {
                setDefaultSongTitle()
            }

            if (isShuffle)
                btnShuffle.setColorFilter(selectedColor)

            if (isRepeat)
                btnRepeat.setColorFilter(selectedColor)
        }

        songProgressBar!!.setOnSeekBarChangeListener(this) // Important
        mp!!.setOnCompletionListener(this)



        btnPlay!!.setOnClickListener {
            // check for already playing
            if (mp != null && mp?.isPlaying == true) {
                if (Helper.isLoggedInUser(requireContext())) {
                    minutes?.let { viewModel.updateMilestone(it) }
                    minutes = 0f
                    updatingMinutes = 0f
                }
                if (countDownTimer != null) {
                    countDownTimer?.cancel()
                    minuteHandler.removeCallbacks(minuteHandlerTask)
                }
                pausePlayer()
            } else {
                // Resume song
                if (mp != null) {
                    if (isSongPlay) {
                        LPHLog.d("ChantNow Resume Song")
                        timerStart()
                        playPlayer()
                        // Changing button image to pause button
                        btnPlay!!.setImageResource(R.drawable.ic_pause_button)
                    } else {
                        playSong(currentSongIndex)
                    }
                }
            }
        }

        /*
         * Forward button click event
         * Forwards song specified seconds
         * */
        btnForward.setOnClickListener {
            // get current song position
            if (currentSongIndex < enabledSongModelList!!.size - 1) {
                playSong(currentSongIndex + 1)
//                    currentSongIndex += 1
            } else if (isRepeat) {
                currentSongIndex = 0
                playSong(currentSongIndex)
            } else {
                Toast.makeText(
                    context,
                    context?.getString(R.string.no_next_song),
                    Toast.LENGTH_SHORT
                ).show()
            }

            /*val currentPosition = mp!!.currentPosition
            // check if seekForward time is lesser than song duration
            if (currentPosition + seekForwardTime <= mp!!.duration) {
                // forward song
                mp!!.seekTo(currentPosition + seekForwardTime)
            } else {
                // forward to end position
                mp!!.seekTo(mp!!.duration)
            }*/
        }

        /*
         * Backward button click event
         * Backward song to specified seconds
         * */
        btnBackward.setOnClickListener {
            // get current song position
            if (currentSongIndex > 0) {
                playSong(currentSongIndex - 1)
//                    currentSongIndex -= 1
            } else if (isShuffle && isRepeat) {
                currentSongIndex = (shuffledSongModelList!!.size - 1)
                playSong(currentSongIndex)
            } else if (isRepeat) {
                currentSongIndex = (enabledSongModelList!!.size - 1)
                playSong(currentSongIndex)
            } else {
                Toast.makeText(
                    context,
                    context?.getString(R.string.no_previous_song),
                    Toast.LENGTH_SHORT
                ).show()
            }

            LPHLog.d("currentSongIndex Backward : " + currentSongIndex)
            /*val currentPosition = mp!!.currentPosition
            // check if seekBackward time is greater than 0 sec
            if (currentPosition - seekBackwardTime >= 0) {
                // forward song
                mp!!.seekTo(currentPosition - seekBackwardTime)
                val progress = Helper.getProgressPercentage(mp?.currentPosition!!.toLong(), mp?.duration!!.toLong())
                //Log.d("Progress", ""+progress);
                songProgressBar!!.progress = progress
            }*/
        }/*else {
            // backward to starting position
            mp!!.seekTo(0)
        }*/

        /*
         * Button Click event for Repeat button
         * Enables repeat flag to true
         * */
        btnRepeat.setOnClickListener {
            if (isRepeat) {
                isRepeat = false
                Toast.makeText(
                    context,
                    context?.getString(R.string.repeat_turned_off),
                    Toast.LENGTH_SHORT
                ).show()
                btnRepeat.setColorFilter(greyColor)
            } else {
                if (enabledSongModelList != null && enabledSongModelList!!.size > 0) {
                    // make repeat to true
                    isRepeat = true
                    Toast.makeText(
                        context,
                        context?.getString(R.string.repeat_turned_on),
                        Toast.LENGTH_SHORT
                    ).show()
                    // make shuffle to false
//                isShuffle = false
                    btnRepeat.setColorFilter(selectedColor)
//                btnShuffle.setColorFilter(greyColor)
                } else {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.repeat_not_possible),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        /*
         * Button Click event for Shuffle button
         * Enables shuffle flag to true
         * */
        btnShuffle.setOnClickListener {
            if (isShuffle) {
                isShuffle = false
                Toast.makeText(
                    context,
                    resources.getString(R.string.shuffle_turned_off),
                    Toast.LENGTH_SHORT
                ).show()
                btnShuffle.setColorFilter(greyColor)
            } else {
                shuffledSongModelList = ArrayList()
                if (enabledSongModelList != null && enabledSongModelList!!.size > 0) {
                    for (songModel: SongsModel in enabledSongModelList!!) {
                        if (songModel != enabledSongModelList!![currentSongIndex]) {
                            shuffledSongModelList?.add(songModel)
                        }
                    }
                    shuffledSongModelList?.shuffle()
                    shuffledSongModelList?.add(0, enabledSongModelList!![currentSongIndex])
                    for (songModel: SongsModel in shuffledSongModelList!!) {
                        LPHLog.d("NAME: " + songModel.songTitle)
                    }

                    // make shuffle to true
                    isShuffle = true
                    Toast.makeText(
                        context,
                        context?.getString(R.string.shuffle_turned_on),
                        Toast.LENGTH_SHORT
                    ).show()
                    // make repeat to false
//                isRepeat = false
                    btnShuffle.setColorFilter(selectedColor)
//                    btnRepeat.setColorFilter(greyColor)
                } else {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.shuffle_not_possible),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }

        updateVolume()

        LoaderManager.getInstance(this).initLoader(Constants.URL_SONG_LOADER, null, this)

    }


    private fun setDefaultSongTitle() {
        if (shuffledSongModelList != null && isShuffle && shuffledSongModelList!!.size > 0) {
            val songName =
                resources.getString(R.string.now_playing) + " " + shuffledSongModelList!![currentSongIndex].getDisplayName()
            tvNowPlaying!!.text = songName
            tvNowPlaying!!.visibility = View.VISIBLE
        } else if (isShuffle && shuffledSongModelList!!.size == 0) {
            tvNowPlaying!!.visibility = View.GONE
        } else if (enabledSongModelList!!.size > 0) {
            val songName =
                resources.getString(R.string.now_playing) + " " + enabledSongModelList!![currentSongIndex].getDisplayName()
            tvNowPlaying!!.text = songName
            tvNowPlaying!!.visibility = View.VISIBLE
        } else if (enabledSongModelList!!.size == 0) {
            tvNowPlaying!!.visibility = View.GONE
        }
    }

    private fun updateVolume() {
        if (mp != null && volumeSeekBar != null) {
            val volumeLevel = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            volumeSeekBar!!.max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
//            volumeSeekBar!!.invalidate()
//            volumeSeekBar!!.progressDrawable.mutate()
            volumeSeekBar!!.progress = volumeLevel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mAudioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_UNMUTE,
                    0
                )
            } else {
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false)
            }
            mp!!.setVolume(volumeLevel.toFloat(), volumeLevel.toFloat())

            volumeSeekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
//                    val progress = seekBar.progress
                    if (progress > 0) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            mAudioManager.adjustStreamVolume(
                                AudioManager.STREAM_MUSIC,
                                AudioManager.ADJUST_UNMUTE,
                                0
                            )
                        } else {
                            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false)
                        }
                    }
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                    mp!!.setVolume(progress.toFloat(), progress.toFloat())
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mAudioManager.adjustStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_UNMUTE,
                            0
                        )
                    } else {
                        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false)
                    }
                }
            })
        }
    }


    fun restartLoader() {
        LoaderManager.getInstance(this).restartLoader(Constants.URL_SONG_LOADER, null, this)
    }

    private fun playSong(songIndex: Int) {
        // Play song
        try {
            var songPlayList = enabledSongModelList
            if (isShuffle) {
                songPlayList = shuffledSongModelList
            }
            if (songPlayList!!.size > 0) {
                currentSongIndex = songIndex
                isSongPlay = true
                mp!!.reset()
                
                // Create a temporary file to store the asset
                val tempFile = File(requireContext().cacheDir, "temp_song.mp3")
                requireContext().assets.open(songPlayList[songIndex].songPath).use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                // Use MediaMetadataRetriever to get the duration
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(tempFile.absolutePath)
                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                correctDuration = durationStr?.toLong() ?: 0L
                retriever.release()
                
                // Set the data source from the temporary file
                mp!!.setDataSource(tempFile.absolutePath)
                mp!!.prepare()
                
                playPlayer()
                timerStart()

                val songName = resources.getString(R.string.now_playing) + " " + songPlayList[songIndex].getDisplayName()
                tvNowPlaying!!.text = songName
                tvNowPlaying!!.visibility = View.VISIBLE

                // Changing Button Image to pause image
                btnPlay!!.setImageResource(R.drawable.ic_pause_button)

                // set Progress bar values
                songProgressBar!!.progress = 0
                songProgressBar!!.max = 100

                // Updating progress bar
                updateProgressBar()
                
                // Clean up the temporary file
                tempFile.delete()
            } else {
                Toast.makeText(
                    context,
                    resources.getString(R.string.please_enable_your_song_and_play),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: IllegalArgumentException) {
            LPHLog.e("IllegalArgumentException in playSong: ${e.message}")
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            LPHLog.e("IllegalStateException in playSong: ${e.message}")
            e.printStackTrace()
        } catch (e: IOException) {
            LPHLog.e("IOException in playSong: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun playSongByTitle(songTitle: String) {
        var songPlayList = enabledSongModelList
        if (isShuffle) {
            songPlayList = shuffledSongModelList
        }
        for (i in songPlayList!!.indices) {
            val songsModel = songPlayList[i]
            if (songTitle == songsModel.songTitle) {
                if (Helper.isLoggedInUser(requireContext())) {
                    Helper.updateMileStonePendingMinutes(requireContext(), minutes!!)
                    minutes?.let { viewModel.updateMilestone(it) }
                    minutes = 0f
                    updatingMinutes = 0f
                }
                playSong(i)
                break
            }
        }
    }

    /**
     * Update timer on seek bar
     */
    private fun updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100)
    }

    override fun onCompletion(mediaPlayer: MediaPlayer) {
        LPHLog.d("ChantNow On Completion is called")
        LPHLog.d("currentSongIndex OnCompletion Start : $currentSongIndex")
        if (Helper.isLoggedInUser(requireContext())) {
            minutes?.let { viewModel.updateMilestone(it) }
            minutes = 0f
            updatingMinutes = 0f
        }

        /*if (isRepeat) {
            // repeat is on play same song again
            playSong(currentSongIndex)
        } else*/ if (isShuffle) {
            // shuffle is on - play a random song
            if (shuffledSongModelList!!.size > 0) {
                if (currentSongIndex < shuffledSongModelList!!.size - 1) {
                    playSong(currentSongIndex + 1)
//                currentSongIndex += 1
                } else if (isRepeat) {
                    currentSongIndex = 0
                    playSong(currentSongIndex)
                } else {
                    btnPlay!!.setImageResource(R.drawable.ic_play_button)
                }
            } else {
                Toast.makeText(
                    context,
                    resources.getString(R.string.shuffle_not_possible),
                    Toast.LENGTH_SHORT
                ).show()
            }

            /*if (enabledSongModelList!!.size > 0) {
                var last = currentSongIndex
                val rand = Random()
                currentSongIndex = rand.nextInt(enabledSongModelList!!.size - 1 - 0 + 1) + 0
                playSong(currentSongIndex)
            } else {
                Toast.makeText(context, resources.getString(R.string.shuffle_not_possible), Toast.LENGTH_SHORT).show()
            }*/
        } else {
            LPHLog.d("ChantNow On Completion is called Else")
            // no repeat or shuffle ON - play next song
            if (currentSongIndex < enabledSongModelList!!.size - 1) {
                playSong(currentSongIndex + 1)
//                currentSongIndex += 1
            } else if (isRepeat) {
                // play first song
                currentSongIndex = 0
                playSong(currentSongIndex)
            } else {
//                mp!!.stop()
                btnPlay!!.setImageResource(R.drawable.ic_play_button)
            }


            /*else {
                // play first song
                currentSongIndex = 0
                playSong(currentSongIndex)
            }*/
        }

        LPHLog.d("currentSongIndex OnCompletion End : " + currentSongIndex)

    }

    override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {

    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask)
        val totalDuration = mp!!.duration
        val currentPosition = Helper.progressToTimer(seekBar.progress, totalDuration)

        // forward or backward to certain seconds
        mp!!.seekTo(currentPosition)

        // update timer progress again
        updateProgressBar()
    }


    override fun onStop() {
        super.onStop()
        LPHLog.d("ChantNow OnStop is called")
    }

    override fun onDestroy() {
        super.onDestroy()
        LPHLog.d("ChantNow OnDestroy ChantNow Fragment")
        if (Helper.isLoggedInUser(requireContext())) {
            Helper.updateMileStonePendingMinutes(requireContext(), minutes!!)
            minutes?.let { viewModel.updateMilestone(it) }
            minutes = 0f
            updatingMinutes = 0f
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            mAudioManager.abandonAudioFocusRequest(mFocusRequest)
        else
            mAudioManager.abandonAudioFocus(this)

        if (mp != null && mp!!.isPlaying) {
            pausePlayer()
//            mp!!.pause()
            // Changing button image to play button
            if (countDownTimer != null) {
                countDownTimer?.cancel()
                minuteHandler.removeCallbacks(minuteHandlerTask)
            }
        }
    }

    fun unRegisterThread() {
        mHandler.removeCallbacks(mUpdateTimeTask)
        minuteHandler.removeCallbacks(minuteHandlerTask)
        mp?.release()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        LPHLog.d("ChantNow onSaveInstanceState is called")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        LPHLog.d("ChantNow Finish OnDestroyView is called")
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return SongsModel.getCursorLoader(requireContext())
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        if (loader.id == Constants.URL_SONG_LOADER) {
            if (cursor != null) {
                val songsList = ArrayList<SongsModel>()
                if (cursor.moveToFirst()) {
                    do {
                        val song = SongsModel.getValueFromCursor(cursor)
                        songsList.add(song)
                    } while (cursor.moveToNext())
                }
                songsAdapter?.updateData(songsList)
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        songsAdapter?.updateData(emptyList())
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment ChantNowFragment.
         */
        fun newInstance(): ChantNowFragment {
            return ChantNowFragment()
        }

        private const val STORAGE_PERMISSION_REQUEST_CODE = 1001
    }

    private fun pausePlayer() {
        if (mp != null && mp!!.isPlaying) {
            mp?.pause()
            btnPlay!!.setImageResource(R.drawable.ic_play_button)
        }
    }


    private fun playPlayer() {
        if (mp != null && successfullyRetrievedAudioFocus()) {
            mp?.start()
            btnPlay!!.setImageResource(R.drawable.ic_pause_button)
            onAudioStart()
        }
    }


    private fun successfullyRetrievedAudioFocus(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest: Int = mAudioManager.requestAudioFocus(mFocusRequest)
            return focusRequest == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            val focusRequest: Int = mAudioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            LPHLog.d("ChantNow focusRequest : $focusRequest")
            return focusRequest == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                LPHLog.d("ChantNow AUDIOFOCUS_GAIN")
                if (mp != null) {
                    LPHLog.d("ChantNow Mp is not null")
                    playPlayer()
                } else {
                    LPHLog.d("ChantNow Mp is null")
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                LPHLog.d("ChantNow AUDIOFOCUS_LOSS")
                pausePlayer()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mAudioManager.abandonAudioFocusRequest(mFocusRequest)
                    mHandler.postDelayed(
                        mDelayedStopRunnable,
                        TimeUnit.SECONDS.toMillis(30)
                    )
                } else
                    mAudioManager.abandonAudioFocus(this)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                LPHLog.d("ChantNow AUDIOFOCUS_LOSS_TRANSIENT")
                pausePlayer()
            }
            else -> {
                LPHLog.d("ChantNow AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK")
            }
        }
    }

    private fun timerStart() {
        if (countDownTimer != null) {
            countDownTimer?.cancel()
            minuteHandler.removeCallbacks(minuteHandlerTask)
        }
        if (Helper.isLoggedInUser(requireContext())) {
            minuteHandler.postDelayed(minuteHandlerTask, Helper.MINUTE_INTERVAL)
            countDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
                override fun onTick(milliTillFinish: Long) {
                    val seconds = (Long.MAX_VALUE - milliTillFinish) / 1000
                    minutes = (seconds * 0.0166667).toFloat()
                    updatingMinutes = (seconds * 0.0166667).toFloat()
                }

                override fun onFinish() {
                }
            }
            countDownTimer?.start()
        }
    }

    private val minuteHandlerTask = object : Runnable {
        override fun run() {
            try {
                if (Helper.isLoggedInUser(requireContext())) {
                    Helper.updateMileStonePendingMinutes(requireContext(), updatingMinutes!!)
                    Helper.updateLocalMileStoneMinutes(requireContext(), updatingMinutes!!)
                    val intent1 = Intent(Constants.BROADCAST_MILESTONES)
                    context?.sendBroadcast(intent1)
                    updatingMinutes = 0f
                    minuteHandler.postDelayed(this, Helper.MINUTE_INTERVAL)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val mDelayedStopRunnable = Runnable {
        // Need to pause mediaplayer
        LPHLog.d("Need to stop media player")
    }

    private fun showStoragePermissionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.permission_required)
            .setMessage(R.string.storage_permission_settings_explanation)
            .setPositiveButton(R.string.open_settings) { _, _ ->
                openAppSettings()
            }
            .setNegativeButton(R.string.try_again) { dialog, _ ->
                dialog.dismiss()
                requestStoragePermission()
            }
            .setCancelable(false)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
        }
        startActivity(intent)
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, R.string.permission_granted, Toast.LENGTH_SHORT).show()
                // Proceed with playing audio
            } else {
                showStoragePermissionDialog()
            }
        }
    }

    private fun onAudioStart() {
        if (!isSessionActive) {
            sessionStartTime = System.currentTimeMillis()
            isSessionActive = true
            chantTimeHandler.post(chantTimeRunnable)
        }
    }

    private fun onAudioPauseOrStop() {
        if (isSessionActive) {
            val sessionSeconds = ((System.currentTimeMillis() - sessionStartTime) / 1000)
            TimeTracker.addSessionSeconds(requireContext(), sessionSeconds)
            sessionAccumulated += sessionSeconds
            isSessionActive = false
            chantTimeHandler.removeCallbacks(chantTimeRunnable)
            // Sync to Firebase
            viewModel.updateMilestone(sessionAccumulated / 60f) // updateMilestone expects minutes
            viewModel.syncChantTimeToFirebase(requireContext())
            sessionAccumulated = 0L
        }
    }

    override fun onResume() {
        super.onResume()
        if (mp != null && mp!!.isPlaying) {
            onAudioStart()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mp != null && mp!!.isPlaying) {
            onAudioPauseOrStop()
        }
    }

}
