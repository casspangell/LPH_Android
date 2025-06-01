package org.lovepeaceharmony.androidapp.ui.activity

import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
    private var currentQuality = "high"

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
    }

    private fun pauseVideo() {
        mediaPlayer?.pause()
        isPlaying = false
        playPauseButton.setImageResource(android.R.drawable.ic_media_play)
    }

    private fun updateTimeText(position: Int) {
        val duration = mediaPlayer?.duration ?: 0
        val current = position.coerceIn(0, duration)
        val currentMinutes = TimeUnit.MILLISECONDS.toMinutes(current.toLong())
        val currentSeconds = TimeUnit.MILLISECONDS.toSeconds(current.toLong()) % 60
        val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(duration.toLong())
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) % 60
        timeText.text = getString(R.string.time_format, 
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
        try {
            val videoPath = intent.getStringExtra("video_asset_path")
            if (videoPath != null) {
                val afd = assets.openFd(videoPath)
                val newMediaPlayer = MediaPlayer()
                newMediaPlayer.apply {
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    setDisplay(holder)
                    setOnPreparedListener {
                        this@LocalVideoPlayerActivity.videoWidth = videoWidth
                        this@LocalVideoPlayerActivity.videoHeight = videoHeight
                        updateVideoSize()
                        seekBar.max = duration
                        playVideo()
                    }
                    setOnCompletionListener {
                        this@LocalVideoPlayerActivity.isPlaying = false
                        playPauseButton.setImageResource(android.R.drawable.ic_media_play)
                        seekBar.progress = 0
                        updateTimeText(0)
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e("VideoPlayer", "Error: what=$what, extra=$extra")
                        true
                    }
                    prepareAsync()
                }
                mediaPlayer = newMediaPlayer
                afd.close()
            }
        } catch (e: Exception) {
            Log.e("VideoPlayer", "Error setting up video: ${e.message}")
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
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer?.let {
            it.seekTo(currentPosition)
            if (isPlaying) {
                it.start()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun showQualityDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.select_quality))
        val qualities = arrayOf(getString(R.string.quality_high), getString(R.string.quality_medium), getString(R.string.quality_low))
        builder.setItems(qualities) { dialog, which ->
            when (which) {
                0 -> {
                    currentQuality = "high"
                    playVideo()
                }
                1 -> {
                    currentQuality = "medium"
                    playVideo()
                }
                2 -> {
                    currentQuality = "low"
                    playVideo()
                }
            }
        }
        builder.show()
    }

    private fun showErrorDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.error))
        builder.setMessage(message)
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
} 