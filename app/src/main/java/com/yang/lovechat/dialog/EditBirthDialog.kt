package com.yang.lovechat.dialog

import android.os.Bundle
import android.view.Gravity
import com.yang.lovechat.base.dialog.BaseDialog
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.databinding.DialogEditBirthBinding
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.dateFormat
import com.yang.lovechat.util.edgeToEdgeBottom


import java.util.Date
import kotlin.apply
import kotlin.let
import kotlin.text.isNotEmpty


class EditBirthDialog : BaseDialog<DialogEditBirthBinding>(DialogEditBirthBinding::inflate) {

    var onConfirm: (Date?,Int) -> Unit = {_,_ ->}


    private var mCurrentDate: Date? = null

    private var mStartDate: Date? = null




    companion object {
        fun newInstance(data: String): EditBirthDialog {
            return EditBirthDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.DATA, data)
                }
            }
        }
    }


    override fun initView() {


        withViewBinding {

            root.edgeToEdgeBottom()


            arguments?.let {

                val data = it.getString(AppConstant.Constant.DATA, "")

                if (data.isNotEmpty()) {
                    mTimePickView.setCurrentTime(data)
                }
            }

            tvAge.text = "You're ${mTimePickView.getCurrentAge()} years old"

            mCurrentDate = mTimePickView.getCurrentDate()

            mStartDate = mCurrentDate

            mTimePickView.onTimeChange = { date, age ->

                tvAge.text = "You're $age years old"

                mCurrentDate = date

                tvCommit.isEnabled = (mCurrentDate?.dateFormat("yyyy.MM.dd") != mStartDate?.dateFormat("yyyy.MM.dd")) && age >= 18
            }

            tvCancel.clicks {

                dismissAllowingStateLoss()
            }

            tvCommit.clicks {


                onConfirm(mCurrentDate,mTimePickView.getCurrentAge())

                dismissAllowingStateLoss()


            }

        }

    }

    override fun initData() {


    }


    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }


}