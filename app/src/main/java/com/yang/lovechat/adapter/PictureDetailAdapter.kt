package com.yang.lovechat.adapter

import android.view.View
import com.yang.lovechat.base.adapter.BaseRecyclerAdapter
import com.yang.lovechat.data.MediaEnumType
import com.yang.lovechat.data.PictureData
import com.yang.lovechat.databinding.ItemPictureDetailBinding
import com.yang.lovechat.util.loadImage
import com.yang.lovechat.util.screenPxHeight
import com.yang.lovechat.util.screenPxWidth

class PictureDetailAdapter : BaseRecyclerAdapter<PictureData, ItemPictureDetailBinding>(ItemPictureDetailBinding::inflate) {

    override fun convert(
        holder: BaseRecyclerViewHolder<ItemPictureDetailBinding>,
        itemView: ItemPictureDetailBinding,
        item: PictureData,
        position: Int
    ) {

        itemView.apply {

            when(item.mediaEnumType){

                MediaEnumType.LOCAL_IMAGE-> {
                    pvVideo.visibility = View.GONE
                    ivImage.visibility = View.VISIBLE
                    ivImage.loadImage(item.uri, width = screenPxWidth, height = screenPxHeight)
                }

                MediaEnumType.LOCAL_VIDEO -> {
                    pvVideo.visibility = View.VISIBLE
                    ivImage.visibility = View.GONE
                    pvVideo.setVideoData(item.uri!!)
                }
                MediaEnumType.VIDEO -> {
                    pvVideo.visibility = View.VISIBLE
                    ivImage.visibility = View.GONE
                    pvVideo.setVideoData(item.url!!,item.coverUrl)
                }
                else -> {
                    pvVideo.visibility = View.GONE
                    ivImage.visibility = View.VISIBLE
                    ivImage.loadImage(item.url, width = screenPxWidth, height = screenPxHeight)
                }
            }

        }

    }


}