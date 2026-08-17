package com.yang.lovechat.adapter

import androidx.core.view.isVisible
import com.yang.lovechat.R
import com.yang.lovechat.app.BaseApplication
import com.yang.lovechat.base.adapter.BaseRecyclerAdapter
import com.yang.lovechat.data.UserInfoData
import com.yang.lovechat.databinding.ItemViewedIBinding
import com.yang.lovechat.helper.UserInfoHold
import com.yang.lovechat.util.blurImageRequestOptions
import com.yang.lovechat.util.getScreenPx
import com.yang.lovechat.util.loadImage


class ViewedIAdapter : BaseRecyclerAdapter<UserInfoData, ItemViewedIBinding>(ItemViewedIBinding::inflate) {


    private val pictureWidth = getScreenPx(BaseApplication.mApplication)[0]/2

    private val pictureHeight = pictureWidth


    private var isVip = UserInfoHold.isVip


    override fun convert(
        holder: BaseRecyclerViewHolder<ItemViewedIBinding>,
        itemView: ItemViewedIBinding,
        item: UserInfoData,
        position: Int
    ) {

        itemView.apply {

            try {

                val url = if (!item.backgroundMediaList.isNullOrEmpty()){
                    item.backgroundMediaList.random().fileUrl
                }else{
                    R.drawable.iv_placeholder
                }

                if (isVip){

                    ivAvatar.loadImage(
                        url,
                        width = pictureWidth,
                        height = pictureHeight
                    )

                }else{
                    ivAvatar.loadImage(
                        url,
                        width = pictureWidth,
                        height = pictureHeight,
                        customOption = blurImageRequestOptions
                    )

                }

                stvAline.isVisible = item.isOnline

                tvName.text = "${item.userName},${item.age}"

                tvMessage.text = "has view ${item.viewedCount} times"


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }


    fun openAllImage(){

        if (isVip) return

        isVip = true

        notifyItemRangeChanged(0,itemCount,false)
    }


}