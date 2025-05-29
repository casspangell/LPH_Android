package org.lovepeaceharmony.androidapp.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import org.lovepeaceharmony.androidapp.R

class WebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var exitButton: ImageButton

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        exitButton = findViewById(R.id.exitButton)

        // Get URL from intent
        val url = intent.getStringExtra("url")
        if (url != null) {
            // Configure WebView
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                setSupportZoom(true)
            }

            // Handle navigation
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    progressBar.visibility = View.GONE
                }
            }

            // Load URL
            webView.loadUrl(url)
        }

        // Handle exit button
        exitButton.setOnClickListener {
            finish()
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
} 