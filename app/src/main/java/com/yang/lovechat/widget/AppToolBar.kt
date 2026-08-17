package com.yang.lovechat.widget

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import com.blankj.utilcode.util.ColorUtils
import com.yang.lovechat.R
import com.yang.lovechat.databinding.ViewToolbarBinding
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.edgeToEdgeTop

class AppToolBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    var mToolbarBinding: ViewToolbarBinding = ViewToolbarBinding.inflate(LayoutInflater.from(context), this, true)


    init {

        mToolbarBinding.root.edgeToEdgeTop()

        context.withStyledAttributes(attrs, R.styleable.AppToolBar){

            val mAppToolBarTitle = getString(R.styleable.AppToolBar_title)

            val mAppToolBarTitleColor = getResourceId(R.styleable.AppToolBar_titleColor, 0)

            val mAppToolBarBackImgVisible = getBoolean(R.styleable.AppToolBar_backImgVisible, true)

            val mAppToolBarEndImgVisible = getBoolean(R.styleable.AppToolBar_endImgVisible, false)

            val mAppToolBarBackImgSrc = getResourceId(R.styleable.AppToolBar_backImgSrc, 0)

            val mAppToolBarEndImgSrc = getResourceId(R.styleable.AppToolBar_endImgSrc, 0)

            val toolbarColor = getResourceId(R.styleable.AppToolBar_toolbarColor, 0)

            mToolbarBinding.tvTitle.text = mAppToolBarTitle

            mToolbarBinding.ivBack.visibility = if (mAppToolBarBackImgVisible) VISIBLE else GONE

            mToolbarBinding.ivRight.visibility = if (mAppToolBarEndImgVisible) VISIBLE else GONE

            if (mAppToolBarBackImgSrc != 0) {
                mToolbarBinding.ivBack.setImageResource(mAppToolBarBackImgSrc)
            }
            if (mAppToolBarEndImgSrc != 0) {
                mToolbarBinding.ivRight.setImageResource(mAppToolBarEndImgSrc)
            }

            if (toolbarColor != 0) {
                mToolbarBinding.clToolbar.setBackgroundResource(toolbarColor)
            }
            if (mAppToolBarTitleColor != 0) {
                mToolbarBinding.tvTitle.setTextColor(ColorUtils.getColor(mAppToolBarTitleColor))
            }

            mToolbarBinding.ivBack.clicks {
                if (context is Activity) {
                    context.finish()
                }
            }

        }

    }


}