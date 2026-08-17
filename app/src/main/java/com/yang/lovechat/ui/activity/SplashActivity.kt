package com.yang.lovechat.ui.activity

import android.view.LayoutInflater
import com.yang.lovechat.R
import com.yang.lovechat.base.activity.BaseActivity
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.databinding.ActSplashBinding
import com.yang.lovechat.databinding.ViewNoNetworkBinding
import com.yang.lovechat.dialog.ChangeUrlDialog
import com.yang.lovechat.helper.UserInfoHold
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.createIntent
import com.yang.lovechat.util.isVpnConnected
import com.yang.lovechat.util.startActivity
import com.yang.lovechat.viewmodel.MainViewModel
import com.yang.lovechat.widget.ErrorReLoadView
import kotlin.jvm.java


class SplashActivity : BaseActivity<ActSplashBinding, MainViewModel>(ActSplashBinding::inflate) {

    override fun initView() {


        mViewBinding.apply {

            errorReLoadView.addNoNetView { viewGroup ->
                ViewNoNetworkBinding.inflate(
                    LayoutInflater.from(this@SplashActivity),
                    viewGroup,
                    true
                )
                    .apply {

                        stvConfirm.clicks {

                            initConfig()
                        }

                        ivImage.setOnLongClickListener { view ->

                            ChangeUrlDialog().show(supportFragmentManager)

                            return@setOnLongClickListener true
                        }


                    }
            }



        }


    }

    override fun initViewModel() {

        mViewModel.mConfigStatus.observe(this){

            if (it){
                if (isVpnConnected(this)) {

                    createIntent(LoginActivity::class.java).putExtra(AppConstant.Constant.TYPE, "VPN")
                        .startActivity(this, true)

                    overridePendingTransition(0, R.anim.fade_out)

                    return@observe

                }


                if (null == UserInfoHold.userInfo) {

                    createIntent(LoginActivity::class.java)
                        .startActivity(this, true)

                    overridePendingTransition(0, R.anim.fade_out)

                    return@observe

                }

                createIntent(MainActivity::class.java).startActivity(this, true)

                overridePendingTransition(0, R.anim.fade_out)
            }

        }


        mViewModel.requestExceptionEvent.observe(this){

            mViewBinding.errorReLoadView.showStatusView(ErrorReLoadView.Status.NO_NETWORK)

        }

    }


    override fun initData() {


        initConfig()

    }


    private fun initConfig() {

        mViewModel.checkNewVersion {
            mViewModel.getTagConfig()
        }

    }


}