package com.yang.lovechat.helper

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import com.yang.lovechat.R
import com.yang.lovechat.base.bus.EventBus
import com.yang.lovechat.base.bus.EventBus.postValue
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.InstructionType
import com.yang.lovechat.data.MessageData
import com.yang.lovechat.data.MessageType
import com.yang.lovechat.databinding.FloatMessageBinding
import com.yang.lovechat.ui.activity.MessageActivity
import com.yang.lovechat.util.blurImageRequestOptions
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.createIntent
import com.yang.lovechat.util.edgeToEdgeTop
import com.yang.lovechat.util.loadImage
import com.yang.lovechat.util.mTranslation
import com.yang.lovechat.util.startActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import kotlin.apply
import kotlin.let

object FloatMessageHelper : Application.ActivityLifecycleCallbacks {

    private val TAG = "FloatMessageHelper"

    private var currentActivityRef: WeakReference<Activity>? = null

    private var floatingViewRef: WeakReference<View>? = null

    private var addAnimator: ObjectAnimator? = null
    private var closeAnimator: ObjectAnimator? = null

    private var startY = 0f
    private var hasSlid = false
    private var isClosing = false

    private val helperScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var autoCloseJob: Job? = null

    @SuppressLint("ClickableViewAccessibility")
    fun showFloatMessage(sendId: String, message: MessageData, instructionType: String? = null) {

        val isMe = sendId == UserInfoHold.userId.toString()

        if (null == instructionType && isMe) return



        if (AppConstant.Constant.isShowBuy) return

        val currentActivity = currentActivityRef?.get()
        if (currentActivity == null || currentActivity.isFinishing || currentActivity.isDestroyed) {
            return
        }

        if (currentActivity is MessageActivity) return

        val userName = if (isMe) message.friendName else message.userName

        val userAvatar = if (isMe) message.friendAvatar else message.userAvatar

        val userId = if (isMe) message.friendUserId else message.userId

        cancelAutoCloseJob()

        removeImmediate()

        val windowManager = currentActivity.getSystemService(Context.WINDOW_SERVICE) as WindowManager


        val resultFloatView: View? = when(message.msgType){

            MessageType.TEXT.type,MessageType.IMAGE_VIDEO.type ->{

                val mFloatMessageBinding =
                    FloatMessageBinding.inflate(LayoutInflater.from(currentActivity))

                mFloatMessageBinding.sclContainer.shapeDrawableBuilder.setSolidColor(currentActivity.getColor(R.color.color_00E676)).intoBackground()

                mFloatMessageBinding.tvName.text = userName

                mFloatMessageBinding.ivAvatar.loadImage(userAvatar)

                val msgContent = when(message.msgType){

                    MessageType.IMAGE_VIDEO.type ->{
                        "[Photo/Video]"
                    }
                    else -> message.msgContent
                }

                mFloatMessageBinding.tvMessage.text = msgContent

                mFloatMessageBinding.root.clicks {

                    ChatHelper.removeLast()


                    currentActivity.createIntent(MessageActivity::class.java)
                        .putExtra(AppConstant.Constant.CONV_ID, message.convId)
                        .putExtra(AppConstant.Constant.FRIEND_AVATAR, userAvatar)
                        .putExtra(AppConstant.Constant.FRIEND_NAME, userName)
                        .putExtra(AppConstant.Constant.FRIEND_USER_ID, userId)
                        .startActivity(currentActivity)

                    removeImmediate()

                }

                mFloatMessageBinding.root
            }
            MessageType.SYSTEM.type ->{

                when(instructionType) {
                    InstructionType.MATCH.value -> {

                        val mFloatMessageBinding =
                            FloatMessageBinding.inflate(LayoutInflater.from(currentActivity))

                        mFloatMessageBinding.sclContainer.shapeDrawableBuilder.setSolidColor(currentActivity.getColor(R.color.color_FF5ACD)).intoBackground()


                        mFloatMessageBinding.tvName.text = userName

                        mFloatMessageBinding.ivAvatar.loadImage(userAvatar)

                        val matchText = "Perfect Match!"

                        val msgContent = matchText

                        mFloatMessageBinding.tvMessage.text = msgContent

                        mFloatMessageBinding.tvMessage.isEnabled = false

                        mFloatMessageBinding.root.clicks {

                            ChatHelper.removeLast()

                            currentActivity.createIntent(MessageActivity::class.java)
                                .putExtra(AppConstant.Constant.CONV_ID, message.convId)
                                .putExtra(AppConstant.Constant.FRIEND_AVATAR, userAvatar)
                                .putExtra(AppConstant.Constant.FRIEND_NAME, userName)
                                .putExtra(AppConstant.Constant.FRIEND_USER_ID, userId)
                                .startActivity(currentActivity)

                            removeImmediate()

                        }

                        mFloatMessageBinding.root
                    }
                    InstructionType.FAST_CONNECT.value -> {

                        val mFloatMessageBinding =
                            FloatMessageBinding.inflate(LayoutInflater.from(currentActivity))

                        mFloatMessageBinding.sclContainer.shapeDrawableBuilder.setSolidColor(currentActivity.getColor(R.color.color_FF5ACD)).intoBackground()



                        mFloatMessageBinding.tvName.text = userName

                        mFloatMessageBinding.ivAvatar.loadImage(userAvatar)

                        val matchText = "Perfect Match!"

                        val msgContent = matchText

                        mFloatMessageBinding.tvMessage.text = msgContent

                        mFloatMessageBinding.tvMessage.isEnabled = false

                        mFloatMessageBinding.root.clicks {

                            ChatHelper.removeLast()

                            currentActivity.createIntent(MessageActivity::class.java)
                                .putExtra(AppConstant.Constant.CONV_ID, message.convId)
                                .putExtra(AppConstant.Constant.FRIEND_AVATAR, userAvatar)
                                .putExtra(AppConstant.Constant.FRIEND_NAME, userName)
                                .putExtra(AppConstant.Constant.FRIEND_USER_ID, userId)
                                .startActivity(currentActivity)

                            removeImmediate()

                        }

                        mFloatMessageBinding.root
                    }

                    InstructionType.VIEW_I.value -> {

                        val mFloatMessageBinding =
                            FloatMessageBinding.inflate(LayoutInflater.from(currentActivity))



                        mFloatMessageBinding.sclContainer.shapeDrawableBuilder.setSolidColor(currentActivity.getColor(R.color.color_FF9800)).intoBackground()

                        mFloatMessageBinding.tvTitle.text = "Who Viewed Me"
                        mFloatMessageBinding.tvName.text = userName

                        if (UserInfoHold.isVip){
                            mFloatMessageBinding.ivAvatar.loadImage(userAvatar)
                        }else{
                            mFloatMessageBinding.ivAvatar.loadImage(userAvatar, customOption = blurImageRequestOptions)
                        }

                        val matchText = "See Who Viewed Me!"

                        val msgContent = matchText

                        mFloatMessageBinding.tvMessage.text = msgContent

                        mFloatMessageBinding.tvMessage.isEnabled = false

                        mFloatMessageBinding.root.clicks {

                            EventBus.with(AppConstant.EventConstant.EVENT_SET_PAGE).postValue(1)
                            EventBus.with(AppConstant.EventConstant.EVENT_SET_LIKE_PAGE).postValue(2)

                            removeImmediate()

                        }

                        EventBus.with(AppConstant.EventConstant.EVENT_RECEIVED_VIEW_I).postValue(userId.toString())

                        mFloatMessageBinding.root




                    }
                    InstructionType.LIKE_I.value -> {

                        val mFloatMessageBinding =
                            FloatMessageBinding.inflate(LayoutInflater.from(currentActivity))

                        mFloatMessageBinding.sclContainer.shapeDrawableBuilder.setSolidColor(currentActivity.getColor(R.color.color_B45CFF)).intoBackground()

                        mFloatMessageBinding.tvTitle.text = "Who Like Me"
                        mFloatMessageBinding.tvName.text = userName

                        if (UserInfoHold.isVip){
                            mFloatMessageBinding.ivAvatar.loadImage(userAvatar)
                        }else{
                            mFloatMessageBinding.ivAvatar.loadImage(userAvatar, customOption = blurImageRequestOptions)
                        }


                        val matchText = "See Who Like Me!"

                        val msgContent = matchText

                        mFloatMessageBinding.tvMessage.text = msgContent

                        mFloatMessageBinding.tvMessage.isEnabled = false

                        mFloatMessageBinding.root.clicks {

                            EventBus.with(AppConstant.EventConstant.EVENT_SET_PAGE).postValue(1)
                            EventBus.with(AppConstant.EventConstant.EVENT_SET_LIKE_PAGE).postValue(0)

                            removeImmediate()

                        }

                        EventBus.with(AppConstant.EventConstant.EVENT_RECEIVED_LIKE_I).postValue(userId.toString())

                        mFloatMessageBinding.root


                    }

                    else -> null

                }

            }
            else -> {

                null
            }

        }

        if (null == resultFloatView) return

        floatingViewRef = WeakReference(resultFloatView)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.CENTER

        floatingViewRef?.get()?.let { v ->
            try {
                v.edgeToEdgeTop()
                windowManager.addView(v, params)
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }

            addAnimator = v.mTranslation(View.TRANSLATION_Y, -100f, 0f).apply {
                duration = 300
                interpolator = LinearInterpolator()
                start()
            }

            v.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startY = event.rawY
                        hasSlid = false
                        false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaY = startY - event.rawY
                        if (deltaY > 20) {
                            if (!hasSlid) {
                                hasSlid = true
                                closeWithAnimation()
                            }
                            return@setOnTouchListener true
                        }
                        false
                    }
                    MotionEvent.ACTION_UP -> hasSlid
                    else -> false
                }
            }

            autoCloseJob = helperScope.launch {
                delay(4000)
                if (isActive) {
                    closeWithAnimation()
                }
            }

        }

    }

    private fun cancelAutoCloseJob() {
        autoCloseJob?.cancel()
        autoCloseJob = null
    }

    private fun closeWithAnimation() {
        if (isClosing) return
        isClosing = true

        val view = floatingViewRef?.get()
        if (view == null) {
            isClosing = false
            return
        }

        val currentY = view.y
        closeAnimator = view.mTranslation(View.TRANSLATION_Y, currentY, -view.height.toFloat() - 10f).apply {
            duration = 300
            interpolator = LinearInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    isClosing = false
                    removeImmediate()
                }
            })
            start()
        }
    }


    private fun removeImmediate() {

        val view = floatingViewRef?.get()
        if (view != null) {
            view.visibility = View.GONE
            if (view.parent != null) {
                val wm = view.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                try {
                    wm.removeViewImmediate(view)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        floatingViewRef?.clear()
        floatingViewRef = null

        addAnimator?.cancel()
        closeAnimator?.cancel()
        addAnimator = null
        closeAnimator = null
    }

    fun dismiss() {
        cancelAutoCloseJob()
        removeImmediate()
    }


    override fun onActivityResumed(activity: Activity) {
        currentActivityRef = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        if (currentActivityRef?.get() == activity) {
            dismiss()
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivityRef?.get() == activity) {
            dismiss()
            currentActivityRef?.clear()
            currentActivityRef = null
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
}