package com.yang.lovechat.adapter

import com.yang.lovechat.R
import com.yang.lovechat.app.BaseApplication
import com.yang.lovechat.base.adapter.BaseRecyclerAdapter
import com.yang.lovechat.data.UserInfoData
import com.yang.lovechat.databinding.ItemILikeBinding
import com.yang.lovechat.util.getScreenPx
import com.yang.lovechat.util.loadImage


class ILikeAdapter : BaseRecyclerAdapter<UserInfoData, ItemILikeBinding>(ItemILikeBinding::inflate) {

    private val pictureWidth = getScreenPx(BaseApplication.mApplication)[0]/2

    private val pictureHeight = pictureWidth


    override fun convert(
        holder: BaseRecyclerViewHolder<ItemILikeBinding>,
        itemView: ItemILikeBinding,
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

                ivImage.loadImage(
                    url,
                    width = pictureWidth,
                    height = pictureHeight
                )

                tvName.text = "${item.userName},${item.age}"

                tvLocation.text = "Nearby"

                tvOnline.text = if (item.isOnline) "Online" else "Active"

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }




}