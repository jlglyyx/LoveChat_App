package com.yang.lovechat.adapter

import android.view.View
import com.yang.lovechat.R
import com.yang.lovechat.app.BaseApplication
import com.yang.lovechat.base.adapter.BaseRecyclerAdapter
import com.yang.lovechat.data.UserInfoData
import com.yang.lovechat.databinding.ItemLikeIBinding
import com.yang.lovechat.helper.UserInfoHold
import com.yang.lovechat.util.blurImageRequestOptions
import com.yang.lovechat.util.getScreenPx
import com.yang.lovechat.util.loadImage
import com.yang.lovechat.util.viewVisibility


class LikeIAdapter : BaseRecyclerAdapter<UserInfoData, ItemLikeIBinding>(ItemLikeIBinding::inflate) {


    private val pictureWidth = getScreenPx(BaseApplication.mApplication)[0]/2

    private val pictureHeight = pictureWidth


    private var isVip = UserInfoHold.isVip


    override fun convert(
        holder: BaseRecyclerViewHolder<ItemLikeIBinding>,
        itemView: ItemLikeIBinding,
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

                    ivImage.loadImage(
                        url,
                        width = pictureWidth,
                        height = pictureHeight
                    )

                    viewVisibility(View.GONE,ivUnlock,tv0,ivCrown)

                }else{
                    ivImage.loadImage(
                        url,
                        width = pictureWidth,
                        height = pictureHeight,
                        customOption = blurImageRequestOptions
                    )
                    viewVisibility(View.VISIBLE,ivUnlock,tv0,ivCrown)
                }

                tvName.text = "${item.userName},${item.age}"

                tvLocation.text = "Nearby"

                tvOnline.text = if (item.isOnline) "Online" else "Active"


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