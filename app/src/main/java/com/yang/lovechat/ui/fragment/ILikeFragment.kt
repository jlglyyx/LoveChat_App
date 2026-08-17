package com.yang.lovechat.ui.fragment

import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.yang.lovechat.R
import com.yang.lovechat.adapter.ILikeAdapter
import com.yang.lovechat.base.bus.EventBus
import com.yang.lovechat.base.bus.EventBus.observe
import com.yang.lovechat.base.bus.EventBus.postValue
import com.yang.lovechat.base.fragment.BaseFragment
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.UserInfoData
import com.yang.lovechat.databinding.FraILikeBinding
import com.yang.lovechat.databinding.ViewEmptyLikeBinding
import com.yang.lovechat.databinding.ViewNoNetworkBinding
import com.yang.lovechat.dialog.FastConnectDialog
import com.yang.lovechat.ui.activity.UserInfoActivity
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.createIntent
import com.yang.lovechat.util.refreshLoadDataListener
import com.yang.lovechat.util.refreshLoadListener
import com.yang.lovechat.util.startActivity
import com.yang.lovechat.viewmodel.MainViewModel
import com.yang.lovechat.widget.ErrorReLoadView

class ILikeFragment: BaseFragment<FraILikeBinding, MainViewModel>(FraILikeBinding::inflate) {

    private var mFastConnectDialog: FastConnectDialog? = null
    private val mILikeAdapter: ILikeAdapter by lazy {
        ILikeAdapter()
    }

    private lateinit var mQuickAdapterHelper: QuickAdapterHelper

    override fun initView() {

        withViewBinding {

            initRecyclerView()

        }
    }

    override fun initData() {

        mViewModel.getILikeList()

    }

    override fun initViewModel() {

        mViewModel.mILikeListData.observe(this){


            withViewBinding {
                mILikeAdapter.refreshLoadDataListener(
                    mSwipeRefreshLayout,
                    mQuickAdapterHelper,
                    errorReLoadView,
                    it
                )

            }
        }
        mViewModel.requestExceptionEvent.observe(this){

            mViewBinding.mSwipeRefreshLayout.isRefreshing = false

            mViewBinding.errorReLoadView.showStatusView(ErrorReLoadView.Status.NO_NETWORK)

        }

        EventBus.with(AppConstant.EventConstant.EVENT_FAST_CONNECT_SUCCESS).observe(this){

            val item = mILikeAdapter.items.findLast { find -> find.id == it }

            item?.let {

                mILikeAdapter.remove(item)

                mViewBinding.errorReLoadView.showSuccessView(mILikeAdapter.items)

            }
        }

    }


    private fun initRecyclerView() {


        withViewBinding {

            mQuickAdapterHelper = mILikeAdapter.refreshLoadListener(mSwipeRefreshLayout, onRefresh = {

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

                        ivImage.setImageResource(R.drawable.iv_swip)
                        tvTitle.text = "No one you like yet"
                        tvDesc.text = "Swipe more cards to find someone you fancy"
                        stvConfirm.text = "Swipe Now"


                        stvConfirm.clicks {

                            EventBus.with(AppConstant.EventConstant.EVENT_SET_PAGE).postValue(0)
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

            mViewBinding.errorReLoadView.showSuccessView(mILikeAdapter.items)


            mILikeAdapter.addOnDebouncedChildClick(R.id.iv_connect){ _, _, position ->

                val item = mILikeAdapter.getItem(position)

                initFastConnectDialog(item)




            }





            mILikeAdapter.setOnDebouncedItemClick { _, _, position ->

                val item = mILikeAdapter.getItem(position)

                createIntent(UserInfoActivity::class.java)
                    .putExtra(AppConstant.Constant.USER_ID, item.id)
                    .startActivity(requireActivity())

            }

        }


    }


    private fun onRefresh() {

        mViewModel.pageNum = 1

        mViewModel.getILikeList()
    }

    private fun onLoadMore() {

        mViewModel.pageNum++

        mViewModel.getILikeList()

    }

    private fun initFastConnectDialog(item: UserInfoData) {

        mFastConnectDialog?.dismissAllowingStateLoss()

        mFastConnectDialog =  FastConnectDialog.newInstance(item)

        mFastConnectDialog?.show(parentFragmentManager)
    }

}