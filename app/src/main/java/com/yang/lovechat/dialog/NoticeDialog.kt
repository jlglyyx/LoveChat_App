package com.yang.lovechat.dialog

import android.view.Gravity
import com.yang.lovechat.base.dialog.BaseDialog
import com.yang.lovechat.databinding.DialogNoticeBinding
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.getScreenPx

class NoticeDialog: BaseDialog<DialogNoticeBinding>(DialogNoticeBinding::inflate) {

    var onConfirm: (() -> Unit)? = null

    var onCancel:  (() -> Unit)? = null


    override fun initView() {

        withViewBinding {

            tvCancel.clicks {

                onCancel?.invoke()
                dismissAllowingStateLoss()
            }
            tvCommit.clicks {

                onConfirm?.invoke()

                dismissAllowingStateLoss()
            }


        }

    }

    override fun initData() {

    }

    override fun setDialogWidth(): Int {
        return getScreenPx(requireContext())[0]*8/10
    }

    override fun setDialogGravity(): Int {
        return Gravity.CENTER
    }


}