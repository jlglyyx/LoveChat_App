package com.yang.lovechat.dialog

import android.view.Gravity
import com.yang.lovechat.base.dialog.BaseDialog
import com.yang.lovechat.databinding.DialogOpenNoticeBinding
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.edgeToEdgeBottom


class OpenNoticeDialog: BaseDialog<DialogOpenNoticeBinding>(DialogOpenNoticeBinding::inflate) {


    var onConfirm: (() -> Unit)? = null

    var onCancel: (() -> Unit)? = null


    override fun initView() {

        withViewBinding {

            root.edgeToEdgeBottom()

            stvConfirm.clicks {

                onConfirm?.invoke()

                dismissAllowingStateLoss()
            }

            stvClose.clicks {

                onCancel?.invoke()

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