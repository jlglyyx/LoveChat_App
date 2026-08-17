package com.yang.lovechat.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.annotation.OptIn
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.yang.lovechat.databinding.ViewVideoPlayerBinding
import com.yang.lovechat.manager.VideoPlayerManager
import com.yang.lovechat.util.loadImage
import kotlin.let
import kotlin.ranges.coerceIn
import kotlin.text.format

@OptIn(UnstableApi::class)
class VideoPlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), DefaultLifecycleObserver {

    val binding = ViewVideoPlayerBinding.inflate(LayoutInflater.from(context), this, true)
    private var videoUrl: Any? = null

    // 缩放控制
    private var mScaleFactor = 1.0f
    private val mMinScale = 1.0f
    private val mMaxScale = 5.0f

    private var isUserTracking = false

    val pvVideo: PlayerView get() = binding.pvVideo



    init {
        setupSeekBar()
        setupGestures()


        binding.ivPlay.setOnClickListener{
            togglePlayPause()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestures() {
        // 双指缩放探测器
        val scaleDetector = ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    mScaleFactor *= detector.scaleFactor
                    mScaleFactor = mScaleFactor.coerceIn(mMinScale, mMaxScale)

                    // 核心：作用于底层的 TextureView
                    getVideoTextureView()?.let {
                        it.scaleX = mScaleFactor
                        it.scaleY = mScaleFactor
                    }
                    return true
                }
            })

        // 双击与单击探测器
        val gestureDetector =
            GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    togglePlayPause() // 执行 播放/暂停 逻辑
                    return true
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    if (mScaleFactor > 1.0f) {
                        resetScale() // 如果放大了，双击还原
                    } else {
                        // 切换 适应/铺满 模式
                        pvVideo.resizeMode =  AspectRatioFrameLayout.RESIZE_MODE_ZOOM
//                            if (pvVideo.resizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM) {
//                                AspectRatioFrameLayout.RESIZE_MODE_ZOOM
//                            } else {
//                                AspectRatioFrameLayout.RESIZE_MODE_ZOOM
//                            }
                    }
                    return true
                }
            })

        binding.pvVideo.setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            true
        }
    }






    private fun setupSeekBar() {
        binding.videoProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                if (fromUser) {
                    // 拖动时只更新文字提示，不触发 player.seekTo (防止卡顿)
                    val total = pvVideo.player?.duration ?: 0L
                    val cur = (p.toFloat() / 100 * total).toLong()
                    binding.tvToProgress.text = "${formatTime(cur)} / ${formatTime(total)}"
                }
            }

            override fun onStartTrackingTouch(s: SeekBar?) {
                isUserTracking = true // 拦截自动更新
                binding.tvToProgress.visibility = VISIBLE
            }

            override fun onStopTrackingTouch(s: SeekBar?) {
                val total = pvVideo.player?.duration ?: 0L
                val pos = (s!!.progress.toFloat() / 100 * total).toLong()
                VideoPlayerManager.seekTo(pos)

                binding.tvToProgress.visibility = GONE
                // 延迟一小会儿再恢复自动更新，等待播放器 seek 完成，防止进度条闪回
                postDelayed({ isUserTracking = false }, 50)
            }
        })
    }

    fun setVideoData(url: Any,coverUrl: String? = null) {

        if (null == coverUrl) {
            binding.ivCover.loadImage(url, isVideo = true)
        }else{
            binding.ivCover.loadImage(coverUrl)
        }

        this.videoUrl = url

        resetUi()
    }

    fun setCoverUrl(coverUrl: Any?) {

        binding.ivCover.loadImage(coverUrl)
    }



    fun resetUi() {
        resetScale()
        binding.pvVideo.player = null
        binding.ivCover.visibility = VISIBLE
        binding.ivPlay.visibility = VISIBLE
        binding.videoProgress.progress = 0
        binding.videoProgress.secondaryProgress = 0
        binding.tvProgress.text = "00:00 / 00:00"
    }

    private fun resetScale() {
        mScaleFactor = 1.0f
        getVideoTextureView()?.animate()?.scaleX(1.0f)?.scaleY(1.0f)?.setDuration(200)?.start()
    }

    // 关键：层级寻找 TextureView
    private fun getVideoTextureView(): View? {
        val frameLayout = pvVideo.getChildAt(0) as? AspectRatioFrameLayout
        return frameLayout?.getChildAt(0)
    }

    fun onProgressUpdate(current: Long, total: Long, buffer: Long) {
        if (total <= 0 || isUserTracking) return // 如果用户正在拖动，直接跳过自动更新

        val p = (current.toFloat() / total * 100).toInt()
        val b = (buffer.toFloat() / total * 100).toInt()
        binding.videoProgress.progress = p
        binding.videoProgress.secondaryProgress = b
        binding.tvProgress.text = "${formatTime(current)} / ${formatTime(total)}"
    }

    fun onStateChanged(state: Int) {
        when (state) {
            Player.STATE_READY -> {
                // 真正开始渲染画面时才彻底隐藏封面，防止黑屏闪烁
                binding.ivCover.visibility = GONE
                binding.ivPlay.visibility = if (binding.pvVideo.player?.isPlaying == true) GONE else VISIBLE
                binding.tvProgress.text = "${formatTime(0)} / ${formatTime(binding.pvVideo.player?.duration?:0)}"
            }
            Player.STATE_BUFFERING -> {
                // 缓冲时可以显示一个 Loading，这里暂不处理
            }
            Player.STATE_ENDED -> {
                // 播放结束显示封面和播放按钮
                binding.ivCover.visibility = VISIBLE
                binding.ivPlay.visibility = VISIBLE
            }
        }
    }



    fun onTogglePlay(isPlaying: Boolean) {
        binding.ivPlay.visibility = if (isPlaying) GONE else VISIBLE
//        binding.ivCover.visibility = if (isPlaying) View.GONE else View.VISIBLE
    }

    fun togglePlayPause(){
        val player = binding.pvVideo.player
        if (player == null) {
            // 还没开始播，触发 Manager 加载
            videoUrl?.let { VideoPlayerManager.play(this, it) }
        } else {
            // 已经在播了，切换状态即可
            if (player.isPlaying) player.pause() else player.play()
        }
    }




    private fun formatTime(ms: Long): String {
        val s = (ms / 1000) % 60
        val m = (ms / (1000 * 60)) % 60
        return String.format("%02d:%02d", m, s)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        VideoPlayerManager.pause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        VideoPlayerManager.releaseCurrent()
    }
}