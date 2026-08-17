package com.yang.lovechat.adapter


import com.yang.lovechat.R
import com.yang.lovechat.base.adapter.BaseRecyclerAdapter
import com.yang.lovechat.data.ProductInfoData
import com.yang.lovechat.databinding.ItemProductDiamondBinding
import com.yang.lovechat.util.formatPrice

class ProductDiamondAdapter :
    BaseRecyclerAdapter<ProductInfoData, ItemProductDiamondBinding>(ItemProductDiamondBinding::inflate) {


    override fun convert(
        holder: BaseRecyclerViewHolder<ItemProductDiamondBinding>,
        itemView: ItemProductDiamondBinding,
        item: ProductInfoData,
        position: Int
    ) {

        itemView.apply {

            item.let {

                tvName.text = "${item.diamondCount}"

                tvPrice.text = "$${item.priceAmount}"

                tvContent.text = "$${(item.priceAmount/item.diamondCount).formatPrice()}/diamond"

                tvSave.text = if (item.save.isNullOrEmpty()) "Best Offer" else "Save ${item.save}%"

            if (item.isSelect) {
                sllContainer.shapeDrawableBuilder
                    .setSolidColor(context.getColor(R.color.color_4DFF5ACD))
                    .setStrokeColor(context.getColor(R.color.endColor))
                    .intoBackground()
                tvSave.shapeDrawableBuilder.setSolidColor(context.getColor(R.color.endColor))
                    .intoBackground()
                tvSave.setTextColor(context.getColor(R.color.white))
            } else {
                sllContainer.shapeDrawableBuilder.setSolidColor(context.getColor(R.color.color_222222))
                    .setStrokeColor(context.getColor(R.color.transparent))
                    .intoBackground()
                tvSave.shapeDrawableBuilder.setSolidColor(context.getColor(R.color.color_4DFF5ACD))
                    .intoBackground()
                tvSave.setTextColor(context.getColor(R.color.white))
            }

        }
        }
    }

}