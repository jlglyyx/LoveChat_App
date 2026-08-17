package com.yang.lovechat.dialog

import android.R.attr.text
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import com.yang.lovechat.base.dialog.BaseDialog
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.databinding.DialogBuyProductStatusBinding
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.edgeToEdgeBottom

class BuyProductStatusDialog : BaseDialog<DialogBuyProductStatusBinding>(DialogBuyProductStatusBinding::inflate) {

    companion object {
        fun newInstance(data: String): BuyProductStatusDialog {
            return BuyProductStatusDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.DATA,data)
                }
            }
        }
    }


    override fun initView() {

        withViewBinding {

            root.edgeToEdgeBottom()


            stvNext.clicks {


                dismissAllowingStateLoss()
            }


        }

    }

    override fun initData() {

        AppConstant.Constant.isShowBuy = true

        arguments?.let {

            val data = it.getString(AppConstant.Constant.DATA) ?: ""

            mDialogBinding.tvDesc.text = "Your ${data} is activated instantly. "

        }
    }



    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        AppConstant.Constant.isShowBuy = false
    }



}