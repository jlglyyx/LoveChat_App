package com.yang.lovechat.dialog

import android.animation.Animator
import android.animation.AnimatorSet
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.Utils
import com.chad.library.adapter4.animation.ItemAnimator
import com.yang.lovechat.R
import com.yang.lovechat.adapter.CardImageAdapter
import com.yang.lovechat.adapter.ConnectMessageAdapter
import com.yang.lovechat.base.dialog.BaseDialog
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.ConnectMessageData
import com.yang.lovechat.data.MediaInfoData
import com.yang.lovechat.data.UserInfoData
import com.yang.lovechat.databinding.DialogFastConnectBinding
import com.yang.lovechat.helper.ProductHelper
import com.yang.lovechat.helper.UserInfoHold
import com.yang.lovechat.http.IHttpException
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.dip2px
import com.yang.lovechat.util.edgeToEdgeBottom
import com.yang.lovechat.util.edgeToEdgeTop
import com.yang.lovechat.util.getParcelableData
import com.yang.lovechat.util.mAlpha
import com.yang.lovechat.util.mScale
import com.yang.lovechat.util.mTranslation
import com.yang.lovechat.util.withAnimate
import com.yang.lovechat.viewmodel.PublicViewModel

class FastConnectDialog :
    BaseDialog<DialogFastConnectBinding>(DialogFastConnectBinding::inflate) {

    private val mViewModel by sharedViewModels<PublicViewModel>()


    var onConfirm: (() -> Unit)? = null

    var onConnectError: (() -> Unit)? = null

    private var mUserInfoData: UserInfoData? = null


    private val mIndicatorHeight = 4f.dip2px(Utils.getApp())

    private val mBannerRound = 20f.dip2px(Utils.getApp()).toFloat()

    private val mConnectMessageAdapter by lazy { ConnectMessageAdapter() }

    private var mAnimatorSet:AnimatorSet? = null


    val list1 = listOf(
        "Hi there _userName.",
        "Hello to you _userName.",
        "Good day _userName.",
        "Hey _userName, nice to see your message.",
        "Greetings _userName.",
        "A warm hello to you _userName.",
        "Hi _userName, I noticed your message.",
        "Hello and welcome _userName.",
        "Hey there _userName.",
        "Sending a simple hello to you _userName."
    )

    val list2 = listOf(
        "You can call me _userName. I'm _age years old.",
        "Feel free to call me _userName, and I am _age years old.",
        "You may address me as _userName. I'm _age this year.",
        "My name is _userName, and I'm currently _age years old.",
        "Please call me _userName. I have just turned _age.",
        "I go by the name _userName, and I'm _age years of age.",
        "You can refer to me as _userName, I'm _age years old.",
        "Let me introduce myself: I'm _userName and I'm _age.",
        "People usually call me _userName, I'm _age years old.",
        "You can just call me _userName. I'm _age as of now."
    )

    val list3 = listOf(
        "It's nice to receive your greeting.",
        "I'm glad you decided to say hi.",
        "Your greeting makes me feel warm.",
        "Thanks for taking the time to message me.",
        "It's lovely to get a hello from you.",
        "I appreciate you reaching out to me.",
        "Happy to see you send me a greeting.",
        "Your little hello is quite sweet.",
        "Nice of you to drop a message here.",
        "I'm pleased with your friendly hello."
    )

    val list4 = listOf(
        "Feel free to start our conversation.",
        "You can share anything you'd like to talk about.",
        "Go ahead and tell me what you want to chat about.",
        "I'm ready whenever you want to begin talking.",
        "Don't hesitate to bring up any topic you like.",
        "You're welcome to start whatever topic you prefer.",
        "Just say whatever you feel like talking about.",
        "It's your turn to lead our little chat.",
        "Feel free to say what's on your mind.",
        "Start the chat with anything you want."
    )


    companion object {
        fun newInstance(
            data: UserInfoData,
        ): FastConnectDialog {
            return FastConnectDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(AppConstant.Constant.DATA, data)
                }
            }
        }
    }


    override fun initView() {

        withViewBinding {

            llClose.edgeToEdgeTop()

            root.edgeToEdgeBottom()

            tvPrice.text = "${AppConstant.Constant.connectPrice}"

            initRecyclerView()

            mAnimatorSet = stvSend.withAnimate()

            ivClose.clicks {

                dismissAllowingStateLoss()
            }

            stvSend.clicks {

                mUserInfoData?.id?.let { userId ->
                    mViewModel.swipUser(2,userId)
                }

                onConfirm?.invoke()


            }

        }

    }

    override fun initData() {

        arguments?.let {


            mUserInfoData = it.getParcelableData<UserInfoData>(AppConstant.Constant.DATA)


            withViewBinding {

                mUserInfoData?.let { mUserInfoData ->

                    initBanner(mUserInfoData.backgroundMediaList)


                    val avatarUrl = mUserInfoData.avatarUrl

                    val userName = UserInfoHold.userInfo?.userName ?: ""

                    val age =
                        if (null == UserInfoHold.userInfo?.age || UserInfoHold.userInfo?.age == 0) {
                            ""
                        } else {
                            UserInfoHold.userInfo?.age.toString()
                        }

                    val myName = mUserInfoData.userName

                    val first = list1.random().replace("_userName", userName)

                    val second = list2.random().replace("_userName", myName).replace("_age", age)

                    val third = list3.random()

                    val fourth = list4.random()

                    val connectMessageData = mutableListOf<ConnectMessageData>()

                    connectMessageData.add(ConnectMessageData().apply {
                        this.avatarUrl = avatarUrl
                        this.message = first
                    })
                    connectMessageData.add(ConnectMessageData().apply {
                        this.avatarUrl = avatarUrl
                        this.message = second
                    })
                    connectMessageData.add(ConnectMessageData().apply {
                        this.avatarUrl = avatarUrl
                        this.message = third
                    })
                    connectMessageData.add(ConnectMessageData().apply {
                        this.avatarUrl = avatarUrl
                        this.message = fourth
                    })

                    mConnectMessageAdapter.submitList(connectMessageData)


                }

            }


        }

    }


    override fun initViewModel() {
        super.initViewModel()

        mViewModel.mSwipUserData.observe(this) {



            dismissAllowingStateLoss()

        }

        mViewModel.mSwipUserErrorStatus.observe(this) {

            if (it is IHttpException.HttpErrorException){

                ProductHelper.showPayProductErrorDialog(this,it.code)

            }

            onConnectError?.invoke()

        }
    }

    private fun initRecyclerView() {

        withViewBinding {

            messageRecyclerView.adapter = mConnectMessageAdapter

            mConnectMessageAdapter.isAnimationFirstOnly = false

            mConnectMessageAdapter.itemAnimation = object : ItemAnimator {
                override fun animator(view: View): Animator {
                    view.alpha = 0f
                    val position = view.tag as? Int ?: 0

                    val scaleX = view.mScale(View.SCALE_X, 0.8f, 1f)
                    val scaleY = view.mScale(View.SCALE_Y, 0.8f, 1f)
                    val alpha = view.mAlpha(0f, 1f)
                    val translationY = view.mTranslation(View.TRANSLATION_Y, 100f, 0f)

                    val set = AnimatorSet()
                    set.playTogether(scaleX, scaleY, alpha, translationY)
                    set.duration = 300L
                    set.startDelay = position * 800L
                    set.interpolator = DecelerateInterpolator()
                    return set
                }

            }
        }
    }


    private fun initBanner(
        list: List<MediaInfoData>?
    ) {

        withViewBinding {

            val mIndicatorWidth = mIndicatorHeight * 3

            val mCardImageAdapter = CardImageAdapter(mutableListOf())

            banner
                .setAdapter(mCardImageAdapter)
                .isAutoLoop(true)
                .setBannerRound(mBannerRound)
                .setCurrentItem(0)
                .setIndicator(mIndicator, false)
                .setIndicatorSelectedColor(ColorUtils.getColor(R.color.white))
                .setIndicatorNormalColor(ColorUtils.getColor(R.color.white_30))
                .setIndicatorHeight(mIndicatorHeight)
                .setIndicatorWidth(mIndicatorWidth, 2 * mIndicatorWidth)
            mCardImageAdapter.setDatas(list)
        }
    }


    override fun setDialogHeight(): Int {
        return WindowManager.LayoutParams.MATCH_PARENT
    }


    override fun onDismiss(dialog: DialogInterface) {
        mAnimatorSet?.cancel()
        mAnimatorSet = null
        super.onDismiss(dialog)
    }

}