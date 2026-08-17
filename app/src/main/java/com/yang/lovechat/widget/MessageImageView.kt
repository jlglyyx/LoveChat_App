package com.yang.lovechat.widget

import android.content.Context
import android.graphics.Outline
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.Utils
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.yang.lovechat.base.adapter.BaseRecyclerAdapter
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.constant.AppConstant.Constant.picturePrice
import com.yang.lovechat.constant.AppConstant.Constant.videoPrice
import com.yang.lovechat.data.MediaInfoData
import com.yang.lovechat.data.MessageData
import com.yang.lovechat.databinding.ItemGridImageBinding
import com.yang.lovechat.databinding.ViewMessageImageBinding
import com.yang.lovechat.util.blurImageRequestOptions
import com.yang.lovechat.util.dip2px
import com.yang.lovechat.util.formatListJson
import com.yang.lovechat.util.getTimeSecond
import com.yang.lovechat.util.loadImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MessageImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) , CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Main + SupervisorJob()



    private val mViewBinding: ViewMessageImageBinding by lazy {

        ViewMessageImageBinding.inflate(LayoutInflater.from(context), this, true)
    }


    var mAdapter: BaseRecyclerAdapter<MediaInfoData, ItemGridImageBinding>? = null

    var onItemClick: ((MutableList<MediaInfoData>, Int) -> Unit)? = null


    private val pictureWidth = 120f.dip2px(Utils.getApp())

    private val pictureHeight = (pictureWidth * 1.3f).toInt()

    private var timerJob: Job? = null

    private val radius = 10f.dip2px(context).toFloat()

    private var isMe = false

    var interceptTouch = false

    private var isLoading = false

    private var isPrivateMessage = false




    override fun onFinishInflate() {
        super.onFinishInflate()

        mViewBinding.recyclerView.clipToOutline = true
        mViewBinding.recyclerView.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radius)
            }
        }
//        initRecyclerView()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return interceptTouch
    }


    private fun initAdapter() {

        if (null == mAdapter) {

            mAdapter = object :
                BaseRecyclerAdapter<MediaInfoData, ItemGridImageBinding>(ItemGridImageBinding::inflate) {
                override fun convert(
                    holder: BaseRecyclerViewHolder<ItemGridImageBinding>,
                    itemView: ItemGridImageBinding,
                    item: MediaInfoData,
                    position: Int
                ) {

                    itemView.apply {

                        // 1. 基础状态：加载 Load 图标
                        ivSendMessageLoad.visibility = if (isLoading) VISIBLE else GONE

                        // 2. 媒体类型判断：确定显示的 URL 和视频图标状态
                        val isVideo = item.mediaType != 0
                        ivVideo.visibility = if (isVideo) VISIBLE else GONE

                        // 决定加载的 URL：视频用封面，图片用原图
                        val loadUrl =
                            if (isVideo) (item.uri ?: item.coverUrl) else (item.uri ?: item.fileUrl)

                        // 3. 私密逻辑判断：处理模糊效果、倒计时和销毁状态
                        if (isPrivateMessage && (item.unlockTime == null || !isEnabledMedia(item.unlockTime!!))) {
                            // 情况 A：私密且未解锁（或已过期）-> 显示模糊图 + 销毁容器
                            stvImage.loadImage(loadUrl, customOption = blurImageRequestOptions)

                            // 如果有 unlockTime 但不可用，说明已过期，显示销毁容器
                            val isExpired =
                                item.unlockTime != null && !isEnabledMedia(item.unlockTime!!)
                            sllTime.visibility = GONE
                            llDestroyContainer.visibility = if (isExpired) VISIBLE else GONE
                        } else {
                            // 情况 B：非私密 或 已解锁 -> 显示清晰图 + 倒计时（如果是私密已解锁）
                            stvImage.loadImage(loadUrl)

                            if (isPrivateMessage && item.unlockTime != null) {
                                // 私密已解锁状态
                                sllTime.visibility = VISIBLE
                                tvTime.text = setTotalSecond(item.unlockTime!!)
                                llDestroyContainer.visibility = GONE
                            } else {
                                // 普通状态
                                sllTime.visibility = GONE
                                llDestroyContainer.visibility = GONE
                            }
                        }
                    }

                }

            }
        }

        mViewBinding.recyclerView.adapter = mAdapter



        mAdapter?.setOnDebouncedItemClick { _, _, position ->

            val data = mAdapter?.items

            data?.let {

                onItemClick?.invoke(it.toMutableList(), position)

//                showPictureDetailDialog(it.toMutableList(), position)
            }
        }

    }


    fun initRecyclerView(color: Int) {


        initAdapter()


//        mViewBinding.recyclerView.setRecycledViewPool(sharedPool)

        mViewBinding.recyclerView.itemAnimator = null
//
        mViewBinding.recyclerView.setItemViewCacheSize(9)

        mViewBinding.recyclerView.layoutManager = ImageLayoutView()

        mViewBinding.sclContainer.shapeDrawableBuilder.setSolidColor(context.getColor(color))
            .intoBackground()

    }





    fun setData(item: MessageData) {


        val list = if (item.msgMediaContent.isNullOrEmpty()){
            item.msgContent.formatListJson<MediaInfoData>()
        }else{
            item.msgMediaContent
        }?:emptyList()


        isLoading = item.id == null

        isPrivateMessage = item.isPrivate == true


        mAdapter?.submitList(list)

        if (item.isPrivate == true) {

            var totalPrice = 0

            list.forEach { item ->

                if (item.unlockTime == null){

                    val isVideo = item.mediaType != 0

                    totalPrice += if (isVideo){

                        videoPrice

                    }else{
                        picturePrice
                    }

                }

            }

            mViewBinding.sllCount.isVisible = totalPrice > 0

            mViewBinding.tvNum.text = "${totalPrice} Unlock all"

            initTime()

        } else {

            mViewBinding.sllCount.visibility = GONE

            timerJob?.cancel()
        }


    }


    private fun initTime() {
        timerJob?.cancel()

        val hasActiveTimer = mAdapter?.items?.any { it.unlockTime != null && isEnabledMedia(it.unlockTime) } == true
        // 每秒循环一次
        if (!hasActiveTimer) return

        timerJob = launch {
            while (isActive) {
                updateVisibleItems()
                delay(1000)
            }
        }
    }


    private fun updateVisibleItems() {
        val recyclerView = mViewBinding.recyclerView

        // 获取当前布局中可见的子 View 数量
        val childCount = recyclerView.childCount

        for (i in 0 until childCount) {
            val view = recyclerView.getChildAt(i) ?: continue
            val position = recyclerView.getChildAdapterPosition(view)
            if (position == RecyclerView.NO_POSITION) continue

            val item = mAdapter?.getItem(position) ?: continue

            // 只有倒计时未结束且有 unlockTime 的才刷新
            if (isEnabledMedia(item.unlockTime)) {
                val holder = recyclerView.getChildViewHolder(view) as? BaseRecyclerAdapter.BaseRecyclerViewHolder<ItemGridImageBinding>
                holder?.binding?.tvTime?.text = setTotalSecond(item.unlockTime!!)

                // 可选：如果时间到了，自动刷新这一项以隐藏倒计时
                if (setTotalSecond(item.unlockTime!!) == "00:00") {
                    mAdapter?.notifyItemChanged(position)
                }
            }
        }

        val hasActiveTimer = mAdapter?.items?.any { it.unlockTime != null && isEnabledMedia(it.unlockTime) } == true

        if (!hasActiveTimer){
            cancel()
        }
    }


    fun isEnabledMedia(unlockTime: Long?): Boolean {

        if (unlockTime == null) return false
        val passMs = System.currentTimeMillis() - unlockTime
        return passMs in 0..(AppConstant.Constant.totalExpiredSecond * 1000L)
    }

    fun setTotalSecond(unlockTime: Long): String {

        val passMs = System.currentTimeMillis() - unlockTime

        val passSecond = (passMs / 1000).toInt()

        val leftSecond = maxOf(AppConstant.Constant.totalExpiredSecond - passSecond, 0)

        return getTimeSecond(leftSecond)
    }




//    override fun onDetachedFromWindow() {
//        super.onDetachedFromWindow()
//        cancel() // 视图销毁时取消所有协程
//    }

}