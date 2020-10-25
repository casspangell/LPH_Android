package org.lovepeaceharmony.androidapp.adapters

import android.content.Context
import android.database.Cursor
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.androidquery.AQuery
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.model.NewsVo
import org.lovepeaceharmony.androidapp.utility.ConfirmationAlertCallback
import org.lovepeaceharmony.androidapp.utility.Helper
import java.text.SimpleDateFormat
import java.util.*

/**
 * NewsAdapter
 * Created by Naveen Kumar M on 28/12/17.
 */

class NewsAdapter(private val context: Context, cursor: Cursor?, private val onNewsCallBack: OnNewsCallBack) : CursorRecyclerViewAdapter<NewsAdapter.ViewHolder>(null){

    private val aQuery: AQuery = AQuery(context)
    private val imageWidth: Int = (Helper.getDisplayWidth(context) * 0.3).toInt()
    private val typefaceBold = ResourcesCompat.getFont(context, R.font.open_sans_bold)
    private val typefaceRegular = ResourcesCompat.getFont(context, R.font.open_sans_regular)



    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var tvHeading: TextView
        var tvDescription: TextView
        var tvDate: TextView
        var ivFavorite: ImageView
        var ivNewsPic: ImageView
        var ivDefaultPic: ImageView

        init {
            tvHeading = view.findViewById(R.id.tv_heading)
            tvDate = view.findViewById(R.id.tv_date)
            tvDescription = view.findViewById(R.id.tv_description)
            ivFavorite = view.findViewById(R.id.iv_favorite)
            ivNewsPic = view.findViewById(R.id.iv_news_pic)
            ivDefaultPic = view.findViewById(R.id.iv_default_news)
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, cursor: Cursor?) {
        val newsVo = NewsVo.getValueFromCursor(cursor!!)
        viewHolder.tvHeading.text = newsVo.newsHeading
        viewHolder.tvDescription.text = newsVo.newsDescription
        viewHolder.ivDefaultPic.visibility = View.GONE

        try {
            val requiredFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            val date = Date()
            date.time = java.lang.Long.parseLong(newsVo.newsDate)
            viewHolder.tvDate.text = requiredFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (newsVo.newsPicUrl != null && !newsVo.newsPicUrl!!.isEmpty() && !newsVo.newsPicUrl.equals("null", ignoreCase = true)) {
            aQuery.id(viewHolder.ivNewsPic).image(newsVo.newsPicUrl, true, true, imageWidth, 0)
            viewHolder.ivNewsPic.visibility = View.VISIBLE
            viewHolder.ivDefaultPic.visibility = View.GONE
        } else {
            viewHolder.ivNewsPic.visibility = View.GONE
            viewHolder.ivDefaultPic.visibility = View.VISIBLE
//            viewHolder.ivNewsPic.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_news_icon))
        }

        if (newsVo.isRead) {
            viewHolder.tvHeading.typeface = typefaceRegular
            viewHolder.tvDescription.setTextColor(ContextCompat.getColor(context, R.color.bottom_icon_color))
        } else {
            viewHolder.tvHeading.typeface = typefaceBold
            viewHolder.tvDescription.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }

        if (newsVo.isFavorite)
            viewHolder.ivFavorite.setImageResource(R.drawable.ic_star_black_24dp)
        else
            viewHolder.ivFavorite.setImageResource(R.drawable.ic_star_border_black_24dp)

        viewHolder.ivFavorite.tag = newsVo
        viewHolder.ivFavorite.setOnClickListener { view ->
            if (Helper.isConnected(context)) {
                view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.bounce))
                val newsVo1 = view.tag as NewsVo
                onNewsCallBack.onFavoriteClick(newsVo1.newsId, !newsVo1.isFavorite)
            } else {

                Helper.showConfirmationAlertTwoButton(context, context.getString(R.string.internet_warning), object : ConfirmationAlertCallback {
                    override fun onPositiveButtonClick() {
                        view.performClick()
                    }

                    override fun onNegativeButtonClick() {

                    }

                    override fun onNeutralButtonClick() {

                    }
                })

            }

        }

        viewHolder.view.tag = newsVo
        viewHolder.view.setOnClickListener { view ->
            val newsVo1 = view.tag as NewsVo
            if(!newsVo1.isRead)
                onNewsCallBack.setRead(newsVo1.newsId, true)
            onNewsCallBack.onViewClick(newsVo1.newsId)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.news_row, parent, false)
        return ViewHolder(itemView)
    }

    interface OnNewsCallBack {
        fun onFavoriteClick(newsId: Int, isFavorite: Boolean)
        fun setRead(newsId: Int, isRead: Boolean)
        fun onViewClick(newsId: Int)
    }

}
