package com.yang.lovechat.ui.activity


import android.view.View
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.Utils
import com.google.android.flexbox.FlexboxLayoutManager
import com.yang.lovechat.R
import com.yang.lovechat.adapter.CardImageAdapter
import com.yang.lovechat.adapter.InterestAdapter
import com.yang.lovechat.base.activity.BaseActivity
import com.yang.lovechat.base.bus.EventBus
import com.yang.lovechat.base.bus.EventBus.observe
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.MediaInfoData
import com.yang.lovechat.data.PictureData
import com.yang.lovechat.data.UserInfoData
import com.yang.lovechat.databinding.ActUserInfoBinding
import com.yang.lovechat.dialog.MoreMenuDialog
import com.yang.lovechat.dialog.PictureDetailDialog
import com.yang.lovechat.helper.UserInfoHold
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.createIntent
import com.yang.lovechat.util.dip2px
import com.yang.lovechat.util.edgeToEdgeAll
import com.yang.lovechat.util.loadImage
import com.yang.lovechat.util.startActivity
import com.yang.lovechat.util.viewVisibility
import com.yang.lovechat.viewmodel.MineViewModel
import kotlin.jvm.java


class UserInfoActivity :
    BaseActivity<ActUserInfoBinding, MineViewModel>(ActUserInfoBinding::inflate) {

    private val mIndicatorHeight = 4f.dip2px(Utils.getApp())

    private val mBannerRound = 20f.dip2px(Utils.getApp()).toFloat()

    private val mInterestAdapter by lazy { InterestAdapter() }
    private var targetUserId: Long = -1L


    override fun initView() {


        withViewBinding {

            root.edgeToEdgeAll()

            ivBack.clicks {
                finish()
            }

            ivMore.clicks {

                if (targetUserId == UserInfoHold.userId){


                }else{
                    MoreMenuDialog.newInstance(false,targetUserId.toString(),0).show(supportFragmentManager)

                }

            }

            initInterestRecyclerView()
        }


    }


    override fun initData() {

        targetUserId = intent.getLongExtra(AppConstant.Constant.USER_ID, -1L)

        if (targetUserId == UserInfoHold.userId){
            mViewBinding.ivMore.setImageResource(R.drawable.iv_edit_my_info)
        }else{
            mViewBinding.ivMore.setImageResource(R.drawable.iv_more)

            swipModel(3, targetUserId)
        }


        mViewModel.getUserInfo(targetUserId)

    }

    override fun initViewModel() {


        mViewModel.mUserInfoData.observe(this) {

            initUserInfo(it)

        }

        EventBus.with(AppConstant.EventConstant.EVENT_REFRESH_MY_USER_INFO).observe(this){

            mViewModel.getUserInfo(targetUserId)

        }

    }


    private fun initUserInfo(mUserInfoData: UserInfoData) {


        withViewBinding {

            tvName.text = mUserInfoData.userName

            tvLocation.text = mUserInfoData.cityName

            stvIntent.text = "${mUserInfoData.intent}"

            stvBio.text = "${mUserInfoData.bio?:""}"

            stvAge.text = "Age:${mUserInfoData.age}"

            stvConstellation.text = "${mUserInfoData.constellation}"

            stvProfession.text = "${mUserInfoData.profession}"

            stvHeight.text = "Height:${mUserInfoData.height}cm"

            stvWeight.text = "Weight:${mUserInfoData.weight}kg"

            sivAvatar.loadImage(mUserInfoData.avatarUrl ?: R.drawable.iv_avatar)

            initBanner(mUserInfoData.backgroundMediaList)




            viewVisibility(if (mUserInfoData.bio.isNullOrEmpty()) View.GONE else View.VISIBLE,stvBio)

            viewVisibility(if (mUserInfoData.profession.isNullOrEmpty()) View.GONE else View.VISIBLE,stvProfession)

            viewVisibility(if (mUserInfoData.cityName.isNullOrEmpty()) View.GONE else View.VISIBLE,tvLocation)

            viewVisibility(if (mUserInfoData.constellation.isNullOrEmpty()) View.GONE else View.VISIBLE,stvConstellation)


            mInterestAdapter.submitList(mUserInfoData.interest)
        }


    }


    private fun initBanner(
        list: List<MediaInfoData>?
    ) {

        withViewBinding {

            val mIndicatorWidth = mIndicatorHeight * 3

            val mCardImageAdapter = CardImageAdapter(mutableListOf())


            val result = list?.map { map ->

                val pictureData = PictureData()

                pictureData.uri = map.uri

                pictureData.url = map.fileUrl

                pictureData.mediaEnumType = map.mediaEnumType

                pictureData.coverUrl = map.coverUrl

                pictureData
            }?.toMutableList()


            mCardImageAdapter.onItemClick = { position ->

                result?.let {
                    PictureDetailDialog.newInstance(result, position)
                        .show(supportFragmentManager)
                }

            }

            banner
                .setAdapter(mCardImageAdapter, false)
                .isAutoLoop(false)
                .setBannerRound(mBannerRound)
                .setIndicator(mIndicator, false)
                .setIndicatorSelectedColor(ColorUtils.getColor(R.color.white))
                .setIndicatorNormalColor(ColorUtils.getColor(R.color.white_30))
                .setIndicatorHeight(mIndicatorHeight)
                .setIndicatorWidth(mIndicatorWidth, 2 * mIndicatorWidth)
            mCardImageAdapter.setDatas(list)

        }
    }



    private fun initInterestRecyclerView() {

        withViewBinding {

            interestRecyclerView.adapter = mInterestAdapter

            interestRecyclerView.layoutManager = FlexboxLayoutManager(this@UserInfoActivity)


        }

    }




    //0-不喜欢, 1-普通喜欢, 2-付费快速建联 3 查看
    private fun swipModel(swipType: Int,friendUserId: Long) {

        mViewModel.swipUser(swipType, friendUserId)

    }
}