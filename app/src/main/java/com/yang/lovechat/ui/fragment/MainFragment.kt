package com.yang.lovechat.ui.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.yang.lovechat.R
import com.yang.lovechat.adapter.CardAdapter
import com.yang.lovechat.base.bus.EventBus
import com.yang.lovechat.base.bus.EventBus.observe
import com.yang.lovechat.base.bus.EventBus.postValue
import com.yang.lovechat.base.fragment.BaseFragment
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.UserInfoData
import com.yang.lovechat.databinding.FraMainBinding
import com.yang.lovechat.databinding.ItemCardBinding
import com.yang.lovechat.databinding.ViewEmptyLikeBinding
import com.yang.lovechat.databinding.ViewNoNetworkBinding
import com.yang.lovechat.dialog.FastConnectDialog
import com.yang.lovechat.helper.ModelTouchHelper
import com.yang.lovechat.helper.ProductHelper
import com.yang.lovechat.http.IHttpException
import com.yang.lovechat.ui.activity.UserInfoActivity
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.createIntent
import com.yang.lovechat.util.mTranslation
import com.yang.lovechat.util.startActivity
import com.yang.lovechat.viewmodel.MainViewModel
import com.yang.lovechat.widget.CardLayoutManager
import com.yang.lovechat.widget.ErrorReLoadView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainFragment : BaseFragment<FraMainBinding, MainViewModel>(FraMainBinding::inflate) {

    private val mCardAdapter by lazy { CardAdapter() }

    private lateinit var mItemTouchHelper: ItemTouchHelper

    private var currentStateImage = -2

    private var currentUserId: Long? = null


    private var animatorSet: AnimatorSet? = AnimatorSet()

    private var layoutManager: CardLayoutManager? = null

    private lateinit var mTranslationAnimator: ObjectAnimator

    private var isRefresh = true

    private var storageUser: UserInfoData? = null

    private var mFastConnectDialog: FastConnectDialog? = null

    private val mModelTouchHelper: ModelTouchHelper by lazy {

        ModelTouchHelper()
    }


    override fun initView() {

        initRecyclerView()

    }

    override fun initData() {

        mViewModel.getRecommendUserList()

    }

    override fun onResume() {
        super.onResume()


    }

    override fun initViewModel() {

        mViewModel.mRecommendUserListData.observe(this) {


            if (isRefresh) {
                mCardAdapter.submitList(it)

            } else {

                val map = mCardAdapter.items.map { item -> item.id }.toSet()

                val list = it.filter { item -> !map.contains(item.id) }

                mCardAdapter.addAll(list)
            }

            mViewBinding.errorReLoadView.showSuccessView(mCardAdapter.items)

        }

        mViewModel.mSwipUserErrorStatus.observe(this) {

            val storageUser = storageUser

            if (null != storageUser){

                mCardAdapter.add(0, storageUser)

                this@MainFragment.storageUser = null

            }

            if (it is IHttpException.HttpErrorException){

                ProductHelper.showPayProductErrorDialog(this,it.code)

            }

        }




        EventBus.with(AppConstant.EventConstant.EVENT_REFRESH_CARD_LIST).observe(this){

            isRefresh = true

            mViewModel.getRecommendUserList()

        }

        mViewModel.requestExceptionEvent.observe(this) {


            mViewBinding.errorReLoadView.showStatusView(ErrorReLoadView.Status.NO_NETWORK)


        }



        EventBus.with(AppConstant.EventConstant.EVENT_FAST_CONNECT_SUCCESS).observe(this){

            val item = mCardAdapter.items.findLast { find -> find.id == it }

            item?.let {

                mCardAdapter.remove(item)

                mViewBinding.errorReLoadView.showSuccessView(mCardAdapter.items)

            }
        }




    }


    private fun initRecyclerView() {

        withViewBinding {

            recyclerView.adapter = mCardAdapter

            layoutManager = CardLayoutManager()

            recyclerView.layoutManager = layoutManager

            recyclerView.setItemViewCacheSize(10)

            recyclerView.setRecycledViewPool(mCardAdapter.sharedPool)

            recyclerView.animation = null


            errorReLoadView.addEmptyView { viewGroup ->
                ViewEmptyLikeBinding.inflate(
                    LayoutInflater.from(requireContext()),
                    viewGroup,
                    true
                )
                    .apply {

                        tvTitle.text = "No more people to display"

                        tvDesc.text = "You’ve viewed all recommended people for now. "

                        stvConfirm.text = "See My Likes"

                        stvConfirm.clicks {

                            EventBus.with(AppConstant.EventConstant.EVENT_SET_PAGE).postValue(1)

                            EventBus.with(AppConstant.EventConstant.EVENT_SET_LIKE_PAGE).postValue(1)
                        }

                    }
            }

            errorReLoadView.addNoNetView { viewGroup ->
                ViewNoNetworkBinding.inflate(LayoutInflater.from(requireContext()), viewGroup, true)
                    .apply {

                        stvConfirm.clicks {

                            isRefresh = true

                            mViewModel.getRecommendUserList()
                        }
                    }
            }


            mCardAdapter.addOnDebouncedChildClick(R.id.cl_info) { _, _, position ->

                val item = mCardAdapter.getItem(position)


                createIntent(UserInfoActivity::class.java)
                    .putExtra(AppConstant.Constant.USER_ID, item.id)
                    .startActivity(requireActivity())

            }
            mCardAdapter.addOnDebouncedChildClick(R.id.ll_connect) { _, _, position ->

                val item = mCardAdapter.getItem(position)


                initFastConnectDialog(item)



            }

            mCardAdapter.addOnDebouncedChildClick(R.id.ll_dislike) { _, _, position ->

                val item = mCardAdapter.getItem(position)

                currentUserId = item.id


                likeAnimate(false)
            }

            mCardAdapter.addOnDebouncedChildClick(R.id.ll_like) { _, _, position ->

                val item = mCardAdapter.getItem(position)

                currentUserId = item.id


                likeAnimate(true)
            }


            initTouchHelper()
        }
    }


    private fun initTouchHelper() {


        mItemTouchHelper = ItemTouchHelper(mModelTouchHelper.apply {


            onShowImage = { type, dX ->

                onShowImage(type)
            }

            onMove = {


            }
            onReset = {


            }

            onSwiped = { position ->


                if (mCardAdapter.itemCount > 0) {

                    currentUserId = mCardAdapter.items.first().id

                    storageUser = mCardAdapter.items.first()

                    mCardAdapter.removeAt(position)

                    mViewBinding.errorReLoadView.showSuccessView(mCardAdapter.items)
                }


            }

            onLike = { position ->

                swipModel(1)

            }

            onDisLike = { position ->

                swipModel(0)
            }

        })


        mItemTouchHelper.attachToRecyclerView(mViewBinding.recyclerView)
    }


    fun onShowImage(type: Int) {

        if (currentStateImage == type) return

        currentStateImage = type


        val layoutManager = layoutManager ?: return


        val findViewByPosition = layoutManager.findViewByPosition(0) ?: return


        val mItemCardBinding = ItemCardBinding.bind(findViewByPosition)


        val ivLikeStatus = mItemCardBinding.llLike

        val ivDisLikeStatus = mItemCardBinding.llDislike

        val ivConnectStatus = mItemCardBinding.llConnect


        showStateImage(type, ivLikeStatus, ivDisLikeStatus, ivConnectStatus)
    }

    private fun showStateImage(
        it: Int,
        ivLikeStatus: LinearLayout,
        ivDisLikeStatus: LinearLayout,
        ivConnectStatus: LinearLayout,
    ) {


        when (it) {
            0 -> {

                ivDisLikeStatus.alpha = 1f
                ivConnectStatus.alpha = 1f
                ivLikeStatus.alpha = 1f
            }

            -1 -> {

                ivLikeStatus.alpha = 0.3f
                ivConnectStatus.alpha = 0.3f
                ivDisLikeStatus.alpha = 1f

            }

            else -> {

                ivDisLikeStatus.alpha = 0.3f
                ivConnectStatus.alpha = 0.3f
                ivLikeStatus.alpha = 1f
            }
        }

    }


    private fun likeAnimate(like: Boolean) {

        if (!mModelTouchHelper.isSwipeEnabled) {

            return
        }
        animatorSet?.cancel()

        animatorSet?.removeAllListeners()

        animatorSet = null

        animatorSet = AnimatorSet()



        val layoutManager = layoutManager ?: return

        val findViewByPosition = layoutManager.findViewByPosition(0) ?: return


        val mItemCardBinding = ItemCardBinding.bind(findViewByPosition)


        val group = mItemCardBinding.root

        val ivLikeStatus = mItemCardBinding.llLike

        val ivDisLikeStatus = mItemCardBinding.llDislike

        val ivConnectStatus = mItemCardBinding.llConnect


        group.let {

            mTranslationAnimator = it.mTranslation(View.TRANSLATION_X, 0f, if (like) 100f else -100f)

            animatorSet?.setDuration(100)

            animatorSet?.playTogether(mTranslationAnimator)

            animatorSet?.addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)

                    mModelTouchHelper.isSwipeEnabled = false

                    showStateImage(
                        if (like) 1 else -1,
                        ivLikeStatus,
                        ivDisLikeStatus,
                        ivConnectStatus
                    )
                }

                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)

                    mModelTouchHelper.isSwipeEnabled = true
                }

                override fun onAnimationEnd(animation: Animator) {

                    super.onAnimationEnd(animation)

                    mModelTouchHelper.isSwipeEnabled = true

                    swipModel(if (like)  1 else 0)

                    if (mCardAdapter.itemCount > 0) {
                        storageUser = mCardAdapter.items.first()
                        mCardAdapter.removeAt(0)
                        mViewBinding.errorReLoadView.showSuccessView(mCardAdapter.items)
                    }

                    lifecycleScope.launch {

                        delay(200)

                        showStateImage(0, ivLikeStatus, ivDisLikeStatus, ivConnectStatus)
                    }

                    animatorSet?.removeAllListeners()
                }
            })

            animatorSet?.start()
        }
    }

    //0-不喜欢, 1-普通喜欢, 2-付费快速建联 3 查看
    private fun swipModel(swipType: Int) {

        val currentUserId = currentUserId ?: return

        mViewModel.swipUser(swipType, currentUserId)

        needLoadNext()
    }



    private fun needLoadNext() {

        if (mCardAdapter.itemCount == 3) {

            lifecycleScope.launch {

                delay(500)

                isRefresh = false

                mViewModel.getRecommendUserList()
            }


        }

    }

    private fun initFastConnectDialog(item: UserInfoData) {

        mFastConnectDialog?.dismissAllowingStateLoss()

        mFastConnectDialog =  FastConnectDialog.newInstance(item).apply {
            onConfirm = {

                currentUserId = item.id

            }

            onConnectError = {

                val storageUser = storageUser

                if (null != storageUser){

                    mCardAdapter.add(0, storageUser)

                    this@MainFragment.storageUser = null

                }
            }
        }
        mFastConnectDialog?.show(parentFragmentManager)
    }
}