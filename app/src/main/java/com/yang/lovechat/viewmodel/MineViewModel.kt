package com.yang.lovechat.viewmodel

import com.yang.lovechat.base.bus.SingleFlow
import kotlin.collections.set

class MineViewModel: PublicViewModel() {

    val mUserFeedbackStatus = SingleFlow<Boolean>()

    val mDeleteUserStatus = SingleFlow<Boolean>()



    fun userFeedback(text: String,email: String) {

        val params = mutableMapOf<String, Any?>()

        params["content"] = text

        params["contactEmail"] = email

        launch({

            mApiService.userFeedback(params)

        }, {

            mUserFeedbackStatus.postValue(true)

        })

    }

    fun deleteUser() {

        launch({

            mApiService.deleteUser()

        }, {

            mDeleteUserStatus.postValue(true)

        })

    }





}