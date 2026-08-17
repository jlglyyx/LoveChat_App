package com.yang.lovechat.adapter


import com.yang.lovechat.R
import com.yang.lovechat.base.adapter.BaseRecyclerAdapter
import com.yang.lovechat.data.TagConfigData
import com.yang.lovechat.databinding.ItemIntersetBinding

class InterestAdapter :
    BaseRecyclerAdapter<TagConfigData, ItemIntersetBinding>(ItemIntersetBinding::inflate) {


    override fun convert(
        holder: BaseRecyclerViewHolder<ItemIntersetBinding>,
        itemView: ItemIntersetBinding,
        item: TagConfigData,
        position: Int
    ) {

        itemView.apply {

            tvTitle.text = item.tagName

            if (item.isCheck) {
                sllContainer.shapeDrawableBuilder
                    .setSolidGradientColors(context.getColor(R.color.startColor),context.getColor(R.color.endColor))
                    .setStrokeColor(context.getColor(R.color.color_4DC76BFF))
                    .intoBackground()
            } else {
                sllContainer.shapeDrawableBuilder
                    .setSolidGradientColors(context.getColor(R.color.color_222222),context.getColor(R.color.color_222222))
                    .setStrokeColor(context.getColor(R.color.transparent))
                    .intoBackground()
            }
        }
    }



}