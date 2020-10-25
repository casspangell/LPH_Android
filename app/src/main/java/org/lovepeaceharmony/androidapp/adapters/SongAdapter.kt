package org.lovepeaceharmony.androidapp.adapters

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.support.v4.app.FragmentActivity
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.model.SongsModel
import org.lovepeaceharmony.androidapp.utility.Constants
import org.lovepeaceharmony.androidapp.utility.Helper
import org.lovepeaceharmony.androidapp.utility.LPHLog

/**
 * SongsAdapter
 * Created by Naveen Kumar M on 28/12/17.
 */
class SongsAdapter(private val activity: FragmentActivity, private val context: Context, cursor: Cursor?, private val onSongRefresh: OnSongRefresh): CursorRecyclerViewAdapter<SongsAdapter.ViewHolder>(null){

    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var tvTitle: TextView
        var toggleButton: ToggleButton

        init {
            tvTitle = view.findViewById(R.id.tv_title)
            toggleButton = view.findViewById(R.id.toggle_enabled)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.songs_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, cursor: Cursor?) {
        val songsModel = SongsModel.getValueFromCursor(cursor!!)
        viewHolder.tvTitle.text = songsModel.songTitle
        viewHolder.toggleButton.isChecked = songsModel.isChecked

        if(cursor.position == 0 && songsModel.isToolTip){
            val isToolTipShown = Helper.getBooleanFromPreference(context, Constants.SHARED_PREF_IS_TOOL_TIP_SHOWN)
            LPHLog.d("isToolTipShown : " +isToolTipShown)
            if(!isToolTipShown){
                /*val sequence = TapTargetSequence(activity)
                        .targets(
                                // This tap target will target the back button, we just need to pass its containing toolbar
                                TapTarget.forView(viewHolder.toggleButton, context.getString(R.string.use_the_switches_tool_tip)).tintTarget(true).cancelable(false)
                                        .dimColor(android.R.color.white)
                                        .outerCircleColor(R.color.tool_tip_color2)
                                        .targetCircleColor(android.R.color.black)
                                        .transparentTarget(true)
                                        .textColor(android.R.color.white)
                                        .id(2)

                        )
                        .listener(object : TapTargetSequence.Listener {
                            // This listener will tell us when interesting(tm) events happen in regards
                            // to the sequence
                            override fun onSequenceFinish() {
                                LPHLog.d("Tool Tip : Sequence Finished SongAdapter")
                                SongsModel.updateIsToolTip(context, 0, false)
                                onSongRefresh.onRefresh()
                                val intent1 = Intent(Constants.BROADCAST_MAIN_BOTTOM_LAYOUT)
                                context.sendBroadcast(intent1)
                            }

                            override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {
                                Log.d("TapTargetView", "Clicked on " + lastTarget.id())
                            }

                            override fun onSequenceCanceled(lastTarget: TapTarget) {
                            }
                        })

                sequence.start()*/


                TapTargetView.showFor(activity, TapTarget.forView(viewHolder.toggleButton, context.getString(R.string.use_the_switches_tool_tip), context.getString(R.string.tap_here_to_continue))
                        .cancelable(false)
//                    .drawShadow(true)
                        .dimColor(android.R.color.white)
                        .outerCircleColor(R.color.tool_tip_color2)
                        .targetCircleColor(android.R.color.black)
                        .transparentTarget(true)
                        .textColor(android.R.color.white)
                        .titleTextSize(18)
                        .descriptionTextSize(14)
                        .textTypeface(ResourcesCompat.getFont(activity, R.font.open_sans_semi_bold))
                        .descriptionTypeface(ResourcesCompat.getFont(activity, R.font.open_sans_regular))
                        .id(1)
                        .tintTarget(false), object : TapTargetView.Listener() {
                    override fun onTargetClick(view: TapTargetView) {
                        super.onTargetClick(view)
                        // .. which evidently starts the sequence we defined earlier
                        LPHLog.d("Tool Tip : Sequence Finished SongAdapter")
                        SongsModel.updateIsToolTip(context, 0, false)
                        onSongRefresh.onRefresh()
                        val intent1 = Intent(Constants.BROADCAST_MAIN_BOTTOM_LAYOUT)
                        context.sendBroadcast(intent1)
                    }

                    override fun onOuterCircleClick(view: TapTargetView?) {
                        super.onOuterCircleClick(view)
                        view?.dismiss(true)
                        LPHLog.d("Tool Tip : Sequence Finished SongAdapter")
                        SongsModel.updateIsToolTip(context, 0, false)
                        onSongRefresh.onRefresh()
                        val intent1 = Intent(Constants.BROADCAST_MAIN_BOTTOM_LAYOUT)
                        context.sendBroadcast(intent1)
                    }

                    override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                        Log.d("TapTargetViewSample", "You dismissed me :(")
                    }
                })
            }
        }

        viewHolder.view.tag = songsModel
        viewHolder.toggleButton.tag = songsModel
        viewHolder.toggleButton.setOnCheckedChangeListener { compoundButton, isChecked ->
            LPHLog.d("onCheckedChanged : " + isChecked)
            val songsModel1 = compoundButton.tag as SongsModel
            SongsModel.updateIsEnabled(context, songsModel1.songTitle, isChecked)
            onSongRefresh.onRefresh()
            onSongRefresh.onDisableSong(songsModel1.songTitle, isChecked, songsModel1)
        }

        viewHolder.view.setOnClickListener { view ->
            val songsModel1 = view.tag as SongsModel
            onSongRefresh.onItemClick(songsModel1.songTitle, songsModel.id)
//            if (songsModel1.isChecked)

        }
    }

    interface OnSongRefresh {
        fun onRefresh()
        fun onItemClick(songTitle: String, index: Int)
        fun onDisableSong(songTitle: String, isChecked: Boolean, songsModel: SongsModel)
    }



}