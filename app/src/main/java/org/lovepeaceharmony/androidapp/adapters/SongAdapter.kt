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
                SongViewHolder(binding, activity, onSongRefresh, context)
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
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(songsModel: SongsModel, position: Int) = with(binding) {
            tvTitle.text = songsModel.getDisplayName()
            binding.root.tag = songsModel
            
            // Check if song is downloaded
            val displayName = songsModel.getDisplayName()
            val songFileName = MP3DownloadManager(context).getFileName(displayName)
            val isDownloaded = if (songFileName == "01_Mandarin_Soul_Language_English.mp3") {
                true
            } else if (songFileName != null) {
                File(context.filesDir, "songs/$songFileName").exists()
            } else false

            // Set colors based on download status
            if (isDownloaded) {
                binding.root.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
                tvTitle.setTextColor(ContextCompat.getColor(context, android.R.color.black))
            } else {
                binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.light_grey))
                tvTitle.setTextColor(ContextCompat.getColor(context, R.color.grey_text))
            }
            
            with(toggleEnabled) {
                setOnCheckedChangeListener(null)
                // Only allow toggle if song is downloaded
                isEnabled = isDownloaded
                isChecked = isDownloaded && songsModel.isChecked
                
                val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val hasSeenIntro = sharedPrefs.getBoolean("has_seen_intro", false)
                if (position == 0 && songsModel.isToolTip && !hasSeenIntro) {
                    doOnPreDraw {
                        TapTarget.forView(
                            it,
                            context.getString(R.string.use_the_switches_to_customize_your_chant),
                            context.getString(R.string.tap_anywhere_to_continue)
                        ).init(activity, R.color.tool_tip_color2) {
                            SongsModel.updateIsToolTip(context, 0, false)
                            sharedPrefs.edit().putBoolean("has_seen_intro", true).apply()
                            onSongRefresh.onRefresh()
                            context.sendBroadcast(Intent(Constants.BROADCAST_MAIN_BOTTOM_LAYOUT))
                        }
                    }
                }
                setOnCheckedChangeListener { _, isChecked ->
                    if (!isEnabled) return@setOnCheckedChangeListener
                    
                    LPHLog.d("onCheckedChanged : $isChecked")
                    val currentSongModel = binding.root.tag as SongsModel
                    val updateSuccessful = SongsModel.updateIsEnabled(context, currentSongModel.songTitle, isChecked)
                    if (updateSuccessful) {
                        currentSongModel.isChecked = isChecked
                        onSongRefresh.onRefresh()
                        onSongRefresh.onDisableSong(currentSongModel.songTitle, isChecked, currentSongModel)
                    } else {
                        setOnCheckedChangeListener(null)
                        toggleEnabled.isChecked = true
                        setOnCheckedChangeListener { _, newIsChecked ->
                            val newUpdateSuccessful = SongsModel.updateIsEnabled(context, currentSongModel.songTitle, newIsChecked)
                            if (newUpdateSuccessful) {
                                currentSongModel.isChecked = newIsChecked
                                onSongRefresh.onRefresh()
                                onSongRefresh.onDisableSong(currentSongModel.songTitle, newIsChecked, currentSongModel)
                            } else {
                                setOnCheckedChangeListener(null)
                                toggleEnabled.isChecked = true
                                setOnCheckedChangeListener { _, _ -> }
                            }
                        }
                    }
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

        private fun checkAndDownloadIfNeeded() {
            val songsModel = binding.root.tag as SongsModel
            val displayName = songsModel.getDisplayName()
            val songFileName = MP3DownloadManager(context).getFileName(displayName)
            if (songFileName == null) return
            
            val localFile = File(context.filesDir, "songs/$songFileName")
            if (!localFile.exists()) {
                val downloadingToast = Toast.makeText(context, "Downloading $displayName", Toast.LENGTH_SHORT)
                downloadingToast.show()
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.getReferenceFromUrl("gs://love-peace-harmony.appspot.com/Songs/$songFileName")
                storageRef.getFile(localFile)
                    .addOnSuccessListener {
                        downloadingToast.cancel()
                        Toast.makeText(context, "Download complete", Toast.LENGTH_SHORT).show()
                        // Refresh the adapter to update colors and toggle state
                        onSongRefresh.onRefresh()
                    }
                    .addOnFailureListener { e ->
                        downloadingToast.cancel()
                        Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
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
}
