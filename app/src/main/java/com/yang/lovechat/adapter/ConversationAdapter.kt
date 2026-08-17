package com.yang.lovechat.adapter

import android.view.View
import androidx.core.view.isVisible
import com.yang.lovechat.R
import com.yang.lovechat.base.adapter.BaseRecyclerAdapter
import com.yang.lovechat.data.ConversationData
import com.yang.lovechat.data.InstructionType
import com.yang.lovechat.data.MessageType
import com.yang.lovechat.databinding.ItemConversationBinding
import com.yang.lovechat.util.getMessageTime
import com.yang.lovechat.util.loadImage
import java.util.Date
import kotlin.apply
import kotlin.let

class ConversationAdapter: BaseRecyclerAdapter<ConversationData, ItemConversationBinding>(ItemConversationBinding::inflate) {

    private val matchText = "Perfect Match!"



    override fun convert(
        holder: BaseRecyclerViewHolder<ItemConversationBinding>,
        itemView: ItemConversationBinding,
        item: ConversationData,
        position: Int
    ) {

        itemView.apply {

            item.let {

                ivAvatar.loadImage(it.friendAvatar?: R.drawable.iv_avatar)

                tvName.text = it.friendName

                tvMessage.setTextColor(context.getColor(R.color.color_999999))

                stvAline.isVisible = it.isOnline

                if (it.unreadCount > 0){

                    stvMessageCount.text = "${it.unreadCount}"

                    stvMessageRed.visibility = View.VISIBLE

                    stvMessageCount.visibility = View.VISIBLE

                }else{

                    stvMessageRed.visibility = View.GONE

                    stvMessageCount.visibility = View.GONE
                }


                ivTopTag.visibility = if (it.isTop) View.VISIBLE else View.GONE

                clMessage.setBackgroundResource(if (it.isTop) R.drawable.ripple_primary_top else R.drawable.ripple_primary)

                tvTop.text = if (it.isTop) "Cancel" else "Top"

                tvUpdateTime.text = getMessageTime(Date(it.lastMsgTime))



                when(it.lastMsgType){

                    MessageType.TEXT.type ->{
                        tvMessage.text = it.lastMsgContent
                    }
                    MessageType.IMAGE_VIDEO.type ->{
                        tvMessage.text = "[Photo/Video]"
                    }

                    MessageType.SYSTEM.type ->{
                        tvMessage.text = it.lastMsgContent
                    }
                    else -> {

                        tvMessage.text = it.lastMsgContent
                    }
                }


                if (it.lastMsgType == MessageType.SYSTEM.type){

                    if (it.lastMsgContent == InstructionType.MATCH.value || it.lastMsgContent == InstructionType.FAST_CONNECT.value){

                        tvMessage.text = matchText

                        tvMessage.setTextColor(context.getColor(R.color.color_FF5ACD))

                        stvMessageCount.visibility = View.GONE


                    }else{

                        stvMessageRed.visibility = View.GONE
                    }

                }else{

                    stvMessageRed.visibility = View.GONE
                }

            }

        }
    }
}
