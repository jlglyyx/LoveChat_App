package com.yang.lovechat.adapter

import androidx.lifecycle.Lifecycle
import com.yang.lovechat.R
import com.yang.lovechat.app.BaseApplication
import com.yang.lovechat.base.adapter.BaseRecyclerAdapter
import com.yang.lovechat.data.ConversationData
import com.yang.lovechat.databinding.ItemTopConversationBinding
import com.yang.lovechat.util.dip2px
import com.yang.lovechat.widget.CarouselImageView

class TopConversationAdapter(val lifecycle: Lifecycle) :
    BaseRecyclerAdapter<ConversationData, ItemTopConversationBinding>(ItemTopConversationBinding::inflate) {

    private val avatarWidth = 40f.dip2px(BaseApplication.mApplication)


    override fun convert(
        holder: BaseRecyclerViewHolder<ItemTopConversationBinding>,
        itemView: ItemTopConversationBinding,
        item: ConversationData,
        position: Int
    ) {
        try {

            lifecycle.removeObserver(itemView.mCarouselImageView)

            lifecycle.addObserver( itemView.mCarouselImageView)
//
//            itemView.mCarouselImageView.setImageList(item.list.map { it.headPic }
//                .toMutableList())
//
//            if (item.name == "Likes") {
//                itemView.mCarouselImageView.setStrokeColor(R.color.color_C678FF)
//            } else {
//                itemView.mCarouselImageView.setStrokeColor(R.color.color_FF78C1)
//            }
//
//            itemView.tvName.text = item.name
//
//            if (item.count == 0) {
//                itemView.stvMessageCount.visibility = View.GONE
//            } else {
//                itemView.stvMessageCount.text = "${item.count}"
//                itemView.stvMessageCount.visibility = View.VISIBLE
//            }
//            if (item.addCount == 0) {
//                itemView.tvAddCount.visibility = View.GONE
//            } else {
//                itemView.tvAddCount.text = "${item.addCount}"
//                itemView.tvAddCount.visibility = View.VISIBLE
//            }
//            itemView.stvNew.visibility = View.GONE


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }



    fun stopCarousel(){
        try {
            items.forEachIndexed { index, data ->
                val layoutManager = recyclerView.layoutManager?:return
                val findViewByPosition = layoutManager.findViewByPosition(index)?:return
                val mCarouselImageView = findViewByPosition.findViewById<CarouselImageView>(R.id.mCarouselImageView)?:return
                mCarouselImageView.release()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }

    }
}