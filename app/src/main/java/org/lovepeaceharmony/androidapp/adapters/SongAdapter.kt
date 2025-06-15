package org.lovepeaceharmony.androidapp.adapters

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.getkeepsafe.taptargetview.TapTarget
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.databinding.SongsRowBinding
import org.lovepeaceharmony.androidapp.model.SongsModel
import org.lovepeaceharmony.androidapp.utility.Constants
import org.lovepeaceharmony.androidapp.utility.LPHLog
import org.lovepeaceharmony.androidapp.utility.init
import android.view.View
import android.widget.TextView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import org.lovepeaceharmony.androidapp.utility.MP3DownloadManager
import com.google.firebase.storage.FirebaseStorage
import android.widget.Toast
import java.io.File
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import android.util.Log

/**
 * SongsAdapter
 * Created by Naveen Kumar M on 28/12/17.
 */
class SongsAdapter(
    private val activity: FragmentActivity,
    private val context: Context,
    private val onSongRefresh: OnSongRefresh
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_SONG = 1
    }

    private var items: List<ListItem> = emptyList()
    private val downloadingSet = mutableSetOf<String>()
    private val adapterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val mp3Manager = MP3DownloadManager(context)  // Cache the manager instance

    sealed class ListItem {
        data class Header(val title: String, val backgroundColor: Int) : ListItem()
        data class Song(val songModel: SongsModel, val position: Int) : ListItem()
    }

    interface OnSongRefresh {
        fun onRefresh()
        fun onItemClick(songTitle: String, index: Int)
        fun onDisableSong(songTitle: String, isChecked: Boolean, songsModel: SongsModel)
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListItem.Header -> TYPE_HEADER
            is ListItem.Song -> TYPE_SONG
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_section_header, parent, false)
                HeaderViewHolder(view)
            }
            TYPE_SONG -> {
                val binding = SongsRowBinding.inflate(LayoutInflater.from(context), parent, false)
                SongViewHolder(binding, activity, onSongRefresh, context, downloadingSet, adapterScope, this, mp3Manager)  // Pass cached manager
            }
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is ListItem.Song -> (holder as SongViewHolder).bind(item.songModel, item.position)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is SongViewHolder) {
            holder.onViewRecycled()
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.headerTitle)
        private val headerContainer: LinearLayout = itemView.findViewById(R.id.headerContainer)
        fun bind(header: ListItem.Header) {
            titleTextView.text = header.title
            headerContainer.setBackgroundColor(header.backgroundColor)
        }
    }

    class SongViewHolder(
        private val binding: SongsRowBinding,
        private val activity: FragmentActivity,
        private val onSongRefresh: OnSongRefresh,
        private val context: Context,
        private val downloadingSet: MutableSet<String>,
        private val adapterScope: CoroutineScope,
        private val adapter: SongsAdapter,
        private val mp3Manager: MP3DownloadManager  // Pass the cached manager
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private var pulseAnimation: ValueAnimator? = null
        private var currentDisplayName: String? = null
        private val lightGreyColor = ContextCompat.getColor(context, R.color.light_grey)
        private val whiteColor = ContextCompat.getColor(context, R.color.white)
        private val goldenColor = ContextCompat.getColor(context, R.color.top_bar_orange)
        private val goldenColorWithOpacity = Color.argb((0.15 * 255).toInt(), goldenColor.red, goldenColor.green, goldenColor.blue)
        private val blackColor = ContextCompat.getColor(context, android.R.color.black)

        init {
            // Initialize pulse animation for text color
            pulseAnimation = ValueAnimator.ofArgb(blackColor, goldenColor, blackColor).apply {
                duration = 1500
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                addUpdateListener { animator ->
                    binding.tvTitle.setTextColor(animator.animatedValue as Int)
                }
            }
        }

        private fun startPulseAnimation() {
            pulseAnimation?.let {
                if (!it.isRunning) {
                    it.start()
                }
            }
        }

        private fun stopPulseAnimation() {
            pulseAnimation?.let {
                if (it.isRunning) {
                    it.cancel()
                }
                // Reset text color to black
                binding.tvTitle.setTextColor(blackColor)
            }
        }

        fun bind(songsModel: SongsModel, position: Int) {
            // Clean up previous animation if display name changed
            if (currentDisplayName != null && currentDisplayName != songsModel.songTitle) {
                stopPulseAnimation()
            }

            // Map file name back to display name for correct title
            val displayName = mp3Manager.displayToFileNameMap.entries.find { it.value == songsModel.songTitle }?.key ?: songsModel.getDisplayName()
            currentDisplayName = displayName
            binding.tvTitle.text = displayName
            binding.root.tag = songsModel
            val songFileName = mp3Manager.getFileName(displayName)
            val isDownloaded = songFileName != null && (
                songFileName == "01_Mandarin_Soul_Language_English.mp3" ||
                File(context.filesDir, "songs/$songFileName").exists()
            )

            // Set initial background color based on download status
            binding.root.setBackgroundColor(
                if (isDownloaded) whiteColor else lightGreyColor
            )

            // Set initial text color
            binding.tvTitle.setTextColor(blackColor)

            // UI state
            binding.toggleEnabled.isEnabled = true
            binding.toggleEnabled.isChecked = songsModel.isChecked && isDownloaded

            // Detach listener before setting state
            binding.toggleEnabled.setOnCheckedChangeListener(null)
            binding.toggleEnabled.setOnCheckedChangeListener { _, isChecked ->
                Log.d("SongAdapter", "Toggling song: ${songsModel.songTitle}, isChecked: $isChecked")
                // Always update the enabled state in the database/model
                SongsModel.updateIsEnabled(context, songsModel.songTitle, isChecked)
                // Update the model immediately so the adapter's data stays in sync
                songsModel.isChecked = isChecked
                if (isChecked) {
                    // Only check download status when toggling ON
                    val isDownloaded = songFileName != null && (
                        songFileName == "01_Mandarin_Soul_Language_English.mp3" ||
                        File(context.filesDir, "songs/$songFileName").exists()
                    )
                    
                    if (!isDownloaded) {
                        // Start download and animation
                        startPulseAnimation()
                        
                        // Download from Firebase Storage
                        val storage = FirebaseStorage.getInstance()
                        val storageRef = storage.getReferenceFromUrl("gs://love-peace-harmony.appspot.com/Songs/$songFileName")
                        val localFile = File(context.filesDir, "songs/$songFileName")
                        
                        storageRef.getFile(localFile)
                            .addOnSuccessListener {
                                stopPulseAnimation()
                                Toast.makeText(context, "Download complete", Toast.LENGTH_SHORT).show()
                                // Update UI to reflect downloaded state
                                binding.root.setBackgroundColor(whiteColor)
                                adapter.updateItem(position)
                            }
                            .addOnFailureListener { e ->
                                stopPulseAnimation()
                                Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
                                // Reset toggle state since download failed
                                binding.toggleEnabled.isChecked = false
                                onSongRefresh.onDisableSong(displayName, false, songsModel)
                            }
                    }
                }
                onSongRefresh.onDisableSong(displayName, isChecked, songsModel)
                // Post the UI update to the next frame
                binding.root.post {
                    adapter.updateItem(position)
                }
            }
            binding.root.setOnClickListener {
                val currentSongModel = it.tag as SongsModel
                if (!isDownloaded) {
                    Toast.makeText(
                        context,
                        "Please wait for the song to download",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (currentSongModel.isChecked) {
                    onSongRefresh.onItemClick(currentSongModel.songTitle, currentSongModel.id)
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.please_enable_your_song_and_play),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        fun onViewRecycled() {
            stopPulseAnimation()
            currentDisplayName = null
        }
    }

    fun updateData(songs: List<SongsModel>) {
        items = createListWithHeaders(songs)
        notifyDataSetChanged()
    }

    private fun createListWithHeaders(songs: List<SongsModel>): List<ListItem> {
        val result = mutableListOf<ListItem>()
        // Section 1
        result.add(ListItem.Header(
            "Love Peace Harmony in Many Languages",
            ContextCompat.getColor(context, R.color.header_blue)
        ))
        // Add first section songs (indices 0-6)
        songs.take(7).forEachIndexed { index, song ->
            result.add(ListItem.Song(song, index))
        }
        // Section 2
        result.add(ListItem.Header(
            "Expressions of Love Peace Harmony",
            ContextCompat.getColor(context, R.color.header_green)
        ))
        // Add second section songs (indices 7+)
        songs.drop(7).forEachIndexed { index, song ->
            result.add(ListItem.Song(song, index + 7))
        }
        return result
    }

    fun updateItem(position: Int) {
        notifyItemChanged(position)
    }
}
