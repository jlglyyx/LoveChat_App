package com.yang.lovechat.adapter


import com.yang.lovechat.R
import com.yang.lovechat.base.adapter.BaseRecyclerAdapter
import com.yang.lovechat.data.TagConfigData
import com.yang.lovechat.databinding.ItemBStepBinding

class WantAdapter :
    BaseRecyclerAdapter<TagConfigData, ItemBStepBinding>(ItemBStepBinding::inflate) {


    override fun convert(
        holder: BaseRecyclerViewHolder<ItemBStepBinding>,
        itemView: ItemBStepBinding,
        item: TagConfigData,
        position: Int
    ) {

        itemView.apply {

            tvTitle.text = item.tagName

//            if (item.isCheck) {
//                sllContainer.shapeDrawableBuilder
//                    .setSolidGradientColors(context.getColor(R.color.startColor),context.getColor(R.color.endColor))
//                    .setStrokeColor(context.getColor(R.color.color_4DC76BFF))
//                    .intoBackground()
//            } else {
//                sllContainer.shapeDrawableBuilder
//                    .setSolidGradientColors(context.getColor(R.color.color_222222),context.getColor(R.color.color_222222))
//                    .setStrokeColor(context.getColor(R.color.transparent))
//                    .intoBackground()
//            }



            if (item.isCheck) {
                sllContainer.shapeDrawableBuilder
                    .setSolidColor(context.getColor(R.color.color_4DFF5ACD))
                    .setStrokeColor(context.getColor(R.color.endColor))
                    .intoBackground()

            } else {
                sllContainer.shapeDrawableBuilder.setSolidColor(context.getColor(R.color.color_222222))
                    .setStrokeColor(context.getColor(R.color.transparent))
                    .intoBackground()

            }

        }


    }



}