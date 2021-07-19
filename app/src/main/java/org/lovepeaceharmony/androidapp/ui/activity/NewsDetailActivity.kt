package org.lovepeaceharmony.androidapp.ui.activity

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.model.NewsVo
import org.lovepeaceharmony.androidapp.utility.*
import java.lang.ref.WeakReference


/**
 * NewsDetailActivity
 * Created by Naveen Kumar M on 18/12/17.
 */

class NewsDetailActivity : AppCompatActivity(), OnRefreshCallback{
    private var context: Context? = null
    private var newsId: Int = 0
    private var categoryId: Int = 0
    private var newsVo: NewsVo? = null
    private var progressBar: ProgressBar? = null
    private var ivFavorite: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        setContentView(R.layout.activity_news_detail)
        handleIntent()
        initView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleIntent() {
        if (intent.extras != null) {
            newsId = intent?.extras!!.getInt(Constants.BUNDLE_NEWS_ID, 0)
            newsVo = NewsVo.getNewsVo(context!!, newsId)
            categoryId = intent?.extras!!.getInt(Constants.BUNDLE_CATEGORY_ID, 0)
        }
    }

    private fun initView() {
        if (newsVo != null) {
            ivFavorite = findViewById(R.id.iv_favorite)
            val tvBack = findViewById<TextView>(R.id.tv_back)
            progressBar = findViewById<ProgressBar>(R.id.progressBar1)
            val webView = findViewById<WebView>(R.id.web_view)

            if (newsVo!!.isFavorite)
                ivFavorite?.setImageResource(R.drawable.ic_star_black_24dp)
            else
                ivFavorite?.setImageResource(R.drawable.ic_star_border_black_24dp)

            val settings = webView.getSettings()
            settings.setJavaScriptEnabled(true)
            webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY)


            webView.setWebViewClient(object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    progressBar?.visibility = View.VISIBLE
                    view.loadUrl(request.url.toString())
                    return true
                }

                override fun onPageFinished(view: WebView, url: String) {
                  progressBar?.visibility = View.GONE
                }

                override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                    LPHLog.d("Error: " + description)
//                    Helper.showAlert(this@NewsDetailActivity, description)
                }

                @TargetApi(android.os.Build.VERSION_CODES.M)
                override fun onReceivedError(view: WebView, req: WebResourceRequest, rerr: WebResourceError) {
                    // Redirect to deprecated method, so you can use it in all SDK versions
                    onReceivedError(view, rerr.errorCode, rerr.description.toString(), req.url.toString())
                }
            })

            newsVo?.newsDetailUrl?.let { webView.loadUrl(it) }

            /*val aQuery = AQuery(context)
            val imageWidth = Helper.getDisplayWidth(context!!)
            val ivNewsPic = findViewById<ImageView>(R.id.iv_image)
            val tvHeading = findViewById<TextView>(R.id.tv_heading)
            val tvDate = findViewById<TextView>(R.id.tv_date)
            val tvDescription = findViewById<TextView>(R.id.tv_description)

            if (newsVo!!.newsPicUrl != null && !newsVo?.newsPicUrl!!.isEmpty() && !newsVo!!.newsPicUrl.equals("null", ignoreCase = true))
                aQuery.id(ivNewsPic).image(newsVo!!.newsPicUrl, true, true, imageWidth, 0)

            if (newsVo!!.isFavorite)
                ivFavorite.setImageResource(R.drawable.ic_star_black_24dp)
            else
                ivFavorite.setImageResource(R.drawable.ic_star_border_black_24dp)

            tvHeading.text = newsVo!!.newsHeading
            tvDate.text = newsVo!!.newsDate
            tvDescription.text = newsVo!!.newsDescription*/

            ivFavorite?.setOnClickListener { view ->
                val isFavorite = !newsVo!!.isFavorite
                if(Helper.isConnected(context!!)) {
                    view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.bounce))
                    val weakReferenceContext = WeakReference(this.context!!)
                    val markFavoriteAsync = Helper.MarkFavoriteAsync(weakReferenceContext, newsId, isFavorite, this, categoryId)
                    markFavoriteAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

                } else {
                    Helper.showConfirmationAlertTwoButton(context!!, context!!.getString(R.string.internet_warning), object : ConfirmationAlertCallback {
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

            tvBack.setOnClickListener { onBackPressed() }
        }

    }

    override fun onRefresh() {
        newsVo = NewsVo.getNewsVo(context!!, newsId)
        if (newsVo!!.isFavorite)
            ivFavorite?.setImageResource(R.drawable.ic_star_black_24dp)
        else
            ivFavorite?.setImageResource(R.drawable.ic_star_border_black_24dp)
        val intent = Intent()
        intent.putExtra(Constants.BUNDLE_IS_REFRESH, true)
        setResult(RESULT_OK, intent)
    }
}
