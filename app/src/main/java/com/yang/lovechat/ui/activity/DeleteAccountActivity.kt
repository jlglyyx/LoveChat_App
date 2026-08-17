package com.yang.lovechat.ui.activity


import com.yang.lovechat.R
import com.yang.lovechat.base.activity.BaseActivity
import com.yang.lovechat.databinding.ActDeleteAccountBinding
import com.yang.lovechat.dialog.NoticeDialog
import com.yang.lovechat.helper.UserInfoHold
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.edgeToEdgeBottom
import com.yang.lovechat.viewmodel.MineViewModel


class DeleteAccountActivity : BaseActivity<ActDeleteAccountBinding, MineViewModel>(ActDeleteAccountBinding::inflate) {

    private var mNoticeDialog: NoticeDialog? = null


    override fun initView() {

        mViewBinding.root.edgeToEdgeBottom()

        mViewBinding.llCheck.setOnClickListener {

            mViewBinding.ivCheck.isSelected = !mViewBinding.ivCheck.isSelected

            mViewBinding.ivCheck.setImageResource(if (mViewBinding.ivCheck.isSelected) R.drawable.iv_check_ed else R.drawable.iv_check)

            mViewBinding.stvDelete.isEnabled = mViewBinding.ivCheck.isSelected
        }


        mViewBinding.stvDelete.clicks {

            if (mViewBinding.ivCheck.isSelected) {

                initNoticeDialog()

            }
        }
    }

    override fun initData() {


    }

    override fun initViewModel() {


        mViewModel.mDeleteUserStatus.observe(this) {

            UserInfoHold.loginOut()
        }

    }


    private fun initNoticeDialog() {

        try {
            mNoticeDialog?.dismissAllowingStateLoss()

            mNoticeDialog = NoticeDialog().apply {

                initView = { dialog, mViewBinding ->
                    mViewBinding.tvTitle.text = "Delete account"
                    mViewBinding.tvContent.text = "Are you sure you want to delete your account?"
                }

                onConfirm = {
                    mViewModel.deleteUser()

                }
            }

            mNoticeDialog?.show(supportFragmentManager)


        } catch (e: Exception) {

            e.printStackTrace()
        }

    }


}