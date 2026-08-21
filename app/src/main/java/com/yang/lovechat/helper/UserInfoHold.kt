package com.yang.lovechat.helper


import android.content.Context
import android.content.Intent
import com.yang.lovechat.app.BaseApplication
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.UserInfoData
import com.yang.lovechat.ui.activity.LoginActivity
import com.yang.lovechat.util.clearAllCache
import com.yang.lovechat.util.createIntent
import com.yang.lovechat.util.fromJson
import com.yang.lovechat.util.getCache
import com.yang.lovechat.util.setCache
import com.yang.lovechat.util.toJson

import kotlin.jvm.java

object UserInfoHold {

    val userInfo: UserInfoData?
        get() = getUserInfoData()


    val userId: Long?
        get() =userInfo?.id

    var currentUserId: Long = -1



    val isVip: Boolean
        get() = getIsVip()

    val isReview :Boolean
        get() = getCache(AppConstant.Constant.IS_REVIEW_VERSION,false)

    private fun getIsVip():Boolean{

        if (null == userInfo) return false

        return userInfo!!.vipStatus
    }

    private fun getUserInfoData(): UserInfoData?{

        val cache = getCache(AppConstant.Constant.USER_INFO, "")

        if (cache == ""){

            return null
        }
        return cache.fromJson<UserInfoData>()

    }


    fun updateLocalUserInfo(userInfoData: UserInfoData) {
        setCache(AppConstant.Constant.USER_INFO, userInfoData.toJson())
    }




    fun loginOut(context: Context = BaseApplication.mApplication) {

        IMHelper.releaseIM()

//
//        RIMClient.logout()
//
//        GooglePayManager.disClient()
//
//        GoogleLoginUtil().googleLogOut(context)
//
//

        val IP = getCache(AppConstant.Constant.IP, AppConstant.ClientInfo.BASE_IP)
        val WANT = getCache(AppConstant.Constant.WANT, "")
        val INTEREST = getCache(AppConstant.Constant.INTEREST, "")
        val PROFESSION = getCache(AppConstant.Constant.PROFESSION, "")
        val TURN = getCache(AppConstant.Constant.TURN, "")

        clearAllCache()

        setCache(AppConstant.Constant.IP, IP)
        setCache(AppConstant.Constant.WANT, WANT)
        setCache(AppConstant.Constant.INTEREST, INTEREST)
        setCache(AppConstant.Constant.PROFESSION, PROFESSION)
        setCache(AppConstant.Constant.TURN, TURN)

        val mIntent = context.createIntent(LoginActivity::class.java)

        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        context.startActivity(mIntent)


    }
}