package com.yang.lovechat.ui.fragment

import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.yang.lovechat.R
import com.yang.lovechat.adapter.UserAdapter
import com.yang.lovechat.adapter.UserListAdapter
import com.yang.lovechat.base.fragment.BaseFragment
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.databinding.FraUserListBinding
import com.yang.lovechat.ui.activity.UserInfoActivity
import com.yang.lovechat.util.createIntent
import com.yang.lovechat.util.startActivity
import com.yang.lovechat.viewmodel.MainViewModel

class UserListFragment: BaseFragment<FraUserListBinding, MainViewModel>(FraUserListBinding::inflate) {


    private val mUserListAdapter: UserListAdapter by lazy { UserListAdapter() }

    private val mUserAdapter by lazy { UserAdapter() }

    override fun initView() {

        withViewBinding {

            initUserRecyclerView()

            initRecyclerView()

            swipeRefreshLayout.setOnRefreshListener {

                onRefresh()

            }

        }
    }

    override fun initData() {


        onRefresh()

    }

    override fun initViewModel() {

        mViewModel.mChatterUserListData.observe(this){

            mUserListAdapter.submitList(it)

            mViewBinding.swipeRefreshLayout.isRefreshing = false

        }

    }

    private fun initUserRecyclerView() {


        withViewBinding {

            userRecyclerView.adapter = mUserListAdapter

            userRecyclerView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)

        }



    }
    private fun initRecyclerView() {

        withViewBinding {

            recyclerView.adapter = mUserAdapter

            recyclerView.setItemViewCacheSize(10)

            recyclerView.setRecycledViewPool(mUserAdapter.sharedPool)

            recyclerView.animation = null

            mUserAdapter.addOnDebouncedChildClick(R.id.tv_name) { _, _, position ->

                val item = mUserAdapter.getItem(position)

                createIntent(UserInfoActivity::class.java)
                    .putExtra(AppConstant.Constant.USER_ID, item.id)
                    .startActivity(requireActivity())

            }
            mUserAdapter.setOnDebouncedItemClick { _, _, position ->

                val item = mUserAdapter.getItem(position)


            }

        }



    }


    private fun onRefresh() {

        mViewModel.getChatterUser()

    }

    private fun onLoadMore() {

        mViewModel.pageNum++

        mViewModel.getLikeIList()


    }


}