package com.yang.lovechat.ui.fragment

import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.yang.lovechat.R
import com.yang.lovechat.adapter.LikeIAdapter
import com.yang.lovechat.base.bus.EventBus
import com.yang.lovechat.base.bus.EventBus.observe
import com.yang.lovechat.base.fragment.BaseFragment
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.UserInfoData
import com.yang.lovechat.databinding.FraLikeIBinding
import com.yang.lovechat.databinding.ViewEmptyLikeBinding
import com.yang.lovechat.databinding.ViewNoNetworkBinding
import com.yang.lovechat.dialog.FastConnectDialog
import com.yang.lovechat.helper.ProductHelper
import com.yang.lovechat.helper.UserInfoHold
import com.yang.lovechat.ui.activity.EditUserInfoActivity
import com.yang.lovechat.ui.activity.UserInfoActivity
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.createIntent
import com.yang.lovechat.util.refreshLoadDataListener
import com.yang.lovechat.util.refreshLoadListener
import com.yang.lovechat.util.startActivity
import com.yang.lovechat.viewmodel.MainViewModel
import com.yang.lovechat.widget.ErrorReLoadView
import kotlin.jvm.java

class LikeIFragment: BaseFragment<FraLikeIBinding, MainViewModel>(FraLikeIBinding::inflate) {

    private val mLikeIAdapter: LikeIAdapter by lazy {
        LikeIAdapter()
    }

    private lateinit var mQuickAdapterHelper: QuickAdapterHelper

    private var currentPosition = -1

    private var mFastConnectDialog: FastConnectDialog? = null

    override fun initView() {

        withViewBinding {

            initRecyclerView()

        }
    }

    override fun initData() {

        mViewModel.getLikeIList()


    }

    override fun initViewModel() {


        mViewModel.mLikeIListData.observe(this){

            withViewBinding {
                mLikeIAdapter.refreshLoadDataListener(
                    mSwipeRefreshLayout,
                    mQuickAdapterHelper,
                    errorReLoadView,
                    it
                )

            }
        }

        mViewModel.requestExceptionEvent.observe(this) {

            mViewBinding.mSwipeRefreshLayout.isRefreshing = false

            mViewBinding.errorReLoadView.showStatusView(ErrorReLoadView.Status.NO_NETWORK)


        }

        EventBus.with(AppConstant.EventConstant.EVENT_FAST_CONNECT_SUCCESS).observe(this){

            val item = mLikeIAdapter.items.findLast { find -> find.id == it }

            item?.let {

                mLikeIAdapter.remove(item)

                mViewBinding.errorReLoadView.showSuccessView(mLikeIAdapter.items)

            }
        }


        EventBus.with(AppConstant.EventConstant.EVENT_RECEIVED_LIKE_I).observe(this){

            onRefresh()

        }

        EventBus.with(AppConstant.EventConstant.EVENT_BUY_VIP_SUCCESS).observe(this){

           mLikeIAdapter.openAllImage()

        }



    }

    private fun initRecyclerView() {


        withViewBinding {

            mQuickAdapterHelper = mLikeIAdapter.refreshLoadListener(mSwipeRefreshLayout, onRefresh = {

                onRefresh()

            }, onLoad = {

                onLoadMore()
            })

            recyclerView.adapter = mQuickAdapterHelper.adapter


            recyclerView.setRecycledViewPool(RecyclerView.RecycledViewPool())

            recyclerView.itemAnimator = null

            recyclerView.layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)


            errorReLoadView.addEmptyView { viewGroup ->
                ViewEmptyLikeBinding.inflate(
                    LayoutInflater.from(requireContext()),
                    viewGroup,
                    true
                )
                    .apply {

                        stvConfirm.clicks {

                            createIntent(EditUserInfoActivity::class.java).startActivity(requireActivity())
                        }

                    }
            }

            errorReLoadView.addNoNetView { viewGroup ->
                ViewNoNetworkBinding.inflate(LayoutInflater.from(requireContext()), viewGroup, true)
                    .apply {

                        stvConfirm.clicks {

                            onRefresh()
                        }
                    }
            }
//
            mViewBinding.errorReLoadView.showSuccessView(mLikeIAdapter.items)


            mLikeIAdapter.addOnDebouncedChildClick(R.id.iv_connect){ _, _, position ->

                val item = mLikeIAdapter.getItem(position)

                currentPosition = position

                if (UserInfoHold.isVip) {

                    initFastConnectDialog(item)

                } else {
                    ProductHelper.showPayProductDialog(this@LikeIFragment,AppConstant.Constant.PRODUCT_VIP)
                }


            }


            mLikeIAdapter.setOnDebouncedItemClick(500) { _, _, position ->

                val item = mLikeIAdapter.getItem(position)

                currentPosition = position

                if (UserInfoHold.isVip) {

                    createIntent(UserInfoActivity::class.java)
                        .putExtra(AppConstant.Constant.USER_ID, item.id)
                        .startActivity(requireActivity())

                } else {
                    ProductHelper.showPayProductDialog(this@LikeIFragment,AppConstant.Constant.PRODUCT_VIP)
                }


            }

        }


    }


    private fun onRefresh() {

        mViewModel.pageNum = 1

        mViewModel.getLikeIList()

    }

    private fun onLoadMore() {

        mViewModel.pageNum++

        mViewModel.getLikeIList()


    }

    private fun initFastConnectDialog(item: UserInfoData) {

        mFastConnectDialog?.dismissAllowingStateLoss()

        mFastConnectDialog =  FastConnectDialog.newInstance(item)

        mFastConnectDialog?.show(parentFragmentManager)
    }

}