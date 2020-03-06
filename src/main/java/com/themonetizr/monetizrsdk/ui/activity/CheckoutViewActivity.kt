package com.themonetizr.monetizrsdk.ui.activity

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.checkout_view_activity.*


class CheckoutViewActivity : AppCompatActivity() {

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.getStringExtra(WEBSITE_ADDRESS)

        if (url == null || url.isEmpty()) finish()

        setContentView(com.themonetizr.monetizrsdk.R.layout.checkout_view_activity)
        val webView = findViewById(com.themonetizr.monetizrsdk.R.id.checkout_webview) as WebView
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.loadUrl(url)

        backToProduct.setOnClickListener { finish() }
        hideStatusBar()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        hideStatusBar()
    }

    override fun onResume() {
        super.onResume()
        hideStatusBar()
    }

    private fun hideStatusBar() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    companion object {
        const val WEBSITE_ADDRESS = "WEBSITE_ADDRESS"
    }
}
