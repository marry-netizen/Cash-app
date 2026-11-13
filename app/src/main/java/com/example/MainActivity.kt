package com.example

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val isDark = isSystemInDarkTheme()
      val bgColor = if (isDark) Color(0xFF0B1117) else Color(0xFFF3F5F7)
      Box(
        modifier =
          Modifier.fillMaxSize()
            .background(bgColor)
            .windowInsetsPadding(WindowInsets.safeDrawing)
      ) {
        OfflineLedgerWebView()
      }
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OfflineLedgerWebView() {
  AndroidView(
    modifier = Modifier.fillMaxSize().testTag("ledger_webview"),
    factory = { context ->
      WebView(context).apply {
        settings.apply {
          javaScriptEnabled = true
          domStorageEnabled = true
          allowFileAccess = true
          allowContentAccess = false
          cacheMode = WebSettings.LOAD_NO_CACHE
          builtInZoomControls = false
          displayZoomControls = false
        }
        webViewClient =
          object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
              view: WebView?,
              request: WebResourceRequest?,
            ): Boolean {
              val url = request?.url?.toString().orEmpty()
              return !url.startsWith("file:///android_asset/")
            }
          }
        webChromeClient =
          object : WebChromeClient() {
            override fun onJsConfirm(
              view: WebView?,
              url: String?,
              message: String?,
              result: JsResult?,
            ): Boolean {
              AlertDialog.Builder(context)
                .setMessage(message ?: "Confirm?")
                .setPositiveButton("OK") { _, _ -> result?.confirm() }
                .setNegativeButton("Cancel") { _, _ -> result?.cancel() }
                .setOnCancelListener { result?.cancel() }
                .show()
              return true
            }

            override fun onJsAlert(
              view: WebView?,
              url: String?,
              message: String?,
              result: JsResult?,
            ): Boolean {
              AlertDialog.Builder(context)
                .setMessage(message.orEmpty())
                .setPositiveButton("OK") { _, _ -> result?.confirm() }
                .setOnCancelListener { result?.confirm() }
                .show()
              return true
            }
          }
        loadUrl("file:///android_asset/index.html")
      }
    },
  )
}
