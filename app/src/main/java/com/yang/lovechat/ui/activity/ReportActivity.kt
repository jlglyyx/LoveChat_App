package com.yang.lovechat.ui.activity

import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import com.yang.lovechat.base.activity.BaseActivity
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.databinding.ActReportBinding
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.edgeToEdgeBottom
import com.yang.lovechat.util.showShort
import com.yang.lovechat.viewmodel.MessageViewModel


class ReportActivity : BaseActivity<ActReportBinding, MessageViewModel>(ActReportBinding::inflate){

    private var eventId : String? = null

    private var eventType : Int? = null


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

                mViewModel.userReport(mViewBinding.setText.text.toString(),textEmail,eventId!!,eventType!!)

            }
        }

    }

    override fun initData() {


        eventId =  intent.getStringExtra(AppConstant.Constant.ID)

        eventType =  intent.getIntExtra(AppConstant.Constant.TYPE,0)


    }

    override fun initViewModel() {

        mViewModel.mUserReportStatus.observe(this){

            showShort("Report successful")

            finish()

        }


    }







}