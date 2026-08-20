package com.yang.lovechat.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.yang.lovechat.base.dialog.BaseDialog
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.databinding.DialogMoreMenuBinding
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.createIntent
import com.yang.lovechat.util.edgeToEdgeBottom
import com.yang.lovechat.util.startActivity
import kotlin.apply
import kotlin.jvm.java

class MoreMenuDialog : BaseDialog<DialogMoreMenuBinding>(DialogMoreMenuBinding::inflate) {

    private var showBlock = true

    private var eventId : String? = null

    private var eventType : Int? = null

    var onBlock: (() -> Unit)? = null

    companion object {
        fun newInstance(
            data: Boolean,
            eventId: String,
            eventType: Int,//0 user 1 message
        ): MoreMenuDialog {
            return MoreMenuDialog().apply {
                arguments = Bundle().apply {
                    putBoolean(AppConstant.Constant.DATA, data)
                    putString(AppConstant.Constant.ID, eventId)
                    putInt(AppConstant.Constant.TYPE, eventType)
                }
            }
        }
    }


    override fun initView() {

        withViewBinding {

            root.edgeToEdgeBottom()

            tvCancel.clicks {

                dismissAllowingStateLoss()
            }

            tvBlock.clicks {

                initBlockDialog()
            }
            tvRetort.clicks {


                dismissAllowingStateLoss()
            }


        }

    }

    override fun initData() {

        arguments?.let {

            showBlock =  it.getBoolean(AppConstant.Constant.DATA,showBlock)

            eventId =  it.getString(AppConstant.Constant.ID)

            eventType =  it.getInt(AppConstant.Constant.TYPE)

            if (showBlock){
                mDialogBinding.tvBlock.visibility = View.VISIBLE
            }else{
                mDialogBinding.tvBlock.visibility = View.GONE
            }
        }



    }


    private fun initBlockDialog() {


        val mNoticeDialog = NoticeDialog().apply {

            initView = { dialog, mViewBinding ->
                mViewBinding.tvTitle.text = "Block"
                mViewBinding.tvContent.text = "Confirm to blacklist this user"
            }

            onConfirm = {

                onBlock?.invoke()

                this@MoreMenuDialog.dismissAllowingStateLoss()
            }
        }

        mNoticeDialog.show(parentFragmentManager)

    }

    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }


}