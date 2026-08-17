package com.yang.lovechat.ui.fragment

import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.yang.lovechat.R
import com.yang.lovechat.adapter.ViewedIAdapter
import com.yang.lovechat.base.bus.EventBus
import com.yang.lovechat.base.bus.EventBus.observe
import com.yang.lovechat.base.fragment.BaseFragment
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.UserInfoData
import com.yang.lovechat.databinding.FraViewedIBinding
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

class ViewedIFragment : BaseFragment<FraViewedIBinding, MainViewModel>(FraViewedIBinding::inflate) {

    private val mViewedIAdapter: ViewedIAdapter by lazy {
        ViewedIAdapter()
    }

    private var currentPosition = -1
    private lateinit var mQuickAdapterHelper: QuickAdapterHelper

    private var mFastConnectDialog: FastConnectDialog? = null

    override fun initView() {

        withViewBinding {

            initRecyclerView()

        }
    }

    override fun initData() {

        mViewModel.getViewedIList()


    }

    override fun initViewModel() {


        mViewModel.mViewedIListData.observe(this) {

            withViewBinding {
                mViewedIAdapter.refreshLoadDataListener(
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


        EventBus.with(AppConstant.EventConstant.EVENT_FAST_CONNECT_SUCCESS).observe(this) {

            val item = mViewedIAdapter.items.findLast { find -> find.id == it }

            item?.let {

                mViewedIAdapter.remove(item)

                mViewBinding.errorReLoadView.showSuccessView(mViewedIAdapter.items)

            }
        }

        EventBus.with(AppConstant.EventConstant.EVENT_RECEIVED_VIEW_I).observe(this) {

            onRefresh()
        }
        EventBus.with(AppConstant.EventConstant.EVENT_BUY_VIP_SUCCESS).observe(this) {

            mViewedIAdapter.openAllImage()
        }

    }


    private fun initRecyclerView() {


        withViewBinding {

            mQuickAdapterHelper =
                mViewedIAdapter.refreshLoadListener(mSwipeRefreshLayout, onRefresh = {

                    onRefresh()

                }, onLoad = {

                    onLoadMore()
                })

            recyclerView.adapter = mQuickAdapterHelper.adapter


            recyclerView.setRecycledViewPool(RecyclerView.RecycledViewPool())

            recyclerView.itemAnimator = null

            recyclerView.layoutManager = LinearLayoutManager(requireContext())


            errorReLoadView.addEmptyView { viewGroup ->
                ViewEmptyLikeBinding.inflate(
                    LayoutInflater.from(requireContext()),
                    viewGroup,
                    true
                )
                    .apply {

                        ivImage.setImageResource(R.drawable.iv_viewed)
                        tvTitle.text = "No profile visitors for now"
                        tvDesc.text = "Fill in your info to draw more attention"
                        stvConfirm.text = "Update Info"


                        stvConfirm.clicks {
                            createIntent(EditUserInfoActivity::class.java).startActivity(
                                requireActivity()
                            )
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

            mViewBinding.errorReLoadView.showSuccessView(mViewedIAdapter.items)



            mViewedIAdapter.addOnDebouncedChildClick(R.id.tv_chat) { _, _, position ->

                val item = mViewedIAdapter.getItem(position)

                currentPosition = position

                if (UserInfoHold.isVip) {

                    initFastConnectDialog(item)

                } else {
                    ProductHelper.showPayProductDialog(
                        this@ViewedIFragment,
                        AppConstant.Constant.PRODUCT_VIP
                    )
                }


            }


            mViewedIAdapter.setOnDebouncedItemClick(500) { _, _, position ->

                val item = mViewedIAdapter.getItem(position)

                currentPosition = position

                if (UserInfoHold.isVip) {

                    createIntent(UserInfoActivity::class.java)
                        .putExtra(AppConstant.Constant.USER_ID, item.id)
                        .startActivity(requireActivity())

                } else {
                    ProductHelper.showPayProductDialog(
                        this@ViewedIFragment,
                        AppConstant.Constant.PRODUCT_VIP
                    )
                }

            }

        }


    }


    private fun onRefresh() {

        mViewModel.pageNum = 1

        mViewModel.getViewedIList()

    }

    private fun onLoadMore() {

        mViewModel.pageNum++

        mViewModel.getViewedIList()


    }

    private fun initFastConnectDialog(item: UserInfoData) {

        mFastConnectDialog?.dismissAllowingStateLoss()

        mFastConnectDialog = FastConnectDialog.newInstance(item)

        mFastConnectDialog?.show(parentFragmentManager)
    }
}