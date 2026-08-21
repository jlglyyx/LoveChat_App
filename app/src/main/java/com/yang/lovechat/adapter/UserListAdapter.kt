package com.yang.lovechat.adapter

import android.content.res.ColorStateList
import androidx.core.view.isVisible
import com.yang.lovechat.R
import com.yang.lovechat.base.adapter.BaseRecyclerAdapter
import com.yang.lovechat.data.ChatterUserData
import com.yang.lovechat.data.UserInfoData
import com.yang.lovechat.databinding.ItemUserListBinding
import com.yang.lovechat.util.loadImage

class UserListAdapter: BaseRecyclerAdapter<ChatterUserData, ItemUserListBinding>(ItemUserListBinding::inflate) {

    override fun convert(
        holder: BaseRecyclerViewHolder<ItemUserListBinding>,
        itemView: ItemUserListBinding,
        item: ChatterUserData,
        position: Int
    ) {

        itemView.apply {
            sivAvatar.loadImage(item.avatarUrl)
            tvName.text = item.userName
            stvMessageCount.text = "${item.totalUnreadCount}"
            stvMessageCount.isVisible = item.totalUnreadCount > 0
            tvId.text = "ID:${item.userId}"

            if (item.isCheck){

                sivAvatar.strokeColor = ColorStateList.valueOf(context.getColor(R.color.startColor))
            }else{
                sivAvatar.strokeColor = ColorStateList.valueOf(context.getColor(R.color.transparent))
            }

        }




    }
}