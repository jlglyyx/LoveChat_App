package com.yang.lovechat.adapter


import com.yang.lovechat.R
import com.yang.lovechat.base.adapter.BaseRecyclerAdapter
import com.yang.lovechat.data.ConnectMessageData
import com.yang.lovechat.databinding.ItemConnectMessageBinding
import com.yang.lovechat.util.loadImage

class ConnectMessageAdapter :
    BaseRecyclerAdapter<ConnectMessageData, ItemConnectMessageBinding>(ItemConnectMessageBinding::inflate) {


    override fun convert(
        holder: BaseRecyclerViewHolder<ItemConnectMessageBinding>,
        itemView: ItemConnectMessageBinding,
        item: ConnectMessageData,
        position: Int
    ) {

        itemView.apply {

            holder.itemView.tag = holder.bindingAdapterPosition

            stvText.text = item.message

            sivAvatar.loadImage(item.avatarUrl?:R.drawable.iv_avatar)

        }
    }



}