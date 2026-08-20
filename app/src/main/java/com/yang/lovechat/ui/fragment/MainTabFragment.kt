package com.yang.lovechat.ui.fragment

import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.yang.lovechat.R
import com.yang.lovechat.adapter.UserAdapter
import com.yang.lovechat.base.adapter.TabAndViewPagerFragmentAdapter
import com.yang.lovechat.base.fragment.BaseFragment
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.databinding.FraTabMainBinding
import com.yang.lovechat.databinding.ViewItemTabBinding
import com.yang.lovechat.ui.activity.UserInfoActivity
import com.yang.lovechat.util.createIntent
import com.yang.lovechat.util.getColor
import com.yang.lovechat.util.startActivity
import com.yang.lovechat.viewmodel.MainViewModel

class MainTabFragment : BaseFragment<FraTabMainBinding, MainViewModel>(FraTabMainBinding::inflate) {

    private val mUserAdapter by lazy { UserAdapter() }

    private val titles = arrayOf("Discover",  "Messages")

    private val mFragments = mutableListOf<Fragment>()


    override fun initView() {

        initRecyclerView()

    }

    override fun initData() {


        mFragments.add(UserListFragment())
        mFragments.add(ConversationFragment())

        initViewPager()

        initTabLayout()

    }


    override fun initViewModel() {


    }

    private fun initViewPager() {

        val tabAndViewPagerAdapter = TabAndViewPagerFragmentAdapter(this, mFragments)
        mViewBinding.viewPager.adapter = tabAndViewPagerAdapter
        mViewBinding.viewPager.isUserInputEnabled = true
        mViewBinding.viewPager.offscreenPageLimit = mFragments.size

    }

    private fun initTabLayout() {

        TabLayoutMediator(
            mViewBinding.tabLayout,
            mViewBinding.viewPager,
            true,
            false
        ) { tab, position ->

            val viewItemTabBinding =
                ViewItemTabBinding.inflate(LayoutInflater.from(requireContext()))

            tab.customView = viewItemTabBinding.root

            if (position == 0) {
                viewItemTabBinding.tvTitle.setTextColor(getColor(R.color.white))
            } else {
                viewItemTabBinding.tvTitle.setTextColor(getColor(R.color.white_70))
            }

            viewItemTabBinding.tvTitle.text = titles[position]

            tab.view.setOnLongClickListener {

                return@setOnLongClickListener true
            }
        }.attach()

        mViewBinding.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                if (null == tab) {

                    return
                }
                tab.customView?.let {

                    val mainTabBinding = ViewItemTabBinding.bind(it)

                    mainTabBinding.tvTitle.setTextColor(getColor(R.color.white))
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

                if (null == tab) {

                    return
                }
                tab.customView?.let {

                    val mainTabBinding = ViewItemTabBinding.bind(it)


                    mainTabBinding.tvTitle.setTextColor(getColor(R.color.white_70))
                }

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {



            }

        })

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


}