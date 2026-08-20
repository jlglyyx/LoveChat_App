package com.yang.lovechat.ui.activity

import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.internal.LinkedTreeMap
import com.yang.lovechat.R
import com.yang.lovechat.base.activity.BaseActivity
import com.yang.lovechat.base.adapter.TabAndViewPagerAdapter
import com.yang.lovechat.base.bus.EventBus
import com.yang.lovechat.base.bus.EventBus.observe
import com.yang.lovechat.base.bus.EventBus.postValue
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.InstructionType
import com.yang.lovechat.databinding.ActMainBinding
import com.yang.lovechat.databinding.ViewMainTabBinding
import com.yang.lovechat.helper.FloatMessageHelper
import com.yang.lovechat.helper.IMHelper
import com.yang.lovechat.helper.UserInfoHold
import com.yang.lovechat.im.MessageManager
import com.yang.lovechat.ui.fragment.MainTabFragment
import com.yang.lovechat.ui.fragment.MineFragment
import com.yang.lovechat.util.edgeToEdgeBottom
import com.yang.lovechat.util.edgeToEdgeTop
import com.yang.lovechat.util.showShort
import com.yang.lovechat.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : BaseActivity<ActMainBinding, MainViewModel>(ActMainBinding::inflate) {
    private val mImages = arrayOf(
        R.drawable.iv_tab_main,
        R.drawable.iv_tab_mine
    )


    private val titles = arrayOf("Discover", "Profile")

    private val mFragments = mutableListOf<Fragment>()

    private var lastBackTime = 0L

    private fun onMessageReceive(){


        lifecycleScope.launch {

            MessageManager.normalMessageFlow.collect { (result,message, text) ->

                val instructionType = result.data.instructionType

                val sendId = result.data.sendId

                FloatMessageHelper.showFloatMessage(sendId,message,instructionType)
            }


        }

        lifecycleScope.launch {

            MessageManager.businessMessageFlow.collect { (result,data, text) ->

                when(data.instructionType){

                    InstructionType.READ_CONVERSATION.value ->{

                        val params = data.data as? LinkedTreeMap<*, *>?: return@collect

                        val convId = params["convId"].toString()

                        EventBus.with(AppConstant.EventConstant.EVENT_UPDATE_CONVERSATION_READ_COUNT).postValue(convId)

                        Log.i(TAG, "收到已读消息应答 ${text}")
                    }

                }
            }
        }

    }


    override fun initView() {


        withViewBinding {

            root.edgeToEdgeTop()

            tabLayout.edgeToEdgeBottom()

        }
        onBackPressedClick()

    }

    override fun initData() {

        mFragments.add(MainTabFragment())
        mFragments.add(MineFragment())

        initViewPager()

        initTabLayout()


        IMHelper.startIM(UserInfoHold.userId.toString())


        mViewModel.getAllConversationReadCount()


        mViewModel.checkNewVersion()
    }

    override fun initViewModel() {

        onMessageReceive()

        mViewModel.mAppVersion.observe(this){



        }


        EventBus.with(AppConstant.EventConstant.EVENT_UPDATE_ALL_CONVERSATION_READ_COUNT).observe(this){

            val count = it as? Int?:0

            getTotalUnreadCount(count)


        }

        EventBus.with(AppConstant.EventConstant.EVENT_SET_PAGE).observe(this){


            if (it is Int){

                mViewBinding.viewPager.setCurrentItem(it,false)
            }


        }

    }


    private fun initTabLayout() {

        TabLayoutMediator(
            mViewBinding.tabLayout,
            mViewBinding.viewPager,
            true,
            false
        ) { tab, position ->

            val mainTabBinding =
                ViewMainTabBinding.inflate(LayoutInflater.from(this@MainActivity))

            tab.customView = mainTabBinding.root

            mainTabBinding.ivImage.setImageResource(mImages[position])

            if (position == 0) {
                mainTabBinding.ivImage.imageTintList = ColorStateList.valueOf(getColor(R.color.endColor))
                mainTabBinding.tvTitle.setTextColor(getColor(R.color.endColor))
            } else {
                mainTabBinding.ivImage.imageTintList = ColorStateList.valueOf(getColor(R.color.color_999999))
                mainTabBinding.tvTitle.setTextColor(getColor(R.color.color_999999))
            }

            mainTabBinding.tvTitle.text = titles[position]

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

                    val mainTabBinding = ViewMainTabBinding.bind(it)

                    mainTabBinding.ivImage.imageTintList = ColorStateList.valueOf(getColor(R.color.endColor))

                    mainTabBinding.tvTitle.setTextColor(getColor(R.color.endColor))
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

                if (null == tab) {

                    return
                }
                tab.customView?.let {

                    val mainTabBinding = ViewMainTabBinding.bind(it)

                    mainTabBinding.ivImage.imageTintList = ColorStateList.valueOf(getColor(R.color.color_999999))

                    mainTabBinding.tvTitle.setTextColor(getColor(R.color.color_999999))
                }

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

                if (tab?.position == 0){

                    EventBus.with(AppConstant.EventConstant.EVENT_REFRESH_CARD_LIST).postValue(true)
                }

            }

        })

    }


    private fun initViewPager() {

        val tabAndViewPagerAdapter = TabAndViewPagerAdapter(this, mFragments)
        mViewBinding.viewPager.adapter = tabAndViewPagerAdapter
        mViewBinding.viewPager.isUserInputEnabled = false
        mViewBinding.viewPager.offscreenPageLimit = mFragments.size

    }


    fun getTotalUnreadCount(count: Int) {
        try {

            IMHelper.allReadCount = count

            val tab = mViewBinding.tabLayout.getTabAt(2) ?: return

            tab.customView?.let { view ->

                val mainTabBinding = ViewMainTabBinding.bind(view)

                if (count > 0) {
                    mainTabBinding.stvMessageCount.visibility = View.VISIBLE
                    if (count > 999) {
                        mainTabBinding.stvMessageCount.text = "${999}+"
                    } else {
                        mainTabBinding.stvMessageCount.text = "$count"
                    }

                } else {

                    mainTabBinding.stvMessageCount.visibility = View.GONE

                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun onBackPressedClick() {

        onBackPressedDispatcher.addCallback {
            val currentTimeMillis = System.currentTimeMillis()
            if (currentTimeMillis - lastBackTime > 1000) {
                lastBackTime = currentTimeMillis
                showShort("Press exit again!")
            } else {
                isEnabled = false
                moveTaskToBack(true)
                isEnabled = true
            }

        }

    }


    override fun onDestroy() {


        IMHelper.releaseIM()

        super.onDestroy()
    }


}