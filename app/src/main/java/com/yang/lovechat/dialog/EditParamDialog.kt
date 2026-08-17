package com.yang.lovechat.dialog

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import com.bigkoo.pickerview.adapter.ArrayWheelAdapter
import com.yang.lovechat.R
import com.yang.lovechat.base.dialog.BaseDialog
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.TagConfigData
import com.yang.lovechat.databinding.DialogEditParamBinding
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.edgeToEdgeBottom
import com.yang.lovechat.util.formatListJson
import com.yang.lovechat.util.getCache

class EditParamDialog : BaseDialog<DialogEditParamBinding>(DialogEditParamBinding::inflate) {

    var onConfirm: ((String, TagConfigData?) -> Unit)? = null


    private var list = mutableListOf<String>()

    private var keyList = mutableListOf<TagConfigData>()

    private lateinit var mAdapter: ArrayWheelAdapter<String>


    private var mCurrentItem = ""

    private var mStartItem = ""

    private var mCurrentIndex = 0


    companion object {
        fun newInstance(data: String, type: Int): EditParamDialog {
            return EditParamDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.DATA, data)
                    putInt(AppConstant.Constant.TYPE, type)
                }
            }
        }
    }


    override fun initView() {


        withViewBinding {

            root.edgeToEdgeBottom()

            tvClose.clicks {

                dismissAllowingStateLoss()
            }

            tvSave.clicks {


                mCurrentItem = mAdapter.getItem(mCurrentIndex).toString()


                val data = if(keyList.isEmpty()){
                   null
                }else{
                    keyList[mCurrentIndex]
                }

                onConfirm?.invoke(mCurrentItem,data)

                dismissAllowingStateLoss()

            }

        }




    }

    override fun initData() {

        arguments?.let {

            val type = it.getInt(AppConstant.Constant.TYPE, -1)

            mCurrentItem = it.getString(AppConstant.Constant.DATA, mCurrentItem)

            mStartItem = mCurrentItem

            list = createData(type)

            val startIndex = list.indexOfFirst { it == mStartItem }

            mAdapter = ArrayWheelAdapter(list)

            mDialogBinding.wheelItem.apply {
                adapter = mAdapter
                setCyclic(false)
                cameraDistance = 20f
                setDividerColor(Color.TRANSPARENT)
                setTextColorCenter(requireContext().getColor(R.color.white))
                setTextColorOut(requireContext().getColor(R.color.color_999999))
                currentItem = list.indexOfFirst { it == mCurrentItem }
                setOnItemSelectedListener { index ->

                    try {
                        mCurrentIndex = index

                        mDialogBinding.tvSave.isEnabled = startIndex != mCurrentIndex
                    }catch (e: Exception){
                        e.printStackTrace()
                    }

                }
            }

        }


    }


    private fun createData(type: Int): MutableList<String> {


        //0 体重 1 身高 2职业 3 i want
        return when (type) {
            0 -> {

                mDialogBinding.tvTitle.text = "Weight"

                (40..120).map { "${it}kg" }.toMutableList()

            }

            1 -> {

                mDialogBinding.tvTitle.text = "Height"

                (130..220).map { "${it}cm" }.toMutableList()
            }

            2 -> {

                mDialogBinding.tvTitle.text = "Profession"

                val mProfessionCache = getCache(AppConstant.Constant.PROFESSION, "")

                if (mProfessionCache.isNotEmpty()) {

                    val mTagConfigData = mProfessionCache.formatListJson<TagConfigData>()

                    keyList = mTagConfigData

                    mTagConfigData.map { it.tagName }.toMutableList()

                } else {

                    mutableListOf()
                }

            }

            3 -> {

                mDialogBinding.tvTitle.text = "Hoping to find?"

                val mSocialAimCache = getCache(AppConstant.Constant.WANT, "")

                if (mSocialAimCache.isNotEmpty()) {

                    val mTagConfigData = mSocialAimCache.formatListJson<TagConfigData>()

                    keyList = mTagConfigData

                    mTagConfigData.map { it.tagName}.toMutableList()

                } else {

                    mutableListOf()
                }
            }

            else -> {

                mutableListOf()
            }
        }


    }


    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }


}