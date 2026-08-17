package com.yang.lovechat.ui.fragment

import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.yang.lovechat.R
import com.yang.lovechat.base.bus.EventBus
import com.yang.lovechat.base.bus.EventBus.observe
import com.yang.lovechat.base.fragment.BaseFragment
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.databinding.FraLikeBinding
import com.yang.lovechat.databinding.ViewItemTabBinding
import com.yang.lovechat.util.getColor
import com.yang.lovechat.viewmodel.MainViewModel

class LikeFragment: BaseFragment<FraLikeBinding, MainViewModel>(FraLikeBinding::inflate) {

    private val mFragments = mutableListOf<Fragment>()

    private val mTitles = mutableListOf("Who likes me","My Likes","Who Viewed Me")

    override fun initView() {

        withViewBinding {


            initViewPager()

            initTabLayout()
        }

    }

    override fun initData() {
    }

    override fun initViewModel() {

        EventBus.with(AppConstant.EventConstant.EVENT_SET_LIKE_PAGE).observe(this){


            if (it is Int){

                mViewBinding.viewPager.setCurrentItem(it,false)
            }


        }
    }


    private fun initTabLayout() {

        mViewBinding.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                if (null == tab) {

                    return
                }
                tab.customView?.let {

                    val mViewItemTabBinding = ViewItemTabBinding.bind(it)

                    mViewItemTabBinding.tvTitle.setTextColor(getColor(R.color.white))


                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

                if (null == tab) {

                    return
                }
                tab.customView?.let {

                    val mViewItemTabBinding = ViewItemTabBinding.bind(it)

                    mViewItemTabBinding.tvTitle.setTextColor(getColor(R.color.white_70))

                }

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {



            }

        })

    }


    private fun initViewPager() {

        mFragments.add(LikeIFragment())
        mFragments.add(ILikeFragment())
        mFragments.add(ViewedIFragment())

        mViewBinding.apply {


            viewPager.adapter = object : FragmentStateAdapter(this@LikeFragment) {
                override fun getItemCount(): Int {

                    return mFragments.size

                }

                override fun createFragment(position: Int): Fragment {

                    return mFragments[position]

                }

            }

            TabLayoutMediator(
                tabLayout,
                viewPager,
                false,
                true
            ) { tab, position ->

                val mViewItemTabBinding =
                    ViewItemTabBinding.inflate(LayoutInflater.from(requireContext()))

                tab.customView = mViewItemTabBinding.root

                mViewItemTabBinding.tvTitle.text = mTitles[position]

                if (position == 0) {
                    mViewItemTabBinding.tvTitle.setTextColor(getColor(R.color.white))
                } else {
                    mViewItemTabBinding.tvTitle.setTextColor(getColor(R.color.white_70))
                }


                tab.view.setOnLongClickListener {

                    return@setOnLongClickListener true
                }


            }.attach()

            viewPager.isUserInputEnabled = true

            viewPager.offscreenPageLimit = mFragments.size

        }
    }

}