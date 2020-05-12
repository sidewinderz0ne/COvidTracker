package com.srsssms

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import kotlinx.android.synthetic.main.activity_monitor.*


class ActivityMonitor : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitor)
        webView.settings.javaScriptEnabled = true
        webView.settings.builtInZoomControls = false
        webView.webViewClient = WebViewClient()
        webView.loadUrl("https://covid.srs-ssms.com/admin/login")
        lottieMonitor.setAnimation("covid.json")//ANIMATION WITH LOTTIE FOR CHECKING DEVICE
        lottieMonitor.loop(true)
        lottieMonitor.playAnimation()
        btHomeMonitor.setOnClickListener {
            val intent = Intent(this@ActivityMonitor, ActivityMenu::class.java)
            startActivity(intent)
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                progressMonitor.visibility = View.VISIBLE
                if (progress == 100){
                    progressMonitor.visibility = View.GONE
                    Handler().postDelayed({ // Stop animation (This will be after 3 seconds)
                        swipeRefresh.isRefreshing = false
                    }, 3000) // Delay in millis
                }
            }
        }
        swipeRefresh.setOnRefreshListener {
            webView.reload()
            webView.loadUrl("https://covid.srs-ssms.com/admin/home")
        }
    }
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        }
    }
}