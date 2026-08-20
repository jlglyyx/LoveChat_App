package com.yang.lovechat.ui.fragment

import com.yang.lovechat.base.fragment.BaseFragment
import com.yang.lovechat.databinding.FraUserListBinding
import com.yang.lovechat.viewmodel.MainViewModel

class UserListFragment: BaseFragment<FraUserListBinding, MainViewModel>(FraUserListBinding::inflate) {



    override fun initView() {

        withViewBinding {

            initRecyclerView()

        }
    }

    override fun initData() {

        mViewModel.getLikeIList()


    }

    override fun initViewModel() {




    }

    private fun initRecyclerView() {





    }


    private fun onRefresh() {

        mViewModel.pageNum = 1

        mViewModel.getLikeIList()

    }

    private fun onLoadMore() {

        mViewModel.pageNum++

        mViewModel.getLikeIList()


    }


}