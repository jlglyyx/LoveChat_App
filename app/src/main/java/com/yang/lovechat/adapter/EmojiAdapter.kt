package com.yang.lovechat.adapter

import android.text.Html
import com.yang.lovechat.base.adapter.BaseRecyclerAdapter
import com.yang.lovechat.databinding.ItemEmojiBinding

class EmojiAdapter : BaseRecyclerAdapter<String, ItemEmojiBinding>(ItemEmojiBinding::inflate) {

    override fun convert(
        holder: BaseRecyclerViewHolder<ItemEmojiBinding>,
        itemView: ItemEmojiBinding,
        item: String,
        position: Int
    ) {

        itemView.apply {

            item.let {

                itemView.tvEmoji.text = Html.fromHtml(item, Html.FROM_HTML_MODE_LEGACY)

            }

        }

    }
}