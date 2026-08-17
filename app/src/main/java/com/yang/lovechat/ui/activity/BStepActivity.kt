package com.yang.lovechat.ui.activity

import com.google.android.flexbox.FlexboxLayoutManager
import com.yang.lovechat.adapter.InterestAdapter
import com.yang.lovechat.adapter.WantAdapter
import com.yang.lovechat.base.activity.BaseActivity
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.UpdateUserInfoData
import com.yang.lovechat.data.UpdateUserTagData
import com.yang.lovechat.databinding.ActBStepBinding
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.createIntent
import com.yang.lovechat.util.edgeToEdgeAll
import com.yang.lovechat.util.startActivity
import com.yang.lovechat.viewmodel.MainViewModel


class BStepActivity : BaseActivity<ActBStepBinding, MainViewModel>(ActBStepBinding::inflate) {


    private val mWantAdapter by lazy { WantAdapter() }

    private val mInterestAdapter by lazy { InterestAdapter() }


    override fun initView() {

        mViewBinding.root.edgeToEdgeAll()


        mViewBinding.apply {


            initRecyclerView()

            initInterestRecyclerView()

            stvNext.clicks {

                val updateUserInfoData = UpdateUserInfoData()

                val wantConfigData = mWantAdapter.items.findLast { item -> item.isCheck }?:return@clicks

                val userTagList = mInterestAdapter.items.filter { item -> item.isCheck }.map {item ->

                    UpdateUserTagData(item.id, item.tagType)

                }.toMutableList()


                updateUserInfoData.interest = userTagList

                updateUserInfoData.intent = wantConfigData.tagName

                mViewModel.updateUserInfo(updateUserInfoData)

                showLoading()

            }

        }


    }

    override fun initViewModel() {

        mViewModel.mUpdateUserInfoStatus.observe(this) {

            dismissLoading()

            createIntent(CStepActivity::class.java).startActivity(this@BStepActivity)
        }

        mViewModel.requestFailEvent.observe(this){

            dismissLoading()
        }


    }


    override fun initData() {


    }


    private fun initRecyclerView() {

        withViewBinding {

            recyclerView.adapter = mWantAdapter


            mWantAdapter.setOnItemClickListener { _, _, position ->

                mWantAdapter.items.findLast { it.isCheck }?.isCheck = false

                mWantAdapter.notifyItemRangeChanged(0, mWantAdapter.itemCount, false)

                val item = mWantAdapter.getItem(position)

                item.isCheck = !item.isCheck

                mWantAdapter.notifyItemChanged(position, false)

                canNext()
            }

            val tagConfigData = mViewModel.getCacheTagConfig(AppConstant.Constant.WANT)

            mWantAdapter.submitList(tagConfigData)

        }

    }


    private fun initInterestRecyclerView() {

        withViewBinding {

            interestRecyclerView.adapter = mInterestAdapter

            interestRecyclerView.layoutManager = FlexboxLayoutManager(this@BStepActivity)

            mInterestAdapter.setOnItemClickListener { _, _, position ->

                val item = mInterestAdapter.getItem(position)

                item.isCheck = !item.isCheck

                mInterestAdapter.notifyItemChanged(position, false)

                canNext()
            }

            val tagConfigData = mViewModel.getCacheTagConfig(AppConstant.Constant.INTEREST)

            mInterestAdapter.submitList(tagConfigData)

        }

    }


    fun canNext(){

       mViewBinding.stvNext.isEnabled =  mWantAdapter.items.any { it.isCheck } && mInterestAdapter.items.any { it.isCheck }

    }

}