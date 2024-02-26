package org.lovepeaceharmony.android.app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import org.lovepeaceharmony.android.app.R
import org.lovepeaceharmony.android.app.databinding.FragmentNewsBinding


/**
 * A simple [Fragment] subclass.
 * Created by Naveen Kumar M on 10/11/17.
 */
class NewsFragment : Fragment(R.layout.fragment_news) {

    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentNewsBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadWebView()
    }

    private fun loadWebView() = with(binding.webView) {
        webViewClient = WebViewClient()
        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                binding.progress.isVisible = newProgress != 100
            }
        }
        loadUrl("https://www.drsha.com/news/")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.webView.webChromeClient = null
        _binding = null
    }
}
