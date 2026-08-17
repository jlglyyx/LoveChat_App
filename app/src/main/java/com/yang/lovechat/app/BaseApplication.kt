package com.yang.lovechat.app

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.blankj.utilcode.util.ProcessUtils
import com.bumptech.glide.Glide
import com.danikula.videocache.HttpProxyCacheServer
import com.yang.lovechat.helper.FloatMessageHelper
import com.yang.lovechat.im.ChatWebSocket
import com.yang.lovechat.manager.AppForegroundManager

class BaseApplication : Application() {


    private var proxy: HttpProxyCacheServer? = null

    companion object {

        private const val TAG = "BaseApplication"

        lateinit var mApplication: BaseApplication


        var isAppForeground = true

    }

    fun getProxy(): HttpProxyCacheServer {

        return proxy ?: newProxy().also { proxy = it }
    }

    private fun newProxy(): HttpProxyCacheServer {
        return HttpProxyCacheServer.Builder(this)
            .maxCacheSize((1024 * 1024 * 1024).toLong()) // 1 Gb for cache
            .build()
    }


    override fun onCreate() {
        mApplication = this

        super.onCreate()
        initGlide(mApplication)

        ChatWebSocket.init(this)

        AppForegroundManager.init()

        registerActivityLifecycleCallbacks(FloatMessageHelper)


        if (ProcessUtils.isMainProcess()) {

            try {

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }



        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                isAppForeground = true

//                FlowBus.with(AppConstant.EventConstant.APP_ENTERED_FOREGROUND).postValue(true)

            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                isAppForeground = false

            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        mApplication = this
    }


    private fun initGlide(application: BaseApplication) {
        Glide.get(application)
    }



    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        try {
            if (level >= TRIM_MEMORY_UI_HIDDEN) {
                Glide.get(this).clearMemory()
            } else {
                Glide.get(this).onTrimMemory(level)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
        super.onLowMemory()
        try {
            Glide.get(this).onLowMemory()
            Glide.get(this).clearMemory()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}