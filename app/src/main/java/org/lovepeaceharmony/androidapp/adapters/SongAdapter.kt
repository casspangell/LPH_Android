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
            root.tag = songsModel
            with(toggleEnabled) {
                setOnCheckedChangeListener(null)
                isChecked = songsModel.isChecked
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
                    LPHLog.d("onCheckedChanged : $isChecked")
                    val currentSongModel = root.tag as SongsModel
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
            root.setOnClickListener {
                val currentSongModel = it.tag as SongsModel
                onSongRefresh.onItemClick(currentSongModel.songTitle, currentSongModel.id)
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
