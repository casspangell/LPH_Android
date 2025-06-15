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
        fun bind(songsModel: SongsModel, position: Int) = with(binding) {
            // Map file name back to display name for correct title
            val displayName = mp3Manager.displayToFileNameMap.entries.find { it.value == songsModel.songTitle }?.key ?: songsModel.getDisplayName()
            tvTitle.text = displayName
            binding.root.tag = songsModel
            val songFileName = mp3Manager.getFileName(displayName)
            val isDownloaded = songFileName != null && (
                songFileName == "01_Mandarin_Soul_Language_English.mp3" ||
                File(context.filesDir, "songs/$songFileName").exists()
            )

            // UI state
            downloadProgress?.visibility = View.GONE
            toggleEnabled.isEnabled = true
            toggleEnabled.isChecked = songsModel.isChecked && isDownloaded

            // Detach listener before setting state
            toggleEnabled.setOnCheckedChangeListener(null)
            toggleEnabled.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Only check download status when toggling ON
                    val isDownloaded = songFileName != null && (
                        songFileName == "01_Mandarin_Soul_Language_English.mp3" ||
                        File(context.filesDir, "songs/$songFileName").exists()
                    )
                    Toast.makeText(
                        context,
                        if (isDownloaded) "$displayName is downloaded" else "$displayName is NOT downloaded",
                        Toast.LENGTH_SHORT
                    ).show()
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

        // Commented out: downloadSong function
        // private suspend fun downloadSong(displayName: String): Boolean = withContext(Dispatchers.IO) { ... }
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
