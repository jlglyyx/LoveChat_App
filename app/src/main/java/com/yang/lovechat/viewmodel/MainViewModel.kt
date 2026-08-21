package com.yang.lovechat.viewmodel

import com.blankj.utilcode.util.EncryptUtils
import com.yang.lovechat.base.bus.SingleFlow
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.AppVersion
import com.yang.lovechat.data.UserInfoData
import com.yang.lovechat.data.ChatterUserData
import com.yang.lovechat.helper.UserInfoHold
import com.yang.lovechat.util.setCache

class MainViewModel: PublicViewModel() {


    val mLoginInfoData = SingleFlow<UserInfoData>()

    val mRecommendUserListData = SingleFlow<MutableList<UserInfoData>>()

    val mLikeIListData = SingleFlow<MutableList<UserInfoData>>()

    val mILikeListData = SingleFlow<MutableList<UserInfoData>>()

    val mViewedIListData = SingleFlow<MutableList<UserInfoData>>()

    val mAppVersion = SingleFlow<AppVersion>()










    fun checkNewVersion(block:(()-> Unit)? = null) {

        launch(onRequest = {

            mApiService.checkNewVersion()
        }, onSuccess = {

            block?.invoke()

            setCache(AppConstant.Constant.IS_REVIEW_VERSION,it.data.isReview)

            mAppVersion.postValue(it.data)

        }, onErrorHandle = {

            requestFailEvent.postValue(it)

        })


    }


    fun login(loginType: String, email: String, password: String) {

        val params = mutableMapOf<String, Any?>()

        if (loginType == AppConstant.Constant.EMAIL) {

            params["email"] = email

            params["password"] = EncryptUtils.encryptMD5ToString(password)

        } else {

            params["googleToken"] = email
        }


        launch(onRequest = {

            mApiService.login(params)
        }, onSuccess = {

            mLoginInfoData.postValue(it.data)

            setCache(AppConstant.Constant.TOKEN, it.data.token)

            getUserInfo(it.data.id)


        }, onError = {


            requestFailEvent.postValue(it.message)

            false


        }, onException = {

            requestFailEvent.postValue(it.message.toString())

            false
        })


    }





    fun getRecommendUserList() {

        val params = mutableMapOf<String, Any?>()


        params[AppConstant.Constant.PAGE_SIZE] = AppConstant.Constant.PAGE_SIZE_COUNT

        launch({

            mApiService.getRecommendUserList(params)

        }, {

            mRecommendUserListData.postValue(it.data)


        }, onErrorHandle = {

            requestExceptionEvent.postValue(it)
        })

    }








    fun getLikeIList() {

        val params = mutableMapOf<String, Any?>()

        params[AppConstant.Constant.USER_ID] = UserInfoHold.userId

        params[AppConstant.Constant.PAGE_NUM] = pageNum

        params[AppConstant.Constant.PAGE_SIZE] = AppConstant.Constant.PAGE_SIZE_COUNT

        launch({

            mApiService.getLikeIList(params)

        }, {

            mLikeIListData.postValue(it.data)
        }, onErrorHandle = {

            requestExceptionEvent.postValue(it)
        })

    }

    fun getILikeList() {

        val params = mutableMapOf<String, Any?>()

        params[AppConstant.Constant.USER_ID] = UserInfoHold.userId

        params[AppConstant.Constant.PAGE_NUM] = pageNum

        params[AppConstant.Constant.PAGE_SIZE] = AppConstant.Constant.PAGE_SIZE_COUNT

        launch({

            mApiService.getILikeList(params)

        }, {

            mILikeListData.postValue(it.data)

        }, onErrorHandle = {

            requestExceptionEvent.postValue(it)
        })

    }






}