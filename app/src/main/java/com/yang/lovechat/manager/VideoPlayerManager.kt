package com.yang.lovechat.manager

import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.yang.lovechat.app.BaseApplication
import com.yang.lovechat.util.getRealUrl
import com.yang.lovechat.util.showShort
import com.yang.lovechat.util.toJson
import com.yang.lovechat.widget.VideoPlayerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlin.apply
import kotlin.let

object VideoPlayerManager {

    private var exoPlayer: ExoPlayer? = null
    private var currentBindingView: VideoPlayerView? = null

    // 协程作用域：跟随整个 App 生命周期
    private val playerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

    // 状态分发：让外面（Activity）也能监听播放状态
    private val _playbackState = MutableStateFlow(Player.STATE_IDLE)
    val playbackState = _playbackState.asStateFlow()

    private fun getPlayer(): ExoPlayer {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(BaseApplication.mApplication).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        _playbackState.value = state
                        currentBindingView?.onStateChanged(state)
                        if (playbackState == Player.STATE_READY) {
                            // 通知 View：视频准备好了，把总时长传过去
                            startProgressTracker()
                        }
                    }
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        currentBindingView?.onTogglePlay(isPlaying)
                        if (isPlaying) startProgressTracker() else stopProgressTracker()
                    }
                    override fun onPlayerError(error: PlaybackException) {
                        // 报错时也要停止进度追踪，防止死循环尝试获取错误状态下的进度
                        stopProgressTracker()
                        _playbackState.value = Player.STATE_IDLE
                        showShort("pay error")
                        error.printStackTrace()
                        Log.i("TAG", "onPlayerError: ${error.toJson()}")
                        // 可以在这里通知 UI 显示“播放失败”
                    }
                })
            }
        }
        return exoPlayer!!
    }

    /**
     * 协程轮询进度
     */
    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = playerScope.launch {
            while (isActive) {
                exoPlayer?.let {
                    if (it.isPlaying) {
                        currentBindingView?.onProgressUpdate(
                            it.currentPosition,
                            it.duration,
                            it.bufferedPosition
                        )
                    }
                }
                delay(500) // 每半秒更新一次，兼顾流畅与性能
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }




    fun play(view: VideoPlayerView, url: Any) {
        // 核心：切换 View 时重置旧 View 状态
        if (currentBindingView != null && currentBindingView != view) {
            currentBindingView?.resetUi()
        }

        currentBindingView = view
        val player = getPlayer()

        // 绑定 PlayerView
        if (view.pvVideo.player != player) {
            view.pvVideo.player = player
        }

        val realUrl = getRealUrl(url)

        if (realUrl is Uri){
            player.setMediaItem(MediaItem.fromUri(realUrl))
        }else{
            player.setMediaItem(MediaItem.fromUri(realUrl.toString()))
        }

        player.prepare()
        player.play()
    }



    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }

    fun pause() {
        exoPlayer?.pause()
    }

    /**
     * RecyclerView 滑动关键点：如果滑出的 View 是当前正在播放的，则重置播放器
     */
    fun resetIfMatching(view: VideoPlayerView) {
        if (currentBindingView == view) {
            stopProgressTracker()
            exoPlayer?.stop()
            currentBindingView?.resetUi()
            currentBindingView = null
        }
    }

    fun releaseCurrent() {
        exoPlayer?.stop()
        exoPlayer?.clearMediaItems()
        currentBindingView = null
    }

    fun release() {
        stopProgressTracker()
        exoPlayer?.release()
        exoPlayer = null
    }
}