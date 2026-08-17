package com.yang.lovechat.ui.fragment

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.yang.lovechat.R
import com.yang.lovechat.adapter.ILikeAdapter
import com.yang.lovechat.adapter.MineProductPrivilegeAdapter
import com.yang.lovechat.base.bus.EventBus
import com.yang.lovechat.base.bus.EventBus.observe
import com.yang.lovechat.base.fragment.BaseFragment
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.UserInfoData
import com.yang.lovechat.databinding.FraMineBinding
import com.yang.lovechat.dialog.FastConnectDialog
import com.yang.lovechat.helper.ProductHelper
import com.yang.lovechat.helper.UserInfoHold
import com.yang.lovechat.ui.activity.EditUserInfoActivity
import com.yang.lovechat.ui.activity.SettingActivity
import com.yang.lovechat.ui.activity.UserInfoActivity
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.createIntent
import com.yang.lovechat.util.dateFormat
import com.yang.lovechat.util.loadImage
import com.yang.lovechat.util.refreshLoadListener
import com.yang.lovechat.util.startActivity
import com.yang.lovechat.util.viewVisibility
import com.yang.lovechat.viewmodel.MainViewModel
import java.util.Date
import kotlin.jvm.java

class MineFragment : BaseFragment<FraMineBinding, MainViewModel>(FraMineBinding::inflate) {

    private var mFastConnectDialog: FastConnectDialog? = null

    private val mMineProductPrivilegeAdapter: MineProductPrivilegeAdapter by lazy {
        MineProductPrivilegeAdapter()
    }

    private val mILikeAdapter: ILikeAdapter by lazy {
        ILikeAdapter()
    }


    private lateinit var mQuickAdapterHelper: QuickAdapterHelper

    override fun initView() {

        withViewBinding {

            initRecyclerView()

            initExtraRecyclerView()

            viewEdit.clicks {

                createIntent(UserInfoActivity::class.java)
                    .putExtra(AppConstant.Constant.USER_ID, UserInfoHold.userId)
                    .startActivity(requireActivity())
            }
            ivEditInfo.clicks {

                createIntent(EditUserInfoActivity::class.java).startActivity(requireActivity())
            }

            ivSetting.clicks {

                createIntent(SettingActivity::class.java).startActivity(requireActivity())
            }

            sclDiamond.clicks {

                ProductHelper.showPayProductDialog(this@MineFragment,AppConstant.Constant.PRODUCT_DIAMOND)

            }
            sclVipDesc.clicks {


                ProductHelper.showPayProductDialog(this@MineFragment,AppConstant.Constant.PRODUCT_VIP)
            }
        }

    }

    override fun initData() {


        withViewBinding {

            UserInfoHold.userInfo?.let {
                initUserInfo(it)
            }


        }


    }

    override fun onResume() {
        super.onResume()

        UserInfoHold.userId?.let {
            mViewModel.getUserInfo(it)

            mViewModel.getRecommendUserList()

            mViewModel.getProductInfoList(AppConstant.Constant.PRODUCT_VIP)

        }

    }

    override fun initViewModel() {

        mViewModel.mUserInfoData.observe(this) {

            initUserInfo(it)
        }

        EventBus.with(AppConstant.EventConstant.EVENT_UPDATE_USER_INFO_NOW).observe(this) {

            if (it is UserInfoData){

                initUserInfo(it)

            }
        }

        mViewModel.mRecommendUserListData.observe(this) {


            mViewBinding.tv4.isVisible = it.isNotEmpty()

            mILikeAdapter.submitList(it)

        }

        EventBus.with(AppConstant.EventConstant.EVENT_FAST_CONNECT_SUCCESS).observe(this){

            val item = mILikeAdapter.items.findLast { find -> find.id == it }

            item?.let {

                mILikeAdapter.remove(item)

                mViewBinding.tv4.isVisible = mILikeAdapter.items.isNotEmpty()

            }
        }

    }

    private fun initUserInfo(mUserInfoData: UserInfoData) {

        withViewBinding {

            tvName.text = mUserInfoData.userName

            tvId.text = "ID:${mUserInfoData.id}"

            ivAvatar.loadImage(UserInfoHold.userInfo?.avatarUrl?:R.drawable.iv_avatar)

            val userWallet = mUserInfoData.userWallet?:return

            tvDiamond.text = "${userWallet.balance}"

            if (mUserInfoData.vipStatus){

                tvExpiredTime.text = "ExpiredTime: ${Date(userWallet.vipExpireTime).dateFormat("MM/dd/yyyy")}"

                viewVisibility(View.VISIBLE,ivVipTag)

            }else{
                tvExpiredTime.text = "Unlock all features and get more matches!"

                viewVisibility(View.GONE,ivVipTag)
            }

            val productInfoCache = mViewModel.getProductInfoCache(AppConstant.Constant.PRODUCT_VIP)


            mMineProductPrivilegeAdapter.submitList(productInfoCache?.productPrivilegeList?.take(5))
        }


    }


    private fun initRecyclerView() {

        withViewBinding {

            recyclerView.adapter = mMineProductPrivilegeAdapter



        }

    }


    private fun initExtraRecyclerView() {


        withViewBinding {

            mQuickAdapterHelper = mILikeAdapter.refreshLoadListener(onLoad = {

                mViewModel.getRecommendUserList()
            })

            extraRecyclerView.adapter = mQuickAdapterHelper.adapter


            extraRecyclerView.setRecycledViewPool(RecyclerView.RecycledViewPool())

            extraRecyclerView.itemAnimator = null

            extraRecyclerView.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)



            mILikeAdapter.setOnDebouncedItemClick { _, _, position ->

                val item = mILikeAdapter.getItem(position)

                initFastConnectDialog(item)

            }

        }


    }


    private fun initFastConnectDialog(item: UserInfoData) {

        mFastConnectDialog?.dismissAllowingStateLoss()

        mFastConnectDialog =  FastConnectDialog.newInstance(item)

        mFastConnectDialog?.show(parentFragmentManager)
    }

}