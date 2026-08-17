package com.yang.lovechat.ui.activity

import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import com.yang.lovechat.base.activity.BaseActivity
import com.yang.lovechat.databinding.ActFeedbackBinding
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.edgeToEdgeBottom
import com.yang.lovechat.util.showShort
import com.yang.lovechat.viewmodel.MineViewModel
import kotlin.text.contains
import kotlin.text.isNotEmpty
import kotlin.toString


class FeedBackActivity : BaseActivity<ActFeedbackBinding, MineViewModel>(ActFeedbackBinding::inflate) {


    override fun initView() {


        withViewBinding {

            root.edgeToEdgeBottom()

            setText.doAfterTextChanged {

                mViewBinding.tvCount.text = "${mViewBinding.setText.text.toString().length}/500"

                mViewBinding.stvNext.isEnabled = (mViewBinding.setText as TextView).text.toString().isNotEmpty()

            }

            stvNext.clicks {

                val textEmail = mViewBinding.setEmails.text.toString()

                if (textEmail.isNotEmpty() && !textEmail.contains("@")) {
                    showShort("Email format error")
                    return@clicks
                }

                mViewModel.userFeedback(mViewBinding.setText.text.toString(),textEmail)

            }
        }




    }

    override fun initData() {

    }

    override fun initViewModel() {

        mViewModel.mUserFeedbackStatus.observe(this) {

            showShort("FeedBack Success")

            finish()
        }

    }



}