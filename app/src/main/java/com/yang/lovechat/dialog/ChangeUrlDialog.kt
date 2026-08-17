package com.yang.lovechat.dialog

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.view.Gravity
import com.yang.lovechat.base.dialog.BaseDialog
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.databinding.DialogChangeUrlBinding
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.getCache
import com.yang.lovechat.util.setCache
import java.lang.ref.WeakReference
import kotlin.apply
import kotlin.let
import kotlin.system.exitProcess

class ChangeUrlDialog: BaseDialog<DialogChangeUrlBinding>(DialogChangeUrlBinding::inflate) {


    override fun initView() {

        mDialogBinding.apply {


            tvTv0.setHint(getCache(AppConstant.Constant.IP, AppConstant.ClientInfo.BASE_IP))

            tvTv3.clicks {

                setCache(AppConstant.Constant.IP,tvTv0.text.toString())

                dismiss()

                restartApp()
            }

        }
    }

    override fun initData() {

    }


    private fun restartApp() {

        val packageManager = requireActivity().packageManager
        val intent = packageManager.getLaunchIntentForPackage(requireActivity().packageName)
        intent?.let {
            it.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_NEW_TASK
            )

            val activityRef = WeakReference(activity)

            Handler(Looper.getMainLooper()).postDelayed({
                activityRef.get()?.startActivity(it)
                activityRef.get()?.finish()
                Process.killProcess(Process.myPid())
                exitProcess(0)
            }, 1000)
        }

    }


    override fun setDialogGravity(): Int {
        return Gravity.CENTER
    }

}