package org.lovepeaceharmony.androidapp.ui.activity

import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.lovepeaceharmony.androidapp.R
import java.util.concurrent.TimeUnit

class LocalVideoPlayerActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private lateinit var surfaceView: SurfaceView
    private lateinit var controlsLayout: LinearLayout
    private lateinit var playPauseButton: ImageButton
    private lateinit var exitButton: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var timeText: TextView
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var currentPosition = 0
    private var videoWidth = 0
    private var videoHeight = 0
    private lateinit var loadingSpinner: ProgressBar
    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    seekBar.progress = it.currentPosition
                    updateTimeText(it.currentPosition)
                    handler.postDelayed(this, 500)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_video_player)

        // Keep screen on while playing video
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        surfaceView = findViewById(R.id.surfaceView)
        controlsLayout = findViewById(R.id.controlsLayout)
        playPauseButton = findViewById(R.id.playPauseButton)
        exitButton = findViewById(R.id.exitButton)
        seekBar = findViewById(R.id.seekBar)
        timeText = findViewById(R.id.timeText)
        // Add a loading spinner overlay (add to layout if not present)
        loadingSpinner = ProgressBar(this)
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.gravity = android.view.Gravity.CENTER
        (findViewById<FrameLayout>(android.R.id.content)).addView(loadingSpinner, params)
        loadingSpinner.visibility = View.GONE

        surfaceView.holder.addCallback(this)
        setupControls()
    }

    private fun setupControls() {
        playPauseButton.setOnClickListener {
            if (isPlaying) {
                pauseVideo()
            } else {
                playVideo()
            }
        }

        exitButton.setOnClickListener {
            finish()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                    updateTimeText(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun playVideo() {
        mediaPlayer?.start()
        isPlaying = true
        playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
        handler.post(updateSeekBarRunnable)
    }

    private fun pauseVideo() {
        mediaPlayer?.pause()
        isPlaying = false
        playPauseButton.setImageResource(android.R.drawable.ic_media_play)
        handler.removeCallbacks(updateSeekBarRunnable)
    }

    private fun updateTimeText(position: Int) {
        val duration = mediaPlayer?.duration ?: 0
        val current = position.coerceIn(0, duration)
        val currentMinutes = TimeUnit.MILLISECONDS.toMinutes(current.toLong())
        val currentSeconds = TimeUnit.MILLISECONDS.toSeconds(current.toLong()) % 60
        val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(duration.toLong())
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) % 60
        timeText.text = String.format("%02d:%02d / %02d:%02d", 
            currentMinutes, currentSeconds, totalMinutes, totalSeconds)
    }

    private fun updateVideoSize() {
        if (videoWidth == 0 || videoHeight == 0) return

        val display = windowManager.defaultDisplay
        val realSize = android.graphics.Point()
        display.getRealSize(realSize)
        
        // Get the actual screen dimensions regardless of orientation
        val screenWidth = realSize.x
        val screenHeight = realSize.y
        val videoRatio = videoWidth.toFloat() / videoHeight.toFloat()
        
        // Get the current rotation
        val rotation = display.rotation
        val isLandscape = rotation == android.view.Surface.ROTATION_90 || 
                         rotation == android.view.Surface.ROTATION_270

        val layoutParams = surfaceView.layoutParams
        if (isLandscape) {
            // In landscape: fill height and maintain aspect ratio
            layoutParams.height = screenHeight
            layoutParams.width = (screenHeight * videoRatio).toInt()
        } else {
            // In portrait: fill width and maintain aspect ratio
            layoutParams.width = screenWidth
            layoutParams.height = (screenWidth / videoRatio).toInt()
        }
        surfaceView.layoutParams = layoutParams
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        val videoUrl = intent.getStringExtra("video_url")
        if (videoUrl.isNullOrEmpty()) {
            Toast.makeText(this, R.string.something_went_wrong_please_try_again, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        if (!isNetworkAvailable()) {
            Toast.makeText(this, R.string.please_check_your_internet_connection, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        loadingSpinner.visibility = View.VISIBLE
        try {
            val newMediaPlayer = MediaPlayer()
            newMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
            newMediaPlayer.setDataSource(this, Uri.parse(videoUrl))
            newMediaPlayer.setDisplay(holder)
            newMediaPlayer.setOnPreparedListener {
                videoWidth = it.videoWidth
                videoHeight = it.videoHeight
                updateVideoSize()
                seekBar.max = it.duration
                playVideo()
                loadingSpinner.visibility = View.GONE
            }
            newMediaPlayer.setOnBufferingUpdateListener { _, percent ->
                // Optionally update spinner or buffer UI
            }
            newMediaPlayer.setOnCompletionListener {
                isPlaying = false
                playPauseButton.setImageResource(android.R.drawable.ic_media_play)
                seekBar.progress = 0
                updateTimeText(0)
            }
            newMediaPlayer.setOnErrorListener { _, what, extra ->
                loadingSpinner.visibility = View.GONE
                Toast.makeText(this, R.string.something_went_wrong_please_try_again, Toast.LENGTH_LONG).show()
                finish()
                true
            }
            newMediaPlayer.prepareAsync()
            mediaPlayer = newMediaPlayer
        } catch (e: Exception) {
            loadingSpinner.visibility = View.GONE
            Toast.makeText(this, R.string.something_went_wrong_please_try_again, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        updateVideoSize()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        // Add a small delay to ensure smooth transition
        surfaceView.post {
            updateVideoSize()
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.let {
            if (it.isPlaying) {
                currentPosition = it.currentPosition
                it.pause()
            }
        }
        handler.removeCallbacks(updateSeekBarRunnable)
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer?.let {
            it.seekTo(currentPosition)
            if (isPlaying) {
                it.start()
                handler.post(updateSeekBarRunnable)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacks(updateSeekBarRunnable)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
} 