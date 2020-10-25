package org.lovepeaceharmony.androidapp.adapters

import android.content.Context
import android.database.Cursor
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.androidquery.AQuery
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.model.CategoryVo
import org.lovepeaceharmony.androidapp.utility.Helper

/**
 * CategoryAdapter
 * Created by Naveen Kumar M on 28/12/17.
 */
class CategoryAdapter(private val context: Context,  private val onNewsCallBack: NewsAdapter.OnNewsCallBack) : CursorRecyclerViewAdapter<CategoryAdapter.ViewHolder>(null) {

    private val aQuery: AQuery = AQuery(context)
    private val imageWidth: Int = (Helper.getDisplayWidth(context) * 0.8).toInt()

    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var tvHeading: TextView
        var tvNoPosts: TextView
        var tvUnReadCount: TextView
        var tvFavoriteCount: TextView
        var ivNewsPic: ImageView
        var ivDefault: ImageView

        init {
            tvHeading = view.findViewById(R.id.tv_heading)
            tvNoPosts = view.findViewById(R.id.tv_no_posts)
            tvUnReadCount = view.findViewById(R.id.tv_unread_count)
            tvFavoriteCount = view.findViewById(R.id.tv_favorite_count)
            ivNewsPic = view.findViewById(R.id.iv_news_pic)
            ivDefault = view.findViewById(R.id.iv_default_category)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.category_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, cursor: Cursor?) {
        val categoryVo = CategoryVo.getValueFromCursor(cursor!!)
        if (categoryVo.newsPicUrl != null && !categoryVo.newsPicUrl!!.isEmpty() && !categoryVo.newsPicUrl.equals("null", ignoreCase = true)) {
            aQuery.id(viewHolder.ivNewsPic).image(categoryVo.newsPicUrl, true, true, imageWidth, 0)
            viewHolder.ivNewsPic.visibility = View.VISIBLE
            viewHolder.ivDefault.visibility = View.GONE
        } else {
            viewHolder.ivNewsPic.visibility = View.GONE
            viewHolder.ivDefault.visibility = View.VISIBLE
        }
        viewHolder.tvHeading.text = categoryVo.categoryName
        var postText = context.getString(R.string.post)
        if(categoryVo.noOfPosts > 1)
            postText = context.getString(R.string.posts)
        val noOfPosts = categoryVo.noOfPosts.toString() + " " + postText
        val unReadCount = categoryVo.unreadCount.toString() + " " + context.getString(R.string.unread)
        val favoriteCount = categoryVo.favoriteCount.toString() + " " + context.getString(R.string.favorite_category)
        viewHolder.tvNoPosts.text = noOfPosts
        viewHolder.tvUnReadCount.text = unReadCount
        viewHolder.tvFavoriteCount.text = favoriteCount

        viewHolder.view.tag = categoryVo
        viewHolder.view.setOnClickListener { view ->
            val categoryVo1 = view.tag as CategoryVo
            onNewsCallBack.onViewClick(categoryVo1.categoryId)
        }
    }
}