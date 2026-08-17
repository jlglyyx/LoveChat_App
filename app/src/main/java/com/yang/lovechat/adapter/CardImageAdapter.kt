package com.yang.lovechat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.Utils
import com.yang.lovechat.R
import com.yang.lovechat.data.MediaInfoData
import com.yang.lovechat.databinding.ItemCardImageBinding
import com.yang.lovechat.util.clicks

import com.yang.lovechat.util.getScreenPx
import com.yang.lovechat.util.loadImage
import com.youth.banner.adapter.BannerAdapter
import kotlin.let


class CardImageAdapter(list: List<MediaInfoData>) :
    BannerAdapter<MediaInfoData, RecyclerView.ViewHolder>(list) {

    val screenPx = getScreenPx(Utils.getApp())

    private val mWidth: Int = screenPx[0] * 4 / 5

    private val mHeight: Int = screenPx[1] * 4 / 5

    var onItemClick :((Int) ->Unit)? = null



    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {

        val inflate =
            ItemCardImageBinding.inflate(LayoutInflater.from(parent?.context), parent, false)

        inflate.root.setOnClickListener {


        }

        return object : RecyclerView.ViewHolder(inflate.root) {


        }

    }

    override fun onBindView(
        holder: RecyclerView.ViewHolder?,
        data: MediaInfoData?,
        position: Int,
        size: Int
    ) {

        holder?.itemView?.let {

            val mItemCardImageBinding = ItemCardImageBinding.bind(it)

            mItemCardImageBinding.ivImage.loadImage(data?.fileUrl?:R.drawable.iv_placeholder, width = mWidth, height = mHeight)

            mItemCardImageBinding.root.clicks {

                onItemClick?.invoke(position)

            }
        }

    }


}


