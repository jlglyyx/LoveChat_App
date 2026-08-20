package com.yang.lovechat.ui.fragment

import android.view.View
import com.yang.lovechat.R
import com.yang.lovechat.base.bus.EventBus
import com.yang.lovechat.base.bus.EventBus.observe
import com.yang.lovechat.base.fragment.BaseFragment
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.UserInfoData
import com.yang.lovechat.databinding.FraMineBinding
import com.yang.lovechat.helper.UserInfoHold
import com.yang.lovechat.ui.activity.SettingActivity
import com.yang.lovechat.ui.activity.UserInfoActivity
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.createIntent
import com.yang.lovechat.util.dateFormat
import com.yang.lovechat.util.loadImage
import com.yang.lovechat.util.startActivity
import com.yang.lovechat.util.viewVisibility
import com.yang.lovechat.viewmodel.MainViewModel
import java.util.Date

class MineFragment : BaseFragment<FraMineBinding, MainViewModel>(FraMineBinding::inflate) {



    override fun initView() {

        withViewBinding {

            initRecyclerView()


            viewEdit.clicks {

                createIntent(UserInfoActivity::class.java)
                    .putExtra(AppConstant.Constant.USER_ID, UserInfoHold.userId)
                    .startActivity(requireActivity())
            }
            ivEditInfo.clicks {

            }

            ivSetting.clicks {

                createIntent(SettingActivity::class.java).startActivity(requireActivity())
            }

            sclDiamond.clicks {


            }
            sclVipDesc.clicks {


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



        }


    }


    private fun initRecyclerView() {

        withViewBinding {




        }

    }







}