package com.yang.lovechat.ui.activity

import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.blankj.utilcode.util.SpanUtils
import com.yang.lovechat.R
import com.yang.lovechat.base.activity.BaseActivity
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.databinding.ActLoginBinding
import com.yang.lovechat.dialog.ChangeUrlDialog
import com.yang.lovechat.dialog.LoginEmailDialog
import com.yang.lovechat.dialog.NoticeDialog
import com.yang.lovechat.util.GoogleLoginUtil
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.copyContent
import com.yang.lovechat.util.createIntent
import com.yang.lovechat.util.edgeToEdgeBottom
import com.yang.lovechat.util.getCache
import com.yang.lovechat.util.isVpnConnected
import com.yang.lovechat.util.setCache
import com.yang.lovechat.util.showShort
import com.yang.lovechat.util.startActivity
import com.yang.lovechat.viewmodel.MainViewModel

import kotlin.jvm.java


class LoginActivity : BaseActivity<ActLoginBinding, MainViewModel>(ActLoginBinding::inflate) {



    override fun initView() {

        mViewBinding.apply {

            tvNotice.edgeToEdgeBottom()


            createText(tvNotice)


            ivBg1.setOnLongClickListener { view ->

                ChangeUrlDialog().show(supportFragmentManager)

                return@setOnLongClickListener true
            }


            sllEmailLogin.clicks {


                initPrivacyNoticeDialog{

                    LoginEmailDialog().show(supportFragmentManager)

                }


            }



        }


    }

    override fun initData() {


        val isVpn = intent.getStringExtra(AppConstant.Constant.TYPE)


        if (isVpn == "VPN"){

            initNoticeDialog(false)

        }



    }

    private fun createNoticeText(text:TextView) {

       SpanUtils.with(text).append("Your account did not pass our security verification.If you believe this is a mistake, please contact us at:\n")
            .append("service@twomorrow-chat.com")
            .setClickSpan(getColor(R.color.color_B45CFF), true) {

                this.copyContent("service@twomorrow-chat.com")

            }
            .create()
    }

    private fun createText(text:TextView){
        SpanUtils.with(text).append("By continuing, you agree to ${getString(R.string.app_name)}'s\n")
            .append("Terms of Service")
            .setUnderline()
            .setClickSpan(getColor(R.color.white), true) {
                createIntent(WebActivity::class.java)
                    .putExtra(AppConstant.Constant.URL, AppConstant.ClientInfo.BASE_SERVICE_POLICY_URL)
                    .putExtra(AppConstant.Constant.TITLE, "User Agreement").startActivity(this@LoginActivity)
            }
            .append(" and ")
            .append("Privacy Policy.")
            .setClickSpan(getColor(R.color.white), true) {
                createIntent(WebActivity::class.java)
                    .putExtra(AppConstant.Constant.URL, AppConstant.ClientInfo.BASE_PRIVACY_POLICY_URL)
                    .putExtra(AppConstant.Constant.TITLE, "Privacy Policy").startActivity(this@LoginActivity)
            }
            .create()
    }

    override fun initViewModel() {

        mViewModel.mLoginInfoData.observe(this) {

            dismissLoading()

            createIntent(MainActivity::class.java).startActivity(this, true)

        }

        mViewModel.requestFailEvent.observe(this) {

            dismissLoading()

            if (it is Boolean){
                initNoticeDialog(true)
            }

        }


    }

    private fun initNoticeDialog(isShuMei: Boolean) {


        NoticeDialog().apply {

            initView = { dialog, mViewBinding ->

                mViewBinding.tvTitle.visibility = View.GONE
               createNoticeText(mViewBinding.tvContent)
                mViewBinding.tvCancel.visibility = View.GONE
                mViewBinding.tvCommit.text = "OK"

            }

        }.show(supportFragmentManager)



    }
    private fun initPrivacyNoticeDialog(onSuccess: () -> Unit) {

        val cache = getCache(AppConstant.Constant.LOGIN_NOTICE, false)

        if (cache) {
            onSuccess()
            return
        }

        NoticeDialog().apply {

            initView = { dialog, mViewBinding ->

                mViewBinding.tvTitle.text = "Welcome to ${getString(R.string.app_name)}"
                createText(mViewBinding.tvContent)
                mViewBinding.tvCancel.text = "Cancel"
                mViewBinding.tvCommit.text = "Continue"

            }

            onConfirm = {

                setCache(AppConstant.Constant.LOGIN_NOTICE, true)

                onSuccess()
            }

        }.show(supportFragmentManager)



    }



}