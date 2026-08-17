package com.yang.lovechat.adapter

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ColorUtils.getColor
import com.blankj.utilcode.util.Utils
import com.blankj.utilcode.util.VibrateUtils
import com.yang.lovechat.R
import com.yang.lovechat.base.adapter.BaseRecyclerAdapter
import com.yang.lovechat.data.MediaInfoData
import com.yang.lovechat.data.UserInfoData
import com.yang.lovechat.databinding.ItemCardBinding
import com.yang.lovechat.databinding.ItemTagBinding
import com.yang.lovechat.util.dip2px
import com.yang.lovechat.util.mTranslation
import kotlin.apply
import kotlin.collections.forEach
import kotlin.collections.take
import kotlin.ranges.coerceAtLeast
import kotlin.ranges.coerceAtMost
import kotlin.text.isNullOrEmpty

class CardAdapter :
    BaseRecyclerAdapter<UserInfoData, ItemCardBinding>(ItemCardBinding::inflate) {


    var sharedPool: RecyclerView.RecycledViewPool = RecyclerView.RecycledViewPool()

    private var mObjectAnimator: ObjectAnimator? = null

    private val mTranslationDuration = 300L

    private val mTranslationX = 10f

    private val mVibrateDuration = 20L

    private val mIndicatorHeight = 4f.dip2px(Utils.getApp())

    private val mBannerRound = 20f.dip2px(Utils.getApp()).toFloat()


    override fun onInitViewHolder(holder: BaseRecyclerViewHolder<ItemCardBinding>) {
        super.onInitViewHolder(holder)

        changePicture(holder)

    }


    override fun convert(
        holder: BaseRecyclerViewHolder<ItemCardBinding>,
        itemView: ItemCardBinding,
        item: UserInfoData,
        position: Int
    ) {
        holder.itemView.rotation = 0f
        holder.itemView.translationX = 0f

        itemView.apply {

            tvName.text = "${item.userName},${item.age}"

            if (item.bio.isNullOrEmpty()) {
                tvBio.visibility = View.GONE
            } else {
                tvBio.text = "${item.bio}"
                tvBio.visibility = View.VISIBLE
            }

            tvOnline.text = if (item.isOnline) "Online" else "Active"


            flTag.removeAllViews()

            val userTags = item.interest?:emptyList()


            ItemTagBinding.inflate(LayoutInflater.from(context), flTag, true)
                .apply {
                    stvTag.shapeDrawableBuilder.setStrokeGradientColors(context.getColor(R.color.color_FF5ACD),context.getColor(R.color.color_C76BFF)).intoBackground()
//                    stvTag.shapeDrawableBuilder.setSolidColor(context.getColor(R.color.color_FF5ACD)).intoBackground()
                    stvTag.text = "Hoping to find : ${item.intent}"
                }

            userTags.take(5).forEach {
                ItemTagBinding.inflate(LayoutInflater.from(context), flTag, true)
                    .apply {
                        stvTag.text = it.tagName
                    }

            }

            initBanner(holder, item.backgroundMediaList)

        }
    }


    private fun changePicture(holder: BaseRecyclerViewHolder<ItemCardBinding>) {

        holder.binding.apply {


            viewLeft.setOnClickListener {

                try {

                    val currentItem = banner.currentItem

                    if (currentItem == 0) {

                        mObjectAnimator = null

                        mObjectAnimator =
                            clContainer.mTranslation(
                                View.TRANSLATION_X,
                                0f,
                                -mTranslationX,
                                mTranslationX,
                                0f
                            )

                        mObjectAnimator?.apply {

                            setDuration(mTranslationDuration)

                            start()
                        }

                        VibrateUtils.vibrate(mVibrateDuration)

                        return@setOnClickListener
                    }

                    banner.currentItem = (currentItem - 1).coerceAtLeast(0)


                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }


            viewRight.setOnClickListener {

                try {

                    val currentItem = banner.currentItem

                    if (currentItem == banner.itemCount - 1) {

                        mObjectAnimator = null

                        mObjectAnimator =
                            clContainer.mTranslation(
                                View.TRANSLATION_X,
                                0f,
                                -mTranslationX,
                                mTranslationX,
                                0f
                            )

                        mObjectAnimator?.apply {

                            setDuration(mTranslationDuration)

                            start()
                        }

                        VibrateUtils.vibrate(mVibrateDuration)

                        return@setOnClickListener
                    }

                    banner.currentItem = (currentItem + 1).coerceAtMost(
                        banner.itemCount - 1
                    )


                } catch (e: Exception) {

                    e.printStackTrace()
                }


            }
        }

    }


    private fun initBanner(
        holder: BaseRecyclerViewHolder<ItemCardBinding>,
        list: List<MediaInfoData>?
    ) {

        holder.binding.apply {


            val mIndicatorWidth = mIndicatorHeight * 3

            val mCardImageAdapter = CardImageAdapter(mutableListOf())

            banner.setUserInputEnabled(false)
                .setAdapter(mCardImageAdapter, false)
                .isAutoLoop(false)
                .setBannerRound(mBannerRound)
                .setIndicator(mIndicator, false)
                .setIndicatorSelectedColor(getColor(R.color.white))
                .setIndicatorNormalColor(getColor(R.color.white_30))
                .setIndicatorHeight(mIndicatorHeight)
                .setIndicatorWidth(mIndicatorWidth, 2 * mIndicatorWidth)
            mCardImageAdapter.setDatas(list)

        }
    }


}