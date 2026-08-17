package com.yang.lovechat.ui.activity

import android.view.View
import com.yang.lovechat.BuildConfig
import com.yang.lovechat.base.activity.BaseActivity
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.databinding.ActSettingBinding
import com.yang.lovechat.dialog.NoticeDialog
import com.yang.lovechat.helper.UserInfoHold
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.copyContent
import com.yang.lovechat.util.createIntent
import com.yang.lovechat.util.edgeToEdgeBottom
import com.yang.lovechat.util.startActivity
import com.yang.lovechat.viewmodel.MainViewModel
import kotlin.jvm.java
import kotlin.toString


class SettingActivity : BaseActivity<ActSettingBinding, MainViewModel>(ActSettingBinding::inflate) {

    override fun initView() {


        withViewBinding {

            root.edgeToEdgeBottom()

            tvEmail.text = "${UserInfoHold.userInfo?.email}"

            tvId.text = "${UserInfoHold.userInfo?.id}"


            tvVersion.text = "V ${BuildConfig.VERSION_NAME}"


            sllId.setOnClickListener {

                this@SettingActivity.copyContent(tvId.text.toString())
            }


            tvBottomEmail.setOnClickListener {

                this@SettingActivity.copyContent(tvBottomEmail.text.toString())
            }

            sllAccount.clicks {

            }
            sllFeedback.clicks {

                createIntent(FeedBackActivity::class.java)
                    .startActivity(this@SettingActivity)
            }


            sllAgreement.clicks{

                createIntent(WebActivity::class.java)
                    .putExtra(AppConstant.Constant.URL, AppConstant.ClientInfo.BASE_SERVICE_POLICY_URL)
                    .putExtra(AppConstant.Constant.TITLE, "User Agreement").startActivity(this@SettingActivity)
            }
            sllPrivacy.clicks{

                createIntent(WebActivity::class.java)
                    .putExtra(AppConstant.Constant.URL, AppConstant.ClientInfo.BASE_PRIVACY_POLICY_URL)
                    .putExtra(AppConstant.Constant.TITLE, "Privacy Policy").startActivity(this@SettingActivity)
            }

            sllDelete.clicks {
                createIntent(DeleteAccountActivity::class.java).startActivity(this@SettingActivity)
            }
            sllLoginOut.clicks {

                NoticeDialog().apply {

                    initView = {dialog, mViewBinding ->

                        mViewBinding.tvTitle.visibility = View.GONE
                        mViewBinding.tvContent.text = "Are you sure you want to log out of this account?"
                        mViewBinding.tvCancel.text = "Cancel"
                        mViewBinding.tvCommit.text = "Logout"
                    }

                    onConfirm = {

                        UserInfoHold.loginOut()
                    }

                }.show(supportFragmentManager)


            }

        }

    }

    override fun initViewModel() {



    }



    override fun initData() {



    }


}