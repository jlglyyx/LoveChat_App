package com.yang.lovechat.adapter


import com.yang.lovechat.R
import com.yang.lovechat.base.adapter.BaseRecyclerAdapter
import com.yang.lovechat.data.MessageData
import com.yang.lovechat.databinding.ItemSystemMessageBinding
import com.yang.lovechat.util.highlightContacts

class SystemMessageAdapter :
    BaseRecyclerAdapter<MessageData, ItemSystemMessageBinding>(ItemSystemMessageBinding::inflate) {

    override fun convert(
        holder: BaseRecyclerViewHolder<ItemSystemMessageBinding>,
        itemView: ItemSystemMessageBinding,
        item: MessageData,
        position: Int
    ) {

        try {

            highlightContacts(itemView.tvMessage,item.msgContent, R.color.endColor)

        }catch (e: Exception){
            e.printStackTrace()
        }


    }
}




