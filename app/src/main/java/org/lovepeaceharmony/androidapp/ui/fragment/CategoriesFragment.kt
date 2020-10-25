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
import org.lovepeaceharmony.androidapp.adapters.CategoryAdapter
import org.lovepeaceharmony.androidapp.adapters.NewsAdapter
import org.lovepeaceharmony.androidapp.model.CategoryVo
import org.lovepeaceharmony.androidapp.ui.activity.CategoryNewsListActivity
import org.lovepeaceharmony.androidapp.utility.Constants
import org.lovepeaceharmony.androidapp.utility.Helper
import org.lovepeaceharmony.androidapp.utility.LPHLog
import org.lovepeaceharmony.androidapp.utility.http.LPHException
import org.lovepeaceharmony.androidapp.utility.http.LPHServiceFactory
import org.lovepeaceharmony.androidapp.utility.http.Response
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [CategoriesFragment.newInstance] factory method to
 * create an instance of this fragment.
 * Created by Naveen Kumar M on 10/11/17.
 */
class CategoriesFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor>, NewsAdapter.OnNewsCallBack {

    private var rlProgressContainer: View? = null
    private var listContainer: View? = null
    private var pbViewMore: ProgressBar? = null
    private var swipeRefreshLayout: View? = null
    private var srlNoMessageRefreshLayout: View? = null
    private var categoryAdapter: CategoryAdapter? = null
    private var pageLimit: Int = 10
    private var pageOffset: Int = 0
    private var totalNewsCount: Int = 0
    private var isAsyncDone: Boolean = false

    private val mCategoriesBroadCast = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            pageOffset = 0
            startAsync(pageLimit, pageOffset)
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

//    private inner class PullToRefresh : SwipeRefreshLayout.OnRefreshListener {
//        override fun onRefresh() {
//            pbViewMore!!.visibility = View.GONE
//            srlNoMessageRefreshLayout!!.isRefreshing = false
//            swipeRefreshLayout!!.isRefreshing = false
//            pageOffset = 0
//            startAsync(pageLimit, pageOffset)
//        }
//    }

    private fun initView(view: View) {
        rlProgressContainer = view.findViewById(R.id.rl_progressContainer)
        listContainer = view.findViewById(R.id.ll_list_container)
        pbViewMore = view.findViewById(R.id.pb_view_more)
        categoryAdapter = CategoryAdapter(context!!, this)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = categoryAdapter

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
//
//        srlNoMessageRefreshLayout!!.setColorSchemeResources(android.R.color.holo_green_dark, android.R.color.holo_red_dark, android.R.color.holo_orange_dark, android.R.color.holo_purple)
//        srlNoMessageRefreshLayout!!.setOnRefreshListener(pullToRefresh)
        listContainer!!.visibility = View.GONE
        rlProgressContainer!!.visibility = View.VISIBLE

        loaderManager.initLoader(Constants.URL_RECENT_NEWS_LOADER, arguments, this)

        startAsync(10, 0)

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

    }

    fun restartLoader() {
        if(isAdded)
            loaderManager.restartLoader(Constants.URL_RECENT_NEWS_LOADER, null, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return CategoryVo.getCursorLoader(context!!)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        if (loader.id == Constants.URL_RECENT_NEWS_LOADER) {
            if(isAsyncDone) {
                listContainer!!.visibility = View.VISIBLE
                rlProgressContainer!!.visibility = View.GONE
            }
            if (cursor != null) {
                categoryAdapter!!.swapCursor(cursor)
                categoryAdapter!!.notifyDataSetChanged()
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
        categoryAdapter!!.swapCursor(null)

    }

    override fun onFavoriteClick(newsId: Int, isFavorite: Boolean) {
    }

    override fun setRead(newsId: Int, isRead: Boolean) {
    }

    override fun onViewClick(categoryId: Int) {
        val intent = Intent(context, CategoryNewsListActivity::class.java)
        intent.putExtra(Constants.BUNDLE_CATEGORY_ID, categoryId)
        startActivityForResult(intent, Constants.REQUEST_CODE_CATEGORY_LIST)
    }

    private fun startAsync(pageLimit: Int, pageOffset: Int) {
        if(Helper.isConnected(context!!)) {
            val categoriesAsync = CategoriesAsync(context = this, pageLimit = pageLimit, pageOffset = pageOffset)
            categoriesAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        } else {
            isAsyncDone = true
        }
    }

    private class CategoriesAsync internal constructor(context: CategoriesFragment, private val pageLimit: Int, private val pageOffset: Int) : AsyncTask<Void, Void, Response<ArrayList<CategoryVo>>>() {

        private val context: WeakReference<CategoriesFragment> = WeakReference(context)

        override fun doInBackground(vararg p0: Void?): Response<ArrayList<CategoryVo>> {
            var response = Response<ArrayList<CategoryVo>>()
            try {
                val lphService = LPHServiceFactory.getCALFService(context.get()?.context!!)
                response = lphService.categories(pageLimit, pageOffset)
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

        override fun onPostExecute(response: Response<ArrayList<CategoryVo>>) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        LPHLog.d("OnaCtivity result CAtegories Fragment")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CODE_CATEGORY_LIST && resultCode == Activity.RESULT_OK) {
            restartLoader()
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity!!.registerReceiver(mCategoriesBroadCast, IntentFilter(Constants.BROADCAST_CATEGORIES))
    }

    override fun onDetach() {
        super.onDetach()
        activity!!.unregisterReceiver(mCategoriesBroadCast)
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment CategoriesFragment.
         */
        fun newInstance(): CategoriesFragment {
            return CategoriesFragment()
        }
    }

}// Required empty public constructor
