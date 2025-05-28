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

/**
 * SongsAdapter
 * Created by Naveen Kumar M on 28/12/17.
 */
class SongsAdapter(
    private val activity: FragmentActivity,
    private val context: Context,
    private val onSongRefresh: OnSongRefresh
) : CursorRecyclerViewAdapter<SongsAdapter.SongViewHolder>(null) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SongViewHolder(
        SongsRowBinding.inflate(LayoutInflater.from(context), parent, false),
        activity, onSongRefresh
    )

    override fun onBindViewHolder(viewHolder: SongViewHolder, cursor: Cursor?) {
        cursor?.let { viewHolder.bind(it) }
    }

    interface OnSongRefresh {
        fun onRefresh()
        fun onItemClick(songTitle: String, index: Int)
        fun onDisableSong(songTitle: String, isChecked: Boolean, songsModel: SongsModel)
    }

    class SongViewHolder(
        private val binding: SongsRowBinding,
        private val activity: FragmentActivity,
        private val onSongRefresh: OnSongRefresh,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cursor: Cursor) = with(binding) {
            val songsModel = SongsModel.getValueFromCursor(cursor)
            tvTitle.text = songsModel.songTitle
            
            // Store the current song model in the view's tag
            root.tag = songsModel
            
            with(toggleEnabled) {
                // Remove any existing listener to prevent duplicate callbacks
                setOnCheckedChangeListener(null)
                
                // Set the initial state
                isChecked = songsModel.isChecked
                
                // Show tooltip for first item if needed
                if (cursor.position == 0 && songsModel.isToolTip) {
                    doOnPreDraw {
                        TapTarget.forView(
                            it,
                            it.context.getString(R.string.use_the_switches_to_customize_your_chant),
                            it.context.getString(R.string.tap_anywhere_to_continue)
                        ).init(activity, R.color.tool_tip_color2) {
                            SongsModel.updateIsToolTip(it.context, 0, false)
                            onSongRefresh.onRefresh()
                            it.context.sendBroadcast(Intent(Constants.BROADCAST_MAIN_BOTTOM_LAYOUT))
                        }
                    }
                }
                
                // Set up the toggle listener
                setOnCheckedChangeListener { _, isChecked ->
                    LPHLog.d("onCheckedChanged : $isChecked")
                    // Get the current song model from the view's tag
                    val currentSongModel = root.tag as SongsModel
                    
                    // Update the database
                    SongsModel.updateIsEnabled(context, currentSongModel.songTitle, isChecked)
                    
                    // Update the model's state
                    currentSongModel.isChecked = isChecked
                    
                    // Notify listeners
                    onSongRefresh.onRefresh()
                    onSongRefresh.onDisableSong(currentSongModel.songTitle, isChecked, currentSongModel)
                }
            }

            // Set up click listener for the entire row
            root.setOnClickListener {
                val currentSongModel = it.tag as SongsModel
                onSongRefresh.onItemClick(currentSongModel.songTitle, currentSongModel.id)
            }
        }
    }
}
