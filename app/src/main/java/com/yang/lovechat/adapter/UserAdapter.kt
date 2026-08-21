package com.yang.lovechat.adapter

import androidx.recyclerview.widget.RecyclerView
import com.yang.lovechat.base.adapter.BaseRecyclerAdapter
import com.yang.lovechat.data.UserInfoData
import com.yang.lovechat.databinding.ItemLikeIBinding

class UserAdapter :
    BaseRecyclerAdapter<UserInfoData, ItemLikeIBinding>(ItemLikeIBinding::inflate) {

    var sharedPool: RecyclerView.RecycledViewPool = RecyclerView.RecycledViewPool()


    override fun onInitViewHolder(holder: BaseRecyclerViewHolder<ItemLikeIBinding>) {
        super.onInitViewHolder(holder)

    }


    override fun convert(
        holder: BaseRecyclerViewHolder<ItemLikeIBinding>,
        itemView: ItemLikeIBinding,
        item: UserInfoData,
        position: Int
    ) {

    }



}