package org.lovepeaceharmony.androidapp.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.database.Cursor
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.Loader
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.adapters.SongsAdapter
import org.lovepeaceharmony.androidapp.model.SongsModel
import org.lovepeaceharmony.androidapp.utility.Constants
import org.lovepeaceharmony.androidapp.utility.Helper
import org.lovepeaceharmony.androidapp.utility.LPHLog
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the [ChantNowFragment.newInstance] factory method to
 * create an instance of this fragment.
 * Created by Naveen Kumar M on 09/11/17.
 */
class ChantNowFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor>, MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener, AudioManager.OnAudioFocusChangeListener {

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
    private  lateinit var mPlaybackAttributes: AudioAttributes
    private lateinit var phoneStateListener: PhoneStateListener
    private lateinit var telephonyManager: TelephonyManager
    private var isOnCall = false
    private var shuffledSongModelList: ArrayList<SongsModel>? = null

    /**
     * Background Runnable thread
     */
    private val mUpdateTimeTask = object : Runnable {
        override fun run() {
            if (mp != null) {
                val totalDuration = mp!!.duration.toLong()
                val currentDuration = mp!!.currentPosition.toLong()

                val songDuration = "" + Helper.milliSecondsToTimer(totalDuration)
                val currentDur = "" + Helper.milliSecondsToTimer(currentDuration)

                if(currentDuration <= totalDuration) {
                    // Displaying Total Duration time
                    songTotalDurationLabel!!.text = songDuration
                    // Displaying time completed playing
                    songCurrentDurationLabel!!.text = currentDur

                    // Updating progress bar
                    val progress = Helper.getProgressPercentage(currentDuration, totalDuration)
                    //Log.d("Progress", ""+progress);
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
            SongsModel.updateIsToolTip(context, 0, true)
            restartLoader()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAudioManager = context!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        enabledSongModelList = SongsModel.getEnabledSongsMadelList(context!!)
        callStateListener()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_chant_now, container, false)
        LPHLog.d("ChantNow : InitView callled ")
        initView()
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        activity!!.registerReceiver(mMessageReceiver, IntentFilter(Constants.BROADCAST_RECEIVER_VOLUME))
        activity!!.registerReceiver(toolTipReceiver, IntentFilter(Constants.BROADCAST_CHANT_NOW_ADAPTER))
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        activity!!.registerReceiver(mNoisyReceiver, filter)
        super.onActivityCreated(savedInstanceState)
    }


    override fun onDetach() {
        super.onDetach()
        activity!!.unregisterReceiver(mMessageReceiver)
        activity!!.unregisterReceiver(mNoisyReceiver)
        activity!!.unregisterReceiver(toolTipReceiver)
    }

    private fun initView() {
        songProgressBar = rootView!!.findViewById(R.id.songProgressBar)
        volumeSeekBar = rootView!!.findViewById(R.id.volume_progress)
        val colorAscent = ContextCompat.getColor(context!!, R.color.top_bar_orange)
        songProgressBar!!.progressTintList = ColorStateList.valueOf(colorAscent)
        songProgressBar!!.thumbTintList = ColorStateList.valueOf(colorAscent)
        val recyclerView = rootView!!.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context!!)
        songsAdapter = SongsAdapter(activity!!, context!!, null, object : SongsAdapter.OnSongRefresh {
                override fun onRefresh() {
                    enabledSongModelList!!.clear()
                    enabledSongModelList = SongsModel.getEnabledSongsMadelList(context!!)
                    restartLoader()
                }

                override fun onItemClick(songTitle: String, index: Int) {
                    val songList = SongsModel.getSongsModelList(context!!)
                    val songsModel = songList?.get(index)
                    if(songsModel!!.isChecked)
                        playSongByTitle(songTitle)
                    else
                        Toast.makeText(context, context!!.getString(R.string.please_enable_your_song), Toast.LENGTH_SHORT).show()
                }

                override fun onDisableSong(songTitle: String, isChecked: Boolean, songsModel: SongsModel) {
                    var nowPlaying = tvNowPlaying!!.text.toString().trim()
                    nowPlaying = nowPlaying.replace("Now playing: ", "")
                    if (mp != null) {
                        if(isShuffle) {
                            if(!isChecked) {
                                if(shuffledSongModelList!!.size > 0) {
                                    var selectedIndex = 0
                                    for(i in shuffledSongModelList!!.indices){
                                        val songsModel1 = shuffledSongModelList!![i]
                                        if(songTitle == songsModel1.songTitle){
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
                            if (Helper.isLoggedInUser(context!!)) {
                                val weakReferenceContext = WeakReference(context!!)
                                Helper.callUpdateMileStoneAsync(weakReferenceContext, minutes!!)
                                updatingMinutes = 0f
                                minutes = 0f
                            }
                            mp!!.seekTo(0)
                            mp!!.stop()
                            btnPlay!!.setImageResource(R.drawable.ic_play_button)
                            Toast.makeText(context, context!!.getString(R.string.please_enable_your_song), Toast.LENGTH_SHORT).show()
                            currentSongIndex = 0
                            isSongPlay = false
                        }

                        setDefaultSongTitle()
                    }
                }
            })
            recyclerView.adapter = songsAdapter

        val selectedColor = ContextCompat.getColor(context!!, R.color.top_bar_orange)
        val greyColor = ContextCompat.getColor(context!!, R.color.bottom_icon_color)
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
            if(enabledSongModelList!!.size > 0) {
                val songName = context!!.getString(R.string.now_playing) + " " + enabledSongModelList!![currentSongIndex].songTitle
                tvNowPlaying!!.text = songName
                tvNowPlaying!!.visibility = View.VISIBLE
            }
        } else {
            LPHLog.d("MP is not null")
            LPHLog.d("isSongPlay : " + isSongPlay)
            LPHLog.d("currentSongIndex : " + currentSongIndex)

            if(isSongPlay && shuffledSongModelList != null && isShuffle &&  shuffledSongModelList!!.size > 0) {
                val songName = context!!.getString(R.string.now_playing) + " " + shuffledSongModelList!![currentSongIndex].songTitle
                tvNowPlaying!!.text = songName
                tvNowPlaying!!.visibility = View.VISIBLE
            } else if(isSongPlay && enabledSongModelList != null && enabledSongModelList!!.size > 0) {
                val songName = context!!.getString(R.string.now_playing) + " " + enabledSongModelList!![currentSongIndex].songTitle
                tvNowPlaying!!.text = songName
                tvNowPlaying!!.visibility = View.VISIBLE
            } else {
                setDefaultSongTitle()
            }

            if(isShuffle)
                btnShuffle.setColorFilter(selectedColor)

            if(isRepeat)
                btnRepeat.setColorFilter(selectedColor)
        }

        songProgressBar!!.setOnSeekBarChangeListener(this) // Important
        mp!!.setOnCompletionListener(this)



        btnPlay!!.setOnClickListener {
            if (!Helper.checkExternalStoragePermission(context)) {
                Toast.makeText(context, context!!.getString(R.string.enable_storage_permission), Toast.LENGTH_SHORT).show()
            } else {
                // check for already playing
                if (mp != null && mp!!.isPlaying && !isOnCall) {
                    if (Helper.isLoggedInUser(context!!)) {
                        val weakReferenceContext = WeakReference(context!!)
                        Helper.callUpdateMileStoneAsync(weakReferenceContext, minutes!!)
                        minutes = 0f
                        updatingMinutes = 0f
                    }
                    if(countDownTimer != null) {
                        countDownTimer?.cancel()
                        minuteHandler.removeCallbacks(minuteHandlerTask)
                    }
//                    if (mp!!.isPlaying) {
                    pausePlayer()
                    // Changing button image to play button
//                    btnPlay!!.setImageResource(R.drawable.ic_play_button)

//                    }
                } else if(!isOnCall){
                    // Resume song
                    if (mp != null) {
                        if (isSongPlay) {
                            LPHLog.d("ChantNow Resume Song")
                            timerStart()
                            playPlayer()
//                            mp!!.start()
                            // Changing button image to pause button
                            btnPlay!!.setImageResource(R.drawable.ic_pause_button)
                        } else if(!isOnCall){
                            playSong(currentSongIndex)
                        }

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
            if(!isOnCall) {

                LPHLog.d("currentSongIndex Forward : "+ currentSongIndex)
                if (currentSongIndex < enabledSongModelList!!.size - 1) {
                    playSong(currentSongIndex + 1)
//                    currentSongIndex += 1
                } else if(isRepeat) {
                    currentSongIndex = 0
                    playSong(currentSongIndex)
                } else {
                    Toast.makeText(context, context?.getString(R.string.no_next_song), Toast.LENGTH_SHORT).show()
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
        }

        /*
         * Backward button click event
         * Backward song to specified seconds
         * */
        btnBackward.setOnClickListener {
            // get current song position
            if(!isOnCall) {
                LPHLog.d("currentSongIndex Backward : "+ currentSongIndex)

                if (currentSongIndex > 0) {
                    playSong(currentSongIndex - 1)
//                    currentSongIndex -= 1
                } else if(isShuffle && isRepeat) {
                    currentSongIndex = (shuffledSongModelList!!.size - 1)
                    playSong(currentSongIndex)
                } else if(isRepeat) {
                    currentSongIndex = (enabledSongModelList!!.size - 1)
                    playSong(currentSongIndex)
                } else {
                    Toast.makeText(context, context?.getString(R.string.no_previous_song), Toast.LENGTH_SHORT).show()
                }

                LPHLog.d("currentSongIndex Backward : "+ currentSongIndex)
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

        }

        /*
         * Button Click event for Repeat button
         * Enables repeat flag to true
         * */
        btnRepeat.setOnClickListener {
            if (isRepeat) {
                isRepeat = false
                Toast.makeText(context, context?.getString(R.string.repeat_is_off), Toast.LENGTH_SHORT).show()
                btnRepeat.setColorFilter(greyColor)
            } else {
                if(enabledSongModelList != null && enabledSongModelList!!.size > 0) {
                    // make repeat to true
                    isRepeat = true
                    Toast.makeText(context, context?.getString(R.string.repeat_is_on), Toast.LENGTH_SHORT).show()
                    // make shuffle to false
//                isShuffle = false
                    btnRepeat.setColorFilter(selectedColor)
//                btnShuffle.setColorFilter(greyColor)
                } else {
                    Toast.makeText(context, context!!.getString(R.string.repeat_not_possible), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, context!!.getString(R.string.shuffle_is_off), Toast.LENGTH_SHORT).show()
                btnShuffle.setColorFilter(greyColor)
            } else {
                shuffledSongModelList = ArrayList()
                if(enabledSongModelList != null && enabledSongModelList!!.size > 0) {
                    for (songModel: SongsModel in enabledSongModelList!!) {
                        if (songModel != enabledSongModelList!![currentSongIndex]) {
                            shuffledSongModelList?.add(songModel)
                        }
                    }
                    Collections.shuffle(shuffledSongModelList)
                    shuffledSongModelList?.add(0, enabledSongModelList!![currentSongIndex])
                    for(songModel: SongsModel in shuffledSongModelList!!){
                        LPHLog.d("NAME: " + songModel.songTitle)
                    }

                    // make shuffle to true
                    isShuffle = true
                    Toast.makeText(context, context?.getString(R.string.shuffle_is_on), Toast.LENGTH_SHORT).show()
                    // make repeat to false
//                isRepeat = false
                    btnShuffle.setColorFilter(selectedColor)
//                    btnRepeat.setColorFilter(greyColor)
                } else {
                    Toast.makeText(context, context!!.getString(R.string.shuffle_not_possible), Toast.LENGTH_SHORT).show()
                }

            }
        }

        updateVolume()

        loaderManager.initLoader(Constants.URL_SONG_LOADER, null, this)

    }


    private fun setDefaultSongTitle() {
        if(shuffledSongModelList != null && isShuffle &&  shuffledSongModelList!!.size > 0) {
            val songName = context!!.getString(R.string.now_playing) + " " + shuffledSongModelList!![currentSongIndex].songTitle
            tvNowPlaying!!.text = songName
            tvNowPlaying!!.visibility = View.VISIBLE
        } else if(isShuffle && shuffledSongModelList!!.size == 0) {
            tvNowPlaying!!.visibility = View.GONE
        }else if(enabledSongModelList!!.size > 0) {
            val songName = context!!.getString(R.string.now_playing) + " " + enabledSongModelList!![currentSongIndex].songTitle
            tvNowPlaying!!.text = songName
            tvNowPlaying!!.visibility = View.VISIBLE
        } else if(enabledSongModelList!!.size == 0) {
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
            } else {
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false)
            }
            mp!!.setVolume(volumeLevel.toFloat(), volumeLevel.toFloat())

            volumeSeekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
//                    val progress = seekBar.progress
                    if(progress > 0) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
                    } else {
                        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false)
                    }
                }
            })
        }
    }


    fun restartLoader() {
        loaderManager.restartLoader(Constants.URL_SONG_LOADER, null, this)
    }

    private fun playSong(songIndex: Int) {
        // Play song
        try {

            var songPlayList = enabledSongModelList
            if(isShuffle) {
                songPlayList = shuffledSongModelList
            }
            if (songPlayList!!.size > 0) {
                currentSongIndex = songIndex
                isSongPlay = true
                mp!!.reset()
                Log.d("SONG PATH : ", songPlayList[songIndex].songPath)
                val descriptor = context!!.assets.openFd(songPlayList[songIndex].songPath)
                val start = descriptor.startOffset
                val end = descriptor.length

                mp!!.setDataSource(descriptor.fileDescriptor, start, end)
                descriptor.close()
                //			mp.setDataSource(songsList.get(songIndex).get("songPath"));
                mp!!.prepare()
                playPlayer()
//                mp!!.start()

                timerStart()

                val songName = context!!.getString(R.string.now_playing) + " " + songPlayList[songIndex].songTitle
                tvNowPlaying!!.text = songName
                tvNowPlaying!!.visibility = View.VISIBLE

                // Changing Button Image to pause image
                btnPlay!!.setImageResource(R.drawable.ic_pause_button)

                // set Progress bar values
                songProgressBar!!.progress = 0
                songProgressBar!!.max = 100

                // Updating progress bar
                updateProgressBar()
            } else {
                Toast.makeText(context, context!!.getString(R.string.please_enable_your_song), Toast.LENGTH_SHORT).show()
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun playSongByTitle(songTitle: String) {
        var songPlayList = enabledSongModelList
        if(isShuffle) {
            songPlayList = shuffledSongModelList
        }
        for (i in songPlayList!!.indices) {
            val songsModel = songPlayList[i]
            if (songTitle == songsModel.songTitle) {
                if (Helper.isLoggedInUser(context!!)) {
                    Helper.updateMileStonePendingMinutes(context!!, minutes!!)
                    val weakReferenceContext = WeakReference(context!!)
                    Helper.callUpdateMileStoneAsync(weakReferenceContext, minutes!!)
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
        LPHLog.d("currentSongIndex OnCompletion Start : "+ currentSongIndex)
        if (Helper.isLoggedInUser(context!!)) {
            val weakReferenceContext = WeakReference(context!!)
            Helper.callUpdateMileStoneAsync(weakReferenceContext, minutes!!)
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
                Toast.makeText(context, context!!.getString(R.string.shuffle_not_possible), Toast.LENGTH_SHORT).show()
            }

            /*if (enabledSongModelList!!.size > 0) {
                var last = currentSongIndex
                val rand = Random()
                currentSongIndex = rand.nextInt(enabledSongModelList!!.size - 1 - 0 + 1) + 0
                playSong(currentSongIndex)
            } else {
                Toast.makeText(context, context!!.getString(R.string.shuffle_not_possible), Toast.LENGTH_SHORT).show()
            }*/
        } else {
            LPHLog.d("ChantNow On Completion is called Else")
            // no repeat or shuffle ON - play next song
            if (currentSongIndex < enabledSongModelList!!.size - 1) {
                playSong(currentSongIndex + 1)
//                currentSongIndex += 1
            } else if(isRepeat) {
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

        LPHLog.d("currentSongIndex OnCompletion End : "+ currentSongIndex)

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
        if (Helper.isLoggedInUser(context!!)) {
            Helper.updateMileStonePendingMinutes(context!!, minutes!!)
            val weakReferenceContext = WeakReference(context!!)
            Helper.callUpdateMileStoneAsync(weakReferenceContext, minutes!!)
            minutes = 0f
            updatingMinutes = 0f
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            mAudioManager.abandonAudioFocusRequest(mFocusRequest)
        else
            mAudioManager.abandonAudioFocus(this)

        if (mp!= null && mp!!.isPlaying) {
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
        return SongsModel.getCursorLoader(context!!)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        if (loader.id == Constants.URL_SONG_LOADER) {
            if (cursor != null) {
                songsAdapter!!.swapCursor(cursor)
                songsAdapter!!.notifyDataSetChanged()
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        songsAdapter!!.swapCursor(null)
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
    }

    private fun pausePlayer() {
        if(mp != null && mp!!.isPlaying) {
            mp?.pause()
            btnPlay!!.setImageResource(R.drawable.ic_play_button)
        }
    }



    private fun playPlayer() {
        if(mp != null && successfullyRetrievedAudioFocus()) {
            mp?.start()
            btnPlay!!.setImageResource(R.drawable.ic_pause_button)
        }
    }


    private fun successfullyRetrievedAudioFocus(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val focusRequest: Int = mAudioManager.requestAudioFocus(mFocusRequest)
            return focusRequest == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            val focusRequest: Int = mAudioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN)
            LPHLog.d("ChantNow focusRequest : " + focusRequest)
            return focusRequest == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                LPHLog.d("ChantNow AUDIOFOCUS_GAIN")
                if(mp != null) {
                    LPHLog.d("ChantNow Mp is not null")
//                    val progress = volumeSeekBar?.progress
//                    LPHLog.d("ChantNow volume : " + progress)
//                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress!!, 0)
//                    mp!!.setVolume(progress.toFloat(), progress.toFloat())

                    if(isOnCall) {
                        isOnCall = false
                        playPlayer()
                    }
                } else {
                    LPHLog.d("ChantNow Mp is null")
                }
//                pausePlayer()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                LPHLog.d("ChantNow AUDIOFOCUS_LOSS")
                pausePlayer()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mAudioManager.abandonAudioFocusRequest(mFocusRequest)
                    mHandler.postDelayed(mDelayedStopRunnable,
                            TimeUnit.SECONDS.toMillis(30))
                } else
                    mAudioManager.abandonAudioFocus(this)
                /*if(mp != null && mp!!.isPlaying) {
                    mp!!.seekTo(0)
                    mp!!.stop()
                    btnPlay!!.setImageResource(R.drawable.ic_play_button)
                    val progress = Helper.getProgressPercentage(mp?.currentPosition!!.toLong(), mp?.duration!!.toLong())
                    //Log.d("Progress", ""+progress);
                    songProgressBar!!.progress = progress
//                    unRegisterThread()
                }*/
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                LPHLog.d("ChantNow AUDIOFOCUS_LOSS_TRANSIENT")
                pausePlayer()
            }
            else -> {
                LPHLog.d("ChantNow AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK")
               /* if(mp != null && mp!!.isPlaying){
                    mp!!.setVolume(0.1f, 0.1f)
                }*/
//                pausePlayer()
            }
        }
    }

    //Handle incoming phone calls
private fun callStateListener() {
  // Get the telephony manager
  telephonyManager = context!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
  //Starting listening for PhoneState changes

        phoneStateListener = object: PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String?) {
                when(state){
                    TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING -> {
                        if(mp != null) {
                            try {
                                if(mp!!.isPlaying) {
                                    isOnCall = true
                                    LPHLog.d("ChantNow incoming call pause")
                                    pausePlayer()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    TelephonyManager.CALL_STATE_IDLE -> {
                        if(isOnCall) {
                            LPHLog.d("ChantNow incoming call resume")
                            playPlayer()
                        }
                    }
                }
            }
        }

  // Register the listener with the telephony manager
  // Listen for changes to the device call state.
  telephonyManager.listen(phoneStateListener,
          PhoneStateListener.LISTEN_CALL_STATE);
}

    private fun timerStart() {
        if(countDownTimer != null) {
            countDownTimer?.cancel()
            minuteHandler.removeCallbacks(minuteHandlerTask)
        }
        if (Helper.isLoggedInUser(context!!)) {
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
                if (Helper.isLoggedInUser(context!!)) {
                    Helper.updateMileStonePendingMinutes(context!!, updatingMinutes!!)
                    Helper.updateLocalMileStoneMinutes(context!!, updatingMinutes!!)
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

}
