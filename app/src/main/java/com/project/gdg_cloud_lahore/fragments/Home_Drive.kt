package com.project.gdg_cloud_lahore.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.project.gdg_cloud_lahore.R

class Home_Drive : Fragment() {

    private lateinit var webView: WebView
    private lateinit var splashImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home__drive, container, false)

        // Initialize views
        webView = view.findViewById(R.id.driveWebView)
        splashImage = view.findViewById(R.id.splashImage)

        // Load the splash GIF using Glide
        Glide.with(this)
            .asGif()
            .load(R.drawable.splash) // Replace with your GIF resource name
            .into(splashImage)

        // Configure WebView
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                splashImage.visibility = View.VISIBLE // Show splash while loading
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                splashImage.visibility = View.GONE // Hide splash after loading
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }
        }

        // Load the folder URL in WebView
        webView.loadUrl("https://drive.google.com/drive/folders/1dTR4s1KLCMzWN6qBuVRtqjj-8jLpkGH3?usp=sharing")

        return view
    }
}
