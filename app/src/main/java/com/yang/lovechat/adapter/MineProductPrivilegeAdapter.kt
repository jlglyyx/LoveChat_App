package com.yang.lovechat.adapter


import android.view.View
import com.yang.lovechat.base.adapter.BaseRecyclerAdapter
import com.yang.lovechat.data.ProductPrivilegeData
import com.yang.lovechat.databinding.ItemMineProductPrivilegeBinding
import com.yang.lovechat.util.loadImage

class MineProductPrivilegeAdapter :
    BaseRecyclerAdapter<ProductPrivilegeData, ItemMineProductPrivilegeBinding>(ItemMineProductPrivilegeBinding::inflate) {


    override fun convert(
        holder: BaseRecyclerViewHolder<ItemMineProductPrivilegeBinding>,
        itemView: ItemMineProductPrivilegeBinding,
        item: ProductPrivilegeData,
        position: Int
    ) {

        itemView.apply {

            item.let {

                if (it.iconUrl.isNullOrEmpty()){
                    ivImage.visibility = View.GONE
                }else{
                    ivImage.visibility = View.VISIBLE

                    ivImage.loadImage(it.iconUrl)
                }

                tvTitle.text = "${it.title}"

                tvContent.text = "${it.description}"

            }


        }


    }

}