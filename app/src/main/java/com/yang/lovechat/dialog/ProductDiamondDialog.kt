package com.yang.lovechat.dialog

import android.animation.AnimatorSet
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import androidx.recyclerview.widget.GridLayoutManager
import com.yang.lovechat.adapter.ProductDiamondAdapter
import com.yang.lovechat.base.dialog.BaseDialog
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.ProductInfoData
import com.yang.lovechat.databinding.DialogProductDiamondBinding
import com.yang.lovechat.helper.UserInfoHold
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.edgeToEdgeBottom
import com.yang.lovechat.util.withAnimate
import com.yang.lovechat.viewmodel.PublicViewModel

class ProductDiamondDialog : BaseDialog<DialogProductDiamondBinding>(DialogProductDiamondBinding::inflate) {


    private val mViewModel by sharedViewModels<PublicViewModel>()

    private val mProductDiamondAdapter: ProductDiamondAdapter by lazy { ProductDiamondAdapter() }
    private var mCurrentPosition = 0

    private var mAnimatorSet:AnimatorSet? = null

    private var mCurrentProduct : ProductInfoData? = null

    companion object {

        fun newInstance(): ProductDiamondDialog {

            return ProductDiamondDialog().apply {

                arguments = Bundle().apply {

                }
            }
        }
    }


    override fun initView() {

        withViewBinding {

//            initPayListener()

            root.edgeToEdgeBottom()


            initRecyclerView()


            ivClose.clicks {


                dismissAllowingStateLoss()
            }


            mAnimatorSet = stvNext.withAnimate()

            stvNext.clicks {

                val item = mProductDiamondAdapter.getItem(mCurrentPosition)

                showLoading(dismissOnBackPressed = false)

                mViewModel.createOrder(item.skuCode)


            }

            tvBalance.text = "Balance: ${UserInfoHold.userInfo?.userWallet?.balance?:0}"


        }

    }

    override fun initData() {

        AppConstant.Constant.isShowBuy = true

        arguments?.let {

        }

        mViewModel.getProductInfoList(AppConstant.Constant.PRODUCT_DIAMOND)



    }


    override fun initViewModel() {
        super.initViewModel()


        mViewModel.mProductData.observe(this){

            mProductDiamondAdapter.submitList(it.productInfo)

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


        mDialogBinding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        mDialogBinding.recyclerView.adapter = mProductDiamondAdapter

        mProductDiamondAdapter.setOnItemClickListener { _, _, position ->

            if (mCurrentPosition == position) {

                mDialogBinding.stvNext.performClick()

                return@setOnItemClickListener
            }

            val item = mProductDiamondAdapter.getItem(position)

            val lastItem = mProductDiamondAdapter.getItem(mCurrentPosition)


            lastItem.isSelect = false

            mProductDiamondAdapter.notifyItemChanged(mCurrentPosition, false)

            item.isSelect = true

            mCurrentPosition = position

            mProductDiamondAdapter.notifyItemChanged(mCurrentPosition, false)

        }


    }



    fun setDefaultProduct(){

        val list = mProductDiamondAdapter.items

        if (list.size >= mCurrentPosition) {

            list[mCurrentPosition].isSelect = true

        }

        mCurrentProduct = mProductDiamondAdapter.getItem(mCurrentPosition)
    }





    private fun initBuySuccessDialog() {

        val item = mProductDiamondAdapter.getItem(mCurrentPosition)

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


//    override fun setDialogHeight(): Int {
//
//        return WindowManager.LayoutParams.MATCH_PARENT
//    }

    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }


    override fun onDismiss(dialog: DialogInterface) {


        mAnimatorSet?.cancel()
        mAnimatorSet = null

        AppConstant.Constant.isShowBuy = false

//        PayManager.removeListener(TAG)

        super.onDismiss(dialog)
    }


}