package com.yang.lovechat.ui.activity

import android.graphics.Color
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import com.yang.lovechat.base.activity.BaseActivity
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.databinding.ActWebBinding
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.isAlive
import com.yang.lovechat.viewmodel.MainViewModel


class WebActivity: BaseActivity<ActWebBinding, MainViewModel>(ActWebBinding::inflate) {

    private var url = ""

    private var pageFinished = false

    private var progressFinished = false

    private var finalChecked = false


    override fun initView() {



        onBackPressedClick()


    }

    override fun initData() {

        val title = intent.getStringExtra(AppConstant.Constant.TITLE)?:""

        url = intent.getStringExtra(AppConstant.Constant.URL)?:url

        mViewBinding.appToolBar.mToolbarBinding.tvTitle.text = title

        initWebView()

        mViewBinding.appToolBar.mToolbarBinding.ivBack.clicks {

            if (mViewBinding.webView.canGoBack()){
                mViewBinding.webView.goBack()
            }else{
                finish()
            }
        }

    }

    override fun initViewModel() {
    }

    private fun initWebView() {

        withViewBinding {

            lifecycle.addObserver(ivLoad)


            webView.webViewClient = object : WebViewClient() {

                override fun onPageFinished(p0: WebView?, p1: String?) {
                    super.onPageFinished(p0, p1)
                    Log.i(TAG, "onPageFinished: 加载完成 ")
                    pageFinished = true
                    tryFinish()
                }

                override fun onReceivedError(p0: WebView?, p1: Int, p2: String?, p3: String?) {
                    super.onReceivedError(p0, p1, p2, p3)
                    Log.i(TAG, "onPageFinished: 加载失败 $p1  $p2  $p3")
                    if (lifecycle.isAlive()){
                        ivLoad.visibility = View.GONE
                    }

                }

            }


            webView.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(p0: WebView?, p1: Int) {
                    super.onProgressChanged(p0, p1)
                    Log.i(TAG, "onProgressChanged:加载进度 $p1 ${p0?.url}")
                    if (p1 >= 90) {
                        progressFinished = true
                        tryFinish()
                    }
                }

            }

            webView.setBackgroundColor(Color.TRANSPARENT)


            val webSettings = webView.settings
            webSettings.javaScriptEnabled = true
            webSettings.domStorageEnabled = true
            webSettings.cacheMode = WebSettings.LOAD_DEFAULT
            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
            webView.loadUrl(url)
        }




    }


    private fun tryFinish() {
        if (pageFinished && progressFinished && !finalChecked) {

            finalChecked = true

            if (this.lifecycle.isAlive()){
                withViewBinding {
                    webView.postDelayed({
                        webView.evaluateJavascript(
                            "(function(){return document.readyState})()"
                        ) { state ->
                            if (state.contains("complete") || state.contains("interactive")) {
                                onPageProbablyReady()
                            } else {
                                webView.postDelayed({
                                    onPageProbablyReady()
                                }, 300)
                            }
                        }
                    }, 200)
                }
            }

        }
    }

    private fun onPageProbablyReady() {

        if (this.lifecycle.isAlive()){
            withViewBinding {
                ivLoad.visibility = View.GONE
                ivLoad.stop()
            }
        }

    }


    override fun onResume() {
        super.onResume()
        mViewBinding.webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mViewBinding.webView.onPause()
    }



    fun onBackPressedClick() {

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mViewBinding.webView.canGoBack()) {
                    mViewBinding.webView.goBack()

                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }

        })


    }


    override fun onDestroy() {
        mViewBinding.webView.destroy()
        mViewBinding.webView.webChromeClient = null
        super.onDestroy()
    }



}