package org.lovepeaceharmony.androidapp.ui.fragment

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
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
import android.widget.TextView
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import org.json.JSONException
import org.json.JSONObject
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.adapters.NewsAdapter
import org.lovepeaceharmony.androidapp.model.NewsVo
import org.lovepeaceharmony.androidapp.ui.activity.LoginDetailActivity
import org.lovepeaceharmony.androidapp.ui.activity.NewsDetailActivity
import org.lovepeaceharmony.androidapp.utility.*
import org.lovepeaceharmony.androidapp.utility.http.LPHException
import org.lovepeaceharmony.androidapp.utility.http.LPHServiceFactory
import org.lovepeaceharmony.androidapp.utility.http.Response
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * Use the [FavoriteNewsFragment.newInstance] factory method to
 * create an instance of this fragment.
 * Created by Naveen Kumar M on 10/11/17.
 */
class FavoriteNewsFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor>, NewsAdapter.OnNewsCallBack, OnRefreshCallback {
    private var rlProgressContainer: View? = null
    private var pbViewMore: ProgressBar? = null
    private var swipeRefreshLayout: View? = null
    private var newsAdapter: NewsAdapter? = null
    private var emptyContainer: View? = null
    private var onTabChange: OnTabChange? = null
    private var googleSingIn: GoogleSingIn? = null
    private var callbackManager: CallbackManager? = null
    private var isFromFb: Boolean = false
    private var pageLimit: Int = 10
    private var pageOffset: Int = 0
    private var totalNewsCount: Int = 0
    private var isAsyncDone: Boolean = false

    private val mFavoriteNewsBroadCast = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            restartLoader()
        }
    }

    fun setOnTabChange(onTabChange: OnTabChange) {
        this.onTabChange = onTabChange
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favorite_news, container, false)
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
        val loginContainer = view.findViewById<View>(R.id.login_container)
        val favoritesContainer = view.findViewById<View>(R.id.favourites_container)
        emptyContainer = view.findViewById(R.id.empty_favorite_container)
//        emptyContainer!!.setColorSchemeResources(android.R.color.holo_green_dark, android.R.color.holo_red_dark, android.R.color.holo_orange_dark, android.R.color.holo_purple)

        val loginType = Helper.getStringFromPreference(context!!, Constants.SHARED_PREF_LOGIN_TYPE)

        if (loginType.equals(Constants.LoginType.WithoutEmail.name, ignoreCase = true)) {
            loginContainer.visibility = View.VISIBLE
            favoritesContainer.visibility = View.GONE
            val tvSignIn = view.findViewById<TextView>(R.id.tv_sign_in)
            val googleSignInBtn = view.findViewById<View>(R.id.google_sign_in)
            val faceBookSignInBtn = view.findViewById<View>(R.id.facebook_sign_in_button)

            tvSignIn.setOnClickListener {
                val intent = Intent(context, LoginDetailActivity::class.java)
                intent.putExtra(Constants.BUNDLE_IS_FROM_PROFILE, false)
                startActivity(intent)
            }


            googleSignInBtn.setOnClickListener {
                googleSingIn = GoogleSingIn(context!!)
                googleSingIn!!.checkUser()
            }

            val loginButton = view.findViewById<LoginButton>(R.id.fb_login_button)
            faceBookSignInBtn.setOnClickListener {
                callbackManager = CallbackManager.Factory.create()

                if (loginButton != null) {
                    loginButton.setReadPermissions("public_profile", "email", "user_friends")

                    if (Helper.isConnected(context!!)) {
                        val lphApplication = activity!!.applicationContext as LPHApplication
                        lphApplication.isFromFavoriteFbLogin = true
                        isFromFb = true
                        loginButton.performClick()

                        loginButton.isPressed = true

                        loginButton.invalidate()

                        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                            override fun onSuccess(loginResult: LoginResult) {

                                LPHLog.d("Facebook Login: ", "Coming to success")
                                val request = GraphRequest.newMeRequest(
                                        loginResult.accessToken
                                ) { `object`, response ->
                                    // Application code
                                    LPHLog.d("response: ", response.toString() + "")
                                    LPHLog.d("response object: ", `object`.toString() + "")
                                    val resultObj: JSONObject = response.jsonObject
                                    val name: String = resultObj.optString("name")
                                    val password: String = resultObj.optString("id")
                                    val email: String = resultObj.optString("email")
                                    val profilePicUrl = resultObj.optJSONObject("picture").optJSONObject("data").optString("url")

                                    val weakReferenceContext: WeakReference<Context> = WeakReference(context!!)
                                    val registerAsync = Helper.RegisterAsync(weakReferenceContext, email, password, profilePicUrl, name, false, Constants.LoginType.Facebook)
                                    registerAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                                }
                                val parameters = Bundle()
                                parameters.putString("fields", "id,name,email,gender, birthday,picture.type(large)")
                                request.parameters = parameters
                                request.executeAsync()
                            }

                            override fun onCancel() {
                                LPHLog.d("Facebook Login: ", "Coming to onCancel")
                            }

                            override fun onError(error: FacebookException) {
                                LPHLog.d("Facebook Login: ", "Coming to error")
                                LPHLog.d("Facebook Login Error: ", error.message!!)
                            }
                        })

                        loginButton.isPressed = false

                        loginButton.invalidate()
                    } else {
                        Helper.showAlert(context!!, context!!.resources.getString(R.string.no_connection))
                    }
                }
            }
        } else {

            loginContainer.visibility = View.GONE
            favoritesContainer.visibility = View.VISIBLE
            rlProgressContainer = view.findViewById(R.id.rl_progressContainer)
            pbViewMore = view.findViewById(R.id.pb_view_more)
            newsAdapter = NewsAdapter(context!!, null, this)
            val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = newsAdapter

            recyclerView.addItemDecoration(DividerItemDecoration(context!!, DividerItemDecoration.VERTICAL))
            recyclerView.setHasFixedSize(true)
            recyclerView.itemAnimator = DefaultItemAnimator()

            pbViewMore!!.visibility = View.GONE
            swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_feeds_page)
            swipeRefreshLayout!!.visibility = View.GONE

//            val pullToRefresh = PullToRefresh()
//            swipeRefreshLayout!!.setColorSchemeResources(android.R.color.holo_green_dark, android.R.color.holo_red_dark, android.R.color.holo_orange_dark, android.R.color.holo_purple)
//            swipeRefreshLayout!!.setOnRefreshListener(pullToRefresh)
//            emptyContainer!!.setOnRefreshListener(pullToRefresh)

            rlProgressContainer!!.visibility = View.VISIBLE

            val tvRecentArticles = view.findViewById<TextView>(R.id.tv_recent_articles)
            tvRecentArticles.setOnClickListener { Handler().postDelayed({ onTabChange!!.onTabChange(0) }, 300) }

            loaderManager.initLoader(Constants.URL_FAVORITE_NEWS_LOADER, arguments, this)
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
    }

    /*private inner class PullToRefresh : SwipeRefreshLayout.OnRefreshListener {
        override fun onRefresh() {
            pbViewMore!!.visibility = View.GONE
            swipeRefreshLayout!!.isRefreshing = false
            emptyContainer!!.isRefreshing = false
            pageOffset = 0
            startAsync(pageLimit, pageOffset)
        }
    }*/

    private fun startAsync(pageLimit: Int, pageOffset: Int) {
        if(Helper.isConnected(context!!)) {
            val favoriteNewsAsync = FavoriteNewsAsync(context = this, pageLimit = pageLimit, pageOffset = pageOffset)
            favoriteNewsAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        } else {
            isAsyncDone = true
        }
    }

    fun restartLoader() {
        if(isAdded)
            loaderManager.restartLoader(Constants.URL_FAVORITE_NEWS_LOADER, null, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return NewsVo.getCursorLoader(context!!, NewsVo.NewsType.FAVORITE, 0)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        if (loader.id == Constants.URL_FAVORITE_NEWS_LOADER) {
            if(isAsyncDone) {
                swipeRefreshLayout!!.visibility = View.VISIBLE
                rlProgressContainer!!.visibility = View.GONE
            }

            if (cursor != null) {
                newsAdapter!!.swapCursor(cursor)
                newsAdapter!!.notifyDataSetChanged()
                if (isAsyncDone && cursor.count > 0) {
                    swipeRefreshLayout!!.visibility = View.VISIBLE
                    emptyContainer!!.visibility = View.GONE
                } else if(isAsyncDone){
                    emptyContainer!!.visibility = View.VISIBLE
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
        startActivityForResult(newsDetailIntent, Constants.REQUEST_CODE_FAVORITE_NEWS)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity != null)
            activity!!.registerReceiver(mFavoriteNewsBroadCast, IntentFilter(Constants.BROADCAST_FAVORITE_NEWS))
    }

    override fun onDetach() {
        super.onDetach()
        if (activity != null)
            activity!!.unregisterReceiver(mFavoriteNewsBroadCast)
    }

    override fun onPause() {
        super.onPause()
        if (googleSingIn != null)
            googleSingIn!!.pause()
    }

    override fun onStop() {
        super.onStop()
        if (googleSingIn != null)
            googleSingIn!!.pause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CODE_FAVORITE_NEWS && resultCode == Activity.RESULT_OK) {
            restartLoader()
            val intent1 = Intent(Constants.BROADCAST_RECENT_NEWS)
            context!!.sendBroadcast(intent1)

            val intent2 = Intent(Constants.BROADCAST_CATEGORIES)
            context!!.sendBroadcast(intent2)

        } else if (isFromFb) {
            callbackManager!!.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRefresh() {
        restartLoader()
        val intent1 = Intent(Constants.BROADCAST_RECENT_NEWS)
        context!!.sendBroadcast(intent1)

        val intent2 = Intent(Constants.BROADCAST_CATEGORIES)
        context!!.sendBroadcast(intent2)
    }

    private class FavoriteNewsAsync internal constructor(context: FavoriteNewsFragment, private val pageLimit: Int, private val pageOffset: Int) : AsyncTask<Void, Void, Response<ArrayList<NewsVo>>>() {

        private val context: WeakReference<FavoriteNewsFragment> = WeakReference(context)

        override fun doInBackground(vararg p0: Void?): Response<ArrayList<NewsVo>> {
            var response = Response<ArrayList<NewsVo>>()
            try {
                val lphService = LPHServiceFactory.getCALFService(context.get()?.context!!)
                response = lphService.favoritesNews(pageLimit, pageOffset)
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

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment FavoriteNewsFragment.
         */
        fun newInstance(): FavoriteNewsFragment {
            return FavoriteNewsFragment()
        }
    }
}
