package com.srsssms

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_monitor.*

class ActivityMonitor : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitor)
        val myWebView: WebView = findViewById(R.id.webView)
        myWebView.settings.javaScriptEnabled = true
        myWebView.getSettings().setBuiltInZoomControls(false)
        myWebView.webViewClient = WebViewClient()
        myWebView.loadUrl("https://covid.srs-ssms.com/admin/login")

        btHomeMonitor.setOnClickListener {
            val intent = Intent(this@ActivityMonitor, ActivityMenu::class.java)
            startActivity(intent)
        }
    }
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        }
    }
}