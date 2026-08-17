package com.yang.lovechat.dialog

import android.animation.AnimatorSet
import android.content.DialogInterface
import android.os.Bundle
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SpanUtils
import com.yang.lovechat.R
import com.yang.lovechat.adapter.ProductPrivilegeAdapter
import com.yang.lovechat.adapter.ProductVipAdapter
import com.yang.lovechat.base.bus.EventBus
import com.yang.lovechat.base.bus.EventBus.postValue
import com.yang.lovechat.base.dialog.BaseDialog
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.ProductInfoData
import com.yang.lovechat.databinding.DialogProductVipBinding
import com.yang.lovechat.ui.activity.WebActivity
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.createIntent
import com.yang.lovechat.util.edgeToEdgeAll
import com.yang.lovechat.util.startActivity
import com.yang.lovechat.util.withAnimate
import com.yang.lovechat.viewmodel.PublicViewModel

class ProductVipDialog : BaseDialog<DialogProductVipBinding>(DialogProductVipBinding::inflate) {


    private val mViewModel by sharedViewModels<PublicViewModel>()


    private val mProductVipAdapter: ProductVipAdapter by lazy { ProductVipAdapter() }

    private val mProductPrivilegeAdapter: ProductPrivilegeAdapter by lazy { ProductPrivilegeAdapter() }


    private var mAnimatorSet:AnimatorSet? = null

    private var mCurrentPosition = 0

    private var mCurrentProduct : ProductInfoData? = null


    companion object {

        fun newInstance(): ProductVipDialog {

            return ProductVipDialog().apply {

                arguments = Bundle().apply {

                }
            }
        }
    }


    override fun initView() {

        withViewBinding {

//            initPayListener()

            root.edgeToEdgeAll()


            initRecyclerView()

            initPrivilegeRecyclerView()


            SpanUtils.with(tvPrivacy)
                .append("Terms of Service")
                .setClickSpan(requireContext().getColor(R.color.color_999999), true) {
                    requireContext().createIntent(WebActivity::class.java)
                        .putExtra(
                            AppConstant.Constant.URL,
                            AppConstant.ClientInfo.BASE_SERVICE_POLICY_URL
                        )
                        .putExtra(AppConstant.Constant.TITLE, "User Agreement")
                        .startActivity(requireContext())
                }
                .append("  &  ")
                .append("Privacy Policy.")
                .setClickSpan(requireContext().getColor(R.color.color_999999), true) {
                    requireContext().createIntent(WebActivity::class.java)
                        .putExtra(
                            AppConstant.Constant.URL,
                            AppConstant.ClientInfo.BASE_PRIVACY_POLICY_URL
                        )
                        .putExtra(AppConstant.Constant.TITLE, "Privacy Policy")
                        .startActivity(requireContext())
                }
                .create()



            ivClose.clicks {


                dismissAllowingStateLoss()
            }

            mAnimatorSet = stvNext.withAnimate()

            stvNext.clicks {

                val item = mProductVipAdapter.getItem(mCurrentPosition)

                showLoading(dismissOnBackPressed = false)

                mViewModel.createOrder(item.skuCode)


            }


        }

    }

    override fun initData() {

        AppConstant.Constant.isShowBuy = true

        arguments?.let {

        }

        mViewModel.getProductInfoList(AppConstant.Constant.PRODUCT_VIP)



    }


    override fun initViewModel() {
        super.initViewModel()


        mViewModel.mProductData.observe(this){

            mProductVipAdapter.submitList(it.productInfo)

            mProductPrivilegeAdapter.submitList(it.productPrivilegeList)

            setDefaultProduct()

        }

        mViewModel.mOrderNoData.observe(this){

            if (AppConstant.ClientInfo.OPEN_GOOGLE) {

//                it.productId?.let { productId ->
//
//                    PayManager.queryProduct(
//                        requireActivity(),
//                        TAG,
//                        productId,
//                        it.purchaseToken, UserInfoHelper.userId
//                    )
//                }

            } else {

                mViewModel.payOrderTest(it)
            }

        }

        mViewModel.mPayOrderStatusData.observe(this){

            if (AppConstant.ClientInfo.OPEN_GOOGLE) {


            }else{

                dismissLoading()

            }

            initBuySuccessDialog()

        }

    }


    private fun initRecyclerView() {


        mDialogBinding.recyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.HORIZONTAL, false
        )

        mDialogBinding.recyclerView.adapter = mProductVipAdapter

        mProductVipAdapter.setOnItemClickListener { _, _, position ->

            if (mCurrentPosition == position) {

                mDialogBinding.stvNext.performClick()

                return@setOnItemClickListener
            }

            val item = mProductVipAdapter.getItem(position)

            val lastItem = mProductVipAdapter.getItem(mCurrentPosition)


            lastItem.isSelect = false

            mProductVipAdapter.notifyItemChanged(mCurrentPosition, false)

            item.isSelect = true

            mCurrentPosition = position

            getDesc(item)

            mProductVipAdapter.notifyItemChanged(mCurrentPosition, false)

        }


    }


    private fun initPrivilegeRecyclerView() {


        withViewBinding {

            privilegeRecyclerView.adapter = mProductPrivilegeAdapter

            privilegeRecyclerView.layoutManager = LinearLayoutManager(context)


        }

    }


    fun setDefaultProduct(){

        val list = mProductVipAdapter.items

        if (list.size >= mCurrentPosition) {

            list[mCurrentPosition].isSelect = true

            getDesc(list[mCurrentPosition])
        }

        mCurrentProduct = mProductVipAdapter.getItem(mCurrentPosition)
    }


    private fun getDesc(mProductInfoData: ProductInfoData?) {

        if (null == mProductInfoData) return

        SpanUtils.with(mDialogBinding.tvDesc)
            .append("Your subscription will automatically renew every ${mProductInfoData.productName} for ${if (null == mProductInfoData.priceAmount) "$${mProductInfoData.priceAmount}" else "$${mProductInfoData.priceAmount}"}.")
            .append(" Cancel  anytime")
            .setBold()
            .setForegroundColor(requireContext().getColor(R.color.white))
            .append(" on your Google Play. For more information, visit our:").create()

    }


    private fun initBuySuccessDialog() {

        EventBus.with(AppConstant.EventConstant.EVENT_BUY_VIP_SUCCESS).postValue(true)

        val item = mProductVipAdapter.getItem(mCurrentPosition)

        BuyProductStatusDialog.newInstance(item.productName).show(parentFragmentManager)

        dismissAllowingStateLoss()


    }


//    private fun initPayListener() {
//
//        PayManager.addListener(TAG, object : PayManager.GooglePayListener {
//
//            override fun onError(code: Int, data: String, orderId: String?) {
//
//                mViewModel.payFail(bizId, orderId, "[${code}] $data")
//
//                dismissLoading()
//            }
//
//            override fun onClientSuccess() {
//
//            }
//
//
//            override fun onPaySuccess(mPurchase: Purchase, type: String, lastOrderId: Int?) {
//
//
//                if (null == bizId) {
//
//                    if (null != lastOrderId && lastOrderId != -1) {
//
//                        mViewModel.payOrderGoogle(
//                            lastOrderId,
//                            currentTplData?.money ?: "0.0",
//                            mPurchase,
//                            type
//                        )
//                    }
//
//                } else {
//
//                    mViewModel.payOrderGoogle(bizId!!, currentTplData?.money ?: "0.0", mPurchase, type)
//
//                    bizId = null
//
//                }
//
//
//            }
//
//            override fun onHandlePurchaseSuccess(mPurchase: Purchase, type: String) {
//
//                lifecycleScope.launch(Dispatchers.Main) {
//
//                    dismissLoading()
//
//                    initBuySuccessDialog()
//                }
//
//            }
//
//
//        })
//
//
//    }


    override fun setDialogHeight(): Int {

        return WindowManager.LayoutParams.MATCH_PARENT
    }


    override fun onDismiss(dialog: DialogInterface) {

        mAnimatorSet?.cancel()
        mAnimatorSet = null


        AppConstant.Constant.isShowBuy = false


//        PayManager.removeListener(TAG)

        super.onDismiss(dialog)
    }


}