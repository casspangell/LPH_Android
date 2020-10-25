package org.lovepeaceharmony.androidapp.ui.fragment


import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import org.json.JSONException
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.adapters.NewsAdapter
import org.lovepeaceharmony.androidapp.model.NewsVo
import org.lovepeaceharmony.androidapp.ui.activity.NewsDetailActivity
import org.lovepeaceharmony.androidapp.utility.ConfirmationAlertCallback
import org.lovepeaceharmony.androidapp.utility.Constants
import org.lovepeaceharmony.androidapp.utility.Helper
import org.lovepeaceharmony.androidapp.utility.OnRefreshCallback
import org.lovepeaceharmony.androidapp.utility.http.LPHException
import org.lovepeaceharmony.androidapp.utility.http.LPHServiceFactory
import org.lovepeaceharmony.androidapp.utility.http.Response
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [RecentNewsFragment.newInstance] factory method to
 * create an instance of this fragment.
 * Created by Naveen Kumar M on 10/11/17.
 */
class RecentNewsFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor>, NewsAdapter.OnNewsCallBack, OnRefreshCallback {
    private var rlProgressContainer: View? = null
    private var listContainer: View? = null
    private var pbViewMore: ProgressBar? = null
    private var swipeRefreshLayout: View? = null
    private var srlNoMessageRefreshLayout: View? = null
    private var newsAdapter: NewsAdapter? = null
    private var pageLimit: Int = 10
    private var pageOffset: Int = 0
    private var totalNewsCount: Int = 0
    private var isAsyncDone: Boolean = false

    private val mRecentNewsBroadCast = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            restartLoader()

        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_recent_news, container, false)
        initView(view)
        return view
    }

    override fun onResume() {
        super.onResume()
        if(!Helper.isConnected(context!!)) {
            Toast.makeText(context!!, context?.getString(R.string.no_connection), Toast.LENGTH_SHORT).show()
        }
    }

    private fun initView(view: View) {
        rlProgressContainer = view.findViewById(R.id.rl_progressContainer)
        listContainer = view.findViewById(R.id.ll_list_container)
        pbViewMore = view.findViewById(R.id.pb_view_more)
        newsAdapter = NewsAdapter(context!!, null, this)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = newsAdapter

        recyclerView.addItemDecoration(DividerItemDecoration(context!!, DividerItemDecoration.VERTICAL))
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()

        pbViewMore!!.visibility = View.GONE
        srlNoMessageRefreshLayout = view.findViewById(R.id.srl_activities_no_message)
        srlNoMessageRefreshLayout!!.visibility = View.GONE
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_feeds_page)

//        val pullToRefresh = PullToRefresh()
//        swipeRefreshLayout!!.setColorSchemeResources(android.R.color.holo_green_dark, android.R.color.holo_red_dark, android.R.color.holo_orange_dark, android.R.color.holo_purple)
//        swipeRefreshLayout!!.setOnRefreshListener(pullToRefresh)

//        srlNoMessageRefreshLayout!!.setColorSchemeResources(android.R.color.holo_green_dark, android.R.color.holo_red_dark, android.R.color.holo_orange_dark, android.R.color.holo_purple)
//        srlNoMessageRefreshLayout!!.setOnRefreshListener(pullToRefresh)
        listContainer!!.visibility = View.GONE
        rlProgressContainer!!.visibility = View.VISIBLE

        loaderManager.initLoader(Constants.URL_RECENT_NEWS_LOADER, arguments, this)

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

        /*recyclerView.addOnScrollListener(object: EndlessRecyclerOnScrollListener() {
            override fun onLoadMore() {
                LPHLog.d("PageOffset : " + pageOffset)
                LPHLog.d("totalNewsCount : " + totalNewsCount)
                if(pageOffset < totalNewsCount){
                    pbViewMore!!.visibility = View.VISIBLE
                    startAsync(pageLimit, pageOffset)
                }
            }
        })*/

    }


    /*private inner class PullToRefresh : SwipeRefreshLayout.OnRefreshListener {
        override fun onRefresh() {
            pbViewMore!!.visibility = View.GONE
            pageOffset = 0
            startAsync(pageLimit, pageOffset)
//            srlNoMessageRefreshLayout!!.isRefreshing = false
//            swipeRefreshLayout!!.isRefreshing = false
        }
    }*/

    private fun startAsync(pageLimit: Int, pageOffset: Int) {
        if(Helper.isConnected(context!!)) {
            val recentAsync = RecentNewsAsync(context = this, pageLimit = pageLimit, pageOffset = pageOffset)
            recentAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        } else {
            isAsyncDone = true
        }
    }

    fun restartLoader() {
        if(isAdded)
            loaderManager.restartLoader(Constants.URL_RECENT_NEWS_LOADER, null, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return NewsVo.getCursorLoader(context!!, NewsVo.NewsType.RECENT, 0)
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


    private class RecentNewsAsync internal constructor(context: RecentNewsFragment, private val pageLimit: Int, private val pageOffset: Int) : AsyncTask<Void, Void, Response<ArrayList<NewsVo>>>() {

        private val context: WeakReference<RecentNewsFragment> = WeakReference(context)

        override fun doInBackground(vararg p0: Void?): Response<ArrayList<NewsVo>> {
            var response = Response<ArrayList<NewsVo>>()
            try {
                val lphService = LPHServiceFactory.getCALFService(context.get()?.context!!)
                response = lphService.recentNews(pageLimit, pageOffset)
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

    override fun onFavoriteClick(newsId: Int, isFavorite: Boolean) {
        if(Helper.isConnected(context!!)) {
            val weakReferenceContext = WeakReference(this.context!!)
            val markFavoriteAsync = Helper.MarkFavoriteAsync(weakReferenceContext, newsId, isFavorite, this, 0)
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
            val markReadAsync = Helper.MarkReadAsync(weakReferenceContext, newsId, isRead, this, 0)
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
        startActivityForResult(newsDetailIntent, Constants.REQUEST_CODE_RECENT_NEWS)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity!!.registerReceiver(mRecentNewsBroadCast, IntentFilter(Constants.BROADCAST_RECENT_NEWS))
    }

    override fun onDetach() {
        super.onDetach()
        activity!!.unregisterReceiver(mRecentNewsBroadCast)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CODE_RECENT_NEWS && resultCode == Activity.RESULT_OK) {
            restartLoader()
            val intent1 = Intent(Constants.BROADCAST_FAVORITE_NEWS)
            context!!.sendBroadcast(intent1)

            val intent2 = Intent(Constants.BROADCAST_CATEGORIES)
            context!!.sendBroadcast(intent2)
        }
    }

    override fun onRefresh() {
        restartLoader()
        val intent1 = Intent(Constants.BROADCAST_FAVORITE_NEWS)
        context!!.sendBroadcast(intent1)

        val intent2 = Intent(Constants.BROADCAST_CATEGORIES)
        context!!.sendBroadcast(intent2)
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment RecentNewsFragment.
         */
        fun newInstance(): RecentNewsFragment {
            return RecentNewsFragment()
        }
    }
}// Required empty public constructor
