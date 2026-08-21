package com.yang.lovechat.adapter

import android.view.View
import com.yang.lovechat.R
import com.yang.lovechat.base.adapter.BaseMultiItemAdapter
import com.yang.lovechat.data.MediaInfoData
import com.yang.lovechat.data.MessageData
import com.yang.lovechat.data.MessageType
import com.yang.lovechat.databinding.ItemMessageMatchBinding
import com.yang.lovechat.databinding.ItemMessageMediaEndBinding
import com.yang.lovechat.databinding.ItemMessageMediaStartBinding
import com.yang.lovechat.databinding.ItemMessageTextEndBinding
import com.yang.lovechat.databinding.ItemMessageTextStartBinding
import com.yang.lovechat.databinding.ItemMessageTimeBinding
import com.yang.lovechat.helper.UserInfoHold
import com.yang.lovechat.util.dateFormat
import com.yang.lovechat.util.getMessageTime
import com.yang.lovechat.util.loadImage
import com.yang.lovechat.util.viewVisibility
import java.util.Date
import kotlin.math.abs

class MessageAdapter() : BaseMultiItemAdapter<MessageData>() {


    private var userId: Long? = UserInfoHold.currentUserId
    private val maxIntervalTime = 6 * 60 * 1000


    var onGridItemClick: ((MutableList<MediaInfoData>, Int, Int) -> Unit)? = null
    companion object {
        const val ITEM_MESSAGE_MATCH = 0

        const val ITEM_MESSAGE_TEXT_START = 1
        const val ITEM_MESSAGE_TEXT_END = 2


        const val ITEM_MESSAGE_MEDIA_START = 3

        const val ITEM_MESSAGE_MEDIA_END = 4


    }


    init {


        addItemType(
            ITEM_MESSAGE_MATCH,
            object : BaseMultiItemViewHolder<MessageData, ItemMessageMatchBinding>(
                ItemMessageMatchBinding::inflate
            ) {
                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemMessageMatchBinding>,
                    position: Int,
                    item: MessageData?
                ) {
                    try {
                        item?.let {

                            holder.viewBinding.apply {

                                ivUser.loadImage(it.userAvatar)

                                ivFriend.loadImage(it.friendAvatar)

                                tvMessage.text =
                                    "Matched on ${Date(it.createTime?:0L).dateFormat("MMM d,yyyy")}"
                            }


                        }
                    } catch (e: Exception) {
                        e.printStackTrace()

                    }
                }

            })

        addItemType(
            ITEM_MESSAGE_TEXT_START,
            object : BaseMultiItemViewHolder<MessageData, ItemMessageTextStartBinding>(
                ItemMessageTextStartBinding::inflate
            ) {
                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemMessageTextStartBinding>,
                    position: Int,
                    item: MessageData?
                ) {
                    try {
                        item?.let {

                            holder.viewBinding.apply {

                                tvMessage.text = it.msgContent

                                showMessageTime(it,position,llTime)
                            }


                        }
                    } catch (e: Exception) {
                        e.printStackTrace()

                    }
                }

            })

        addItemType(
            ITEM_MESSAGE_TEXT_END,
            object : BaseMultiItemViewHolder<MessageData, ItemMessageTextEndBinding>(
                ItemMessageTextEndBinding::inflate
            ) {
                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemMessageTextEndBinding>,
                    position: Int,
                    item: MessageData?
                ) {
                    try {
                        item?.let {

                            holder.viewBinding.apply {



                                if(it.id == null){

                                    ivSendMessageLoad.visibility = View.VISIBLE

                                }else{

                                    ivSendMessageLoad.visibility = View.GONE
                                }


                                if(it.errorDesc.isNullOrEmpty()){


                                    viewVisibility(View.GONE,ivSendTextMessageError,tvErrorReason)

                                }else{

                                    tvErrorReason.text = it.errorDesc

                                    ivSendMessageLoad.visibility = View.GONE

                                    viewVisibility(View.VISIBLE,ivSendTextMessageError,tvErrorReason)
                                }



                                tvMessage.text = it.msgContent


                                showMessageTime(it,position,llTime)
                            }


                        }
                    } catch (e: Exception) {
                        e.printStackTrace()

                    }
                }

            })


        addItemType(
            ITEM_MESSAGE_MEDIA_START,
            object : BaseMultiItemViewHolder<MessageData, ItemMessageMediaStartBinding>(
                ItemMessageMediaStartBinding::inflate
            ) {


                override fun onInitViewHolder(holder: BaseRecyclerViewHolder<ItemMessageMediaStartBinding>) {
                    super.onInitViewHolder(holder)

                    holder.viewBinding.gridImageView.initRecyclerView(R.color.endColor)

                    holder.viewBinding.gridImageView.onItemClick = { data, position ->

                        onGridItemClick?.invoke(data,position,holder.bindingAdapterPosition)
                    }
                }

                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemMessageMediaStartBinding>,
                    position: Int,
                    item: MessageData?
                ) {
                    try {
                        item?.let {

                            holder.viewBinding.apply {


                                gridImageView.setData(it)


                                showMessageTime(it,position,llTime)
                            }


                        }
                    } catch (e: Exception) {
                        e.printStackTrace()

                    }
                }

            })

        addItemType(
            ITEM_MESSAGE_MEDIA_END,
            object : BaseMultiItemViewHolder<MessageData, ItemMessageMediaEndBinding>(
                ItemMessageMediaEndBinding::inflate
            ) {

                override fun onInitViewHolder(holder: BaseRecyclerViewHolder<ItemMessageMediaEndBinding>) {
                    super.onInitViewHolder(holder)

                    holder.viewBinding.gridImageView.initRecyclerView(R.color.white)

                    holder.viewBinding.gridImageView.onItemClick = { data, position ->

                        onGridItemClick?.invoke(data,position,holder.bindingAdapterPosition)
                    }

                }

                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemMessageMediaEndBinding>,
                    position: Int,
                    item: MessageData?
                ) {
                    try {
                        item?.let {

                            holder.viewBinding.apply {


                                gridImageView.setData(it)


                                showMessageTime(it,position,llTime)
                            }


                        }
                    } catch (e: Exception) {
                        e.printStackTrace()

                    }
                }

            })




        onItemViewType { position, list ->

            val data = list[position]

            //'消息类型: 0-文字, 1-图片, 2-视频,3-系统消息',

            when (data.msgType) {

                MessageType.TEXT.type -> {

                    if (data.userId == userId) {
                        ITEM_MESSAGE_TEXT_END
                    } else {
                        ITEM_MESSAGE_TEXT_START
                    }

                }

                MessageType.IMAGE_VIDEO.type -> {

                    if (data.userId == userId) {
                        ITEM_MESSAGE_MEDIA_END
                    } else {
                        ITEM_MESSAGE_MEDIA_START
                    }
                }


                3 -> {

                    ITEM_MESSAGE_MATCH
                }

                else -> {

                    ITEM_MESSAGE_MATCH
                }

            }

        }


    }





    private fun showMessageTime(
        message: MessageData?,
        position: Int,
        mBinding: ItemMessageTimeBinding
    ) {

        if (null == message) {

            return
        }

        val currentTime = Date(message.createTime?:0L)

        val formattedTime = getMessageTime(currentTime)

        if (position == 0) {
            mBinding.root.visibility = View.VISIBLE
        } else {
            val previousItem = getItem(position - 1)
            previousItem.let {
                val previousTime = Date(previousItem.createTime?:0L)

                val diffInMinutes = (currentTime.time - previousTime.time)

                if (abs(diffInMinutes) >= maxIntervalTime) {
                    mBinding.root.visibility = View.VISIBLE
                } else {
                    mBinding.root.visibility = View.GONE
                }
            }
        }

        mBinding.tvMessageTime.text = formattedTime

    }

}