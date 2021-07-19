package org.lovepeaceharmony.androidapp.ui.activity

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.AsyncTask
import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import org.json.JSONException
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.adapters.NewsAdapter
import org.lovepeaceharmony.androidapp.model.CategoryVo
import org.lovepeaceharmony.androidapp.model.NewsVo
import org.lovepeaceharmony.androidapp.utility.*
import org.lovepeaceharmony.androidapp.utility.http.LPHException
import org.lovepeaceharmony.androidapp.utility.http.LPHServiceFactory
import org.lovepeaceharmony.androidapp.utility.http.Response
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

/**
 * CategoryNewsListActivity
 * Created by Naveen Kumar M on 18/12/17.
 */

class CategoryNewsListActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor>, NewsAdapter.OnNewsCallBack, OnRefreshCallback {
    private var context: Context? = null
    private var categoryVo: CategoryVo? = null
    private var rlProgressContainer: View? = null
    private var listContainer: View? = null
    private var pbViewMore: ProgressBar? = null
    private var swipeRefreshLayout: View? = null
    private var srlNoMessageRefreshLayout: View? = null
    private var newsAdapter: NewsAdapter? = null
    private var categoryId: Int = 0
    private var pageLimit: Int = 10
    private var pageOffset: Int = 0
    private var totalNewsCount: Int = 0
    private var isAsyncDone: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        setContentView(R.layout.activity_category_news_list)
        handleIntent()
        initView()
    }

    override fun onResume() {
        super.onResume()
        if(!Helper.isConnected(context!!)) {
            Toast.makeText(context!!, context?.getString(R.string.no_connection), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleIntent() {
        if (intent.extras != null) {
            categoryId = intent.extras!!.getInt(Constants.BUNDLE_CATEGORY_ID, 0)
            LPHLog.d("categoryId: " + categoryId)
            categoryVo = CategoryVo.getCategoryVo(context!!, categoryId)
        }
    }

    private fun initView() {
        if (categoryVo != null) {
            val tvBack = findViewById<TextView>(R.id.tv_back)
            val tvHeading = findViewById<TextView>(R.id.tv_heading)
            updateCounts()
            tvHeading.text = categoryVo!!.categoryName

            tvBack.setOnClickListener { onBackPressed() }

            rlProgressContainer = findViewById(R.id.rl_progressContainer)
            listContainer = findViewById(R.id.ll_list_container)
            pbViewMore = findViewById(R.id.pb_view_more)
            newsAdapter = NewsAdapter(context!!, null, this)
            val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = newsAdapter

            recyclerView.addItemDecoration(DividerItemDecoration(context!!, DividerItemDecoration.VERTICAL))
            recyclerView.setHasFixedSize(true)
            recyclerView.itemAnimator = DefaultItemAnimator()

            pbViewMore!!.visibility = View.GONE
            srlNoMessageRefreshLayout = findViewById(R.id.srl_activities_no_message)
            srlNoMessageRefreshLayout!!.visibility = View.GONE
            swipeRefreshLayout = findViewById(R.id.swipe_refresh_feeds_page)

//            val pullToRefresh = PullToRefresh()
//            swipeRefreshLayout!!.setColorSchemeResources(android.R.color.holo_green_dark, android.R.color.holo_red_dark, android.R.color.holo_orange_dark, android.R.color.holo_purple)
//            swipeRefreshLayout!!.setOnRefreshListener(pullToRefresh)
//
//            srlNoMessageRefreshLayout!!.setColorSchemeResources(android.R.color.holo_green_dark, android.R.color.holo_red_dark, android.R.color.holo_orange_dark, android.R.color.holo_purple)
//            srlNoMessageRefreshLayout!!.setOnRefreshListener(pullToRefresh)
            listContainer!!.visibility = View.GONE
            rlProgressContainer!!.visibility = View.VISIBLE

            supportLoaderManager.initLoader(Constants.URL_RECENT_NEWS_LOADER, null, this)

            startAsync(pageLimit, pageOffset)

            fun RecyclerView.addOnScrolledToEnd(onScrolledToEnd: () -> Unit){

                this.addOnScrollListener(object: RecyclerView.OnScrollListener(){

                    private val visibleThreshold = 5

                    private var loading = true
                    private var previousTotal = 0

                    override fun onScrollStateChanged(recyclerView: RecyclerView,
                                                      newState: Int) {

                        with(layoutManager as LinearLayoutManager){

                            val visibleItemCount = childCount
                            val totalItemCount = itemCount
                            val firstVisibleItem = findFirstVisibleItemPosition()

                            if (loading && totalItemCount > previousTotal){

                                loading = false
                                previousTotal = totalItemCount
                            }

                            if(!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)){

                                onScrolledToEnd()
                                loading = true
                            }
                        }
                    }
                })
            }

            recyclerView.addOnScrolledToEnd {
                //What you want to do once the end is reached
                pbViewMore!!.visibility = View.VISIBLE
                startAsync(pageLimit, pageOffset)
            }
        } else
            LPHLog.d("Category is null")
    }

    /*private inner class PullToRefresh : SwipeRefreshLayout.OnRefreshListener {
        override fun onRefresh() {
            pbViewMore!!.visibility = View.GONE
            srlNoMessageRefreshLayout!!.isRefreshing = false
            swipeRefreshLayout!!.isRefreshing = false
            pageOffset = 0
            startAsync(pageLimit, pageOffset)
        }
    }*/

    fun restartLoader() {
        supportLoaderManager.restartLoader(Constants.URL_RECENT_NEWS_LOADER, null, this)
    }

    private fun updateCounts(){
        val tvNoPosts = findViewById<TextView>(R.id.tv_no_posts)
        val tvUnReadCount = findViewById<TextView>(R.id.tv_unread_count)
        val tvFavoriteCount = findViewById<TextView>(R.id.tv_favorite_count)

        var postText = context?.getString(R.string.post)
        if(categoryVo?.noOfPosts!! > 1)
            postText = context?.getString(R.string.posts)
        val noOfPosts = categoryVo!!.noOfPosts.toString() + " " + postText
        val unReadCount = categoryVo!!.unreadCount.toString() + " " + context!!.getString(R.string.unread)
        val favoriteCount = categoryVo!!.favoriteCount.toString() + " " + context!!.getString(R.string.favorite_category)
        tvNoPosts.text = noOfPosts
        tvUnReadCount.text = unReadCount
        tvFavoriteCount.text = favoriteCount
    }

    private fun refresh() {
        categoryVo = CategoryVo.getCategoryVo(context!!, categoryId)
        updateCounts()

        restartLoader()
        val intent1 = Intent(Constants.BROADCAST_FAVORITE_NEWS)
        context!!.sendBroadcast(intent1)

        val intent2 = Intent(Constants.BROADCAST_RECENT_NEWS)
        context!!.sendBroadcast(intent2)

        val intent = Intent()
        intent.putExtra(Constants.BUNDLE_IS_REFRESH, true)
        setResult(RESULT_OK, intent)


    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return NewsVo.getCursorLoader(context!!, NewsVo.NewsType.CATEGORY, categoryVo!!.categoryId)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        if (loader.id == Constants.URL_RECENT_NEWS_LOADER) {
            if(isAsyncDone) {
                listContainer!!.visibility = View.VISIBLE
                rlProgressContainer!!.visibility = View.GONE
            }
            if (cursor != null) {
                newsAdapter!!.swapCursor(cursor)
                newsAdapter!!.notifyDataSetChanged()
                if (isAsyncDone && cursor.count > 0) {
                    srlNoMessageRefreshLayout!!.visibility = View.GONE
                    swipeRefreshLayout!!.visibility = View.VISIBLE
                } else if(isAsyncDone){
                    srlNoMessageRefreshLayout!!.visibility = View.VISIBLE
                    swipeRefreshLayout!!.visibility = View.GONE
                }
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        newsAdapter!!.swapCursor(null)
    }

    override fun onFavoriteClick(newsId: Int, isFavorite: Boolean) {
        if(Helper.isConnected(context!!)) {
            val weakReferenceContext = WeakReference(this.context!!)
            val markFavoriteAsync = Helper.MarkFavoriteAsync(weakReferenceContext, newsId, isFavorite, this, categoryId)
            markFavoriteAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        } else {
            Helper.showConfirmationAlertTwoButton(context!!, context!!.getString(R.string.internet_warning), object : ConfirmationAlertCallback {
                override fun onPositiveButtonClick() {
                    onFavoriteClick(newsId, isFavorite)
                }

                override fun onNegativeButtonClick() {

                }

                override fun onNeutralButtonClick() {

                }
            })
        }
    }

    override fun setRead(newsId: Int, isRead: Boolean) {
        if(Helper.isConnected(context!!)) {
            val weakReferenceContext = WeakReference(context!!)
            val markReadAsync = Helper.MarkReadAsync(weakReferenceContext, newsId, isRead, this, categoryId)
            markReadAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        } else {
            Helper.showConfirmationAlertTwoButton(context!!, context!!.getString(R.string.internet_warning), object : ConfirmationAlertCallback {
                override fun onPositiveButtonClick() {
                    setRead(newsId, isRead)
                }

                override fun onNegativeButtonClick() {

                }

                override fun onNeutralButtonClick() {

                }
            })
        }
    }

    override fun onViewClick(newsId: Int) {
        val newsDetailIntent = Intent(context, NewsDetailActivity::class.java)
        newsDetailIntent.putExtra(Constants.BUNDLE_NEWS_ID, newsId)
        newsDetailIntent.putExtra(Constants.BUNDLE_CATEGORY_ID, categoryId)
        startActivityForResult(newsDetailIntent, Constants.REQUEST_CODE_CATEGORY_NEWS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CODE_CATEGORY_NEWS && resultCode == RESULT_OK) {
            LPHLog.d("Coming to onActivityResult CAtegoryNewsList")
            refresh()

        }
    }

    override fun onRefresh() {
        refresh()
    }

    private fun startAsync(pageLimit: Int, pageOffset: Int) {
        if(Helper.isConnected(context!!)) {
            val categoryNewsAsync = CategoryNewsAsync(context = this, pageLimit = pageLimit, pageOffset = pageOffset)
            categoryNewsAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        } else {
            isAsyncDone = true
        }
    }

    private class CategoryNewsAsync internal constructor(context: CategoryNewsListActivity, private val pageLimit: Int, private val pageOffset: Int) : AsyncTask<Void, Void, Response<ArrayList<NewsVo>>>() {

        private val context: WeakReference<CategoryNewsListActivity> = WeakReference(context)

        override fun doInBackground(vararg p0: Void?): Response<ArrayList<NewsVo>> {
            var response = Response<ArrayList<NewsVo>>()
            try {
                val lphService = LPHServiceFactory.getCALFService(context.get()?.context!!)
                response = lphService.categoryNews(pageLimit, pageOffset, context.get()!!.categoryId)
            } catch (e: LPHException) {
                e.printStackTrace()
                response.setThrowable(e)
            } catch (e: JSONException) {
                e.printStackTrace()
                response.setThrowable(e)
            } catch (e: IOException) {
                e.printStackTrace()
                response.setThrowable(e)
            }

            return response
        }

        override fun onPostExecute(response: Response<ArrayList<NewsVo>>) {
            super.onPostExecute(response)
            context.get()?.isAsyncDone = true
            context.get()?.pbViewMore?.visibility = View.GONE
            if (response.isSuccess()) {
                context.get()?.totalNewsCount = response.getMetaData() as Int
                if(context.get() != null)
                    context.get()!!.pageOffset += response.getResult()?.size!!
                context.get()?.restartLoader()
            }
        }
    }
}
