package org.lovepeaceharmony.android.app.ui.fragment

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import org.lovepeaceharmony.android.app.R
import org.lovepeaceharmony.android.app.databinding.FragmentNewsBinding
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Created by Naveen Kumar M on 10/11/17.
 */
class NewsFragment : Fragment(R.layout.fragment_news) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FragmentNewsBinding.bind(view).apply {
            webView.webViewClient = WebViewClient()
            webView.loadUrl("https://www.drsha.com/news/")
        }
    }
}
