package com.yang.lovechat.helper

import com.yang.lovechat.base.activity.BaseActivity
import com.yang.lovechat.base.dialog.BaseDialog
import com.yang.lovechat.base.fragment.BaseFragment
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.ResultEnum
import com.yang.lovechat.dialog.ProductDiamondDialog
import com.yang.lovechat.dialog.ProductVipDialog
import com.yang.lovechat.util.isAlive
import java.lang.ref.WeakReference

object ProductHelper {


    fun <T:BaseActivity<*,*>> showPayProductDialog(activity: T, productType: Int){

        val mActivity =  WeakReference(activity).get()?:return

        if (!mActivity.lifecycle.isAlive()) return


        if (productType == AppConstant.Constant.PRODUCT_VIP){

            ProductVipDialog.newInstance().show(mActivity.supportFragmentManager)

        }else{

            ProductDiamondDialog.newInstance().show(mActivity.supportFragmentManager)
        }

    }

    fun <T: BaseFragment<*,*>> showPayProductDialog(fragment: T, productType: Int){

        val mFragment =  WeakReference(fragment).get()?:return

        if (!mFragment.lifecycle.isAlive()) return


        if (productType == AppConstant.Constant.PRODUCT_VIP){

            ProductVipDialog.newInstance().show(mFragment.parentFragmentManager)

        }else{

            ProductDiamondDialog.newInstance().show(mFragment.parentFragmentManager)
        }

    }


    fun <T:BaseActivity<*,*>> showPayProductErrorDialog(activity: T, code: Int){

        val mActivity =  WeakReference(activity).get()?:return

        if (!mActivity.lifecycle.isAlive()) return


        when(code){

            ResultEnum.NEED_VIP_PERMISSION_ERROR.code ->{
                ProductVipDialog.newInstance().show(mActivity.supportFragmentManager)
            }
            ResultEnum.NEED_DIAMOND_ERROR.code ->{
                ProductDiamondDialog.newInstance().show(mActivity.supportFragmentManager)
            }
        }


    }

    fun <T: BaseFragment<*,*>> showPayProductErrorDialog(fragment: T, code: Int){

        val mFragment =  WeakReference(fragment).get()?:return

        if (!mFragment.lifecycle.isAlive()) return

        when(code){

            ResultEnum.NEED_VIP_PERMISSION_ERROR.code ->{
                ProductVipDialog.newInstance().show(mFragment.parentFragmentManager)
            }
            ResultEnum.NEED_DIAMOND_ERROR.code ->{
                ProductDiamondDialog.newInstance().show(mFragment.parentFragmentManager)
            }
        }

    }

    fun <T: BaseDialog<*>> showPayProductErrorDialog(fragment: T, code: Int){

        val mFragment =  WeakReference(fragment).get()?:return

        if (!mFragment.lifecycle.isAlive()) return

        when(code){

            ResultEnum.NEED_VIP_PERMISSION_ERROR.code ->{
                ProductVipDialog.newInstance().show(mFragment.parentFragmentManager)
            }
            ResultEnum.NEED_DIAMOND_ERROR.code ->{
                ProductDiamondDialog.newInstance().show(mFragment.parentFragmentManager)
            }
        }

    }

}