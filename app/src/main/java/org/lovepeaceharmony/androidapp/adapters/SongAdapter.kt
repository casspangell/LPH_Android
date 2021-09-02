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
            with(toggleEnabled) {
                if (cursor.position == 0 && songsModel.isToolTip) doOnPreDraw {
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
                isChecked = songsModel.isChecked
                tag = songsModel
                setOnCheckedChangeListener { compoundButton, isChecked ->
                    LPHLog.d("onCheckedChanged : $isChecked")
                    val songsModel1 = compoundButton.tag as SongsModel
                    SongsModel.updateIsEnabled(context, songsModel1.songTitle, isChecked)
                    onSongRefresh.onRefresh()
                    onSongRefresh.onDisableSong(songsModel1.songTitle, isChecked, songsModel1)
                }
            }

            with(root) {
                tag = songsModel
                setOnClickListener { view ->
                    val songsModel1 = view.tag as SongsModel
                    onSongRefresh.onItemClick(songsModel1.songTitle, songsModel.id)
                }
            }
        }
    }
}
