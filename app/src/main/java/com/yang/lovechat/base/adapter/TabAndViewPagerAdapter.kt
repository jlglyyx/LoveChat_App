package com.yang.lovechat.base.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.imageview.ShapeableImageView
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.loadImage


/**
 * @ClassName TabAndViewPagerAdapter
 *
 * @Description
 *
 * @Author 1
 *
 * @Date 2020/12/1 14:52
 */
class TabAndViewPagerAdapter(fragmentActivity: FragmentActivity, private val fragments: MutableList<Fragment>) : FragmentStateAdapter(fragmentActivity) {


    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }


}

class TabAndViewPagerFragmentAdapter(fragment: Fragment, private val fragments: MutableList<Fragment>) : FragmentStateAdapter(fragment) {


    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}

class ImageViewPagerAdapter(var data: MutableList<String>) : RecyclerView.Adapter<ImageViewPagerAdapter.ImageViewPagerViewHolder>() {

    var clickListener:ClickListener? = null

    interface ClickListener{
        fun onClickListener(view:View,position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewPagerAdapter.ImageViewPagerViewHolder {
        val shapeAbleImageView = ShapeableImageView(parent.context)
        shapeAbleImageView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)
        //shapeAbleImageView.scaleType = ImageView.ScaleType.FIT_CENTER
        shapeAbleImageView.setBackgroundColor(Color.BLACK)
        return ImageViewPagerViewHolder(shapeAbleImageView)
    }

    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: ImageViewPagerAdapter.ImageViewPagerViewHolder, position: Int) {
        holder.shapeAbleImageView.clicks {
            clickListener?.onClickListener(holder.shapeAbleImageView,position)
        }
        holder.shapeAbleImageView.loadImage(data[position])
    }

    override fun getItemCount(): Int {

        return data.size
    }





    inner class ImageViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var shapeAbleImageView: ShapeableImageView = itemView as ShapeableImageView

    }
}