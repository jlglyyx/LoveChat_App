package com.yang.lovechat.manager

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.yang.lovechat.base.bus.EventBus
import com.yang.lovechat.base.bus.EventBus.postValue
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.helper.UserInfoHold
import com.yang.lovechat.im.ChatWebSocket

object AppForegroundManager : DefaultLifecycleObserver {

    private const val TAG = "AppForegroundManager"


    fun init() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        EventBus.with(AppConstant.EventConstant.EVENT_FOREGROUND_CHANGE).postValue(true)

        if (!ChatWebSocket.isConnect()) {
            ChatWebSocket.startClient(UserInfoHold.userId.toString(), force = true)
        }
        Log.i(TAG, "onStart: 进入前台")
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.i(TAG, "onStart: 进入后台")
        EventBus.with(AppConstant.EventConstant.EVENT_FOREGROUND_CHANGE).postValue(false)
    }
}