package com.yang.lovechat.viewmodel

import com.yang.lovechat.base.bus.EventBus
import com.yang.lovechat.base.bus.EventBus.postValue
import com.yang.lovechat.base.bus.SingleFlow
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.ConversationData
import com.yang.lovechat.data.InstructionType
import com.yang.lovechat.data.MessageData
import com.yang.lovechat.helper.IMHelper
import com.yang.lovechat.helper.IMHelper.buildInstructionMessage
import com.yang.lovechat.helper.UserInfoHold

class MessageViewModel: PublicViewModel() {

    val mConversationListData = SingleFlow<MutableList<ConversationData>>()

    val mConversationData = SingleFlow<ConversationData>()

    val mMessageListData = SingleFlow<MutableList<MessageData>>()

    val mAlignmentMessageListData = SingleFlow<MutableList<MessageData>>()


    val mUnlockMediaMessageData = SingleFlow<MessageData>()


    val mUnlockMediaErrorStatus = SingleFlow<Any>()


    val mShieldConversationStatus = SingleFlow<String>()



    fun getConversationList() {

        val params = mutableMapOf<String, Any?>()

        params[AppConstant.Constant.PAGE_NUM] = pageNum

        params[AppConstant.Constant.PAGE_SIZE] = AppConstant.Constant.PAGE_SIZE_COUNT

        launch({

            mApiService.getConversationList(params)

        }, {

            mConversationListData.postValue(it.data)

        }, onErrorHandle = {

            requestExceptionEvent.postValue(it)
        })

    }

    fun getConversationDetail(convId: String) {

        val params = mutableMapOf<String, Any?>()

        params[AppConstant.Constant.CONV_ID] = convId


        launch({

            mApiService.getConversationDetail(params)

        }, {

            mConversationData.postValue(it.data)

        })

    }

    fun topConversation(isTop: Boolean,convId: String) {

        val params = mutableMapOf<String, Any?>()

        params[AppConstant.Constant.CONV_ID] = convId

        params["topConversation"] = isTop

        launch({

            mApiService.topConversation(params)

        }, {


        })

    }

    fun deleteConversation(isDelete: Boolean,convId: String) {

        val params = mutableMapOf<String, Any?>()


        params[AppConstant.Constant.CONV_ID] = convId

        params["deleteConversation"] = isDelete

        launch({

            mApiService.deleteConversation(params)

        }, {


        })

    }

    fun shieldConversation(isShield: Boolean,convId: String) {

        val params = mutableMapOf<String, Any?>()


        params[AppConstant.Constant.CONV_ID] = convId

        params["shieldConversation"] = isShield

        launch({

            mApiService.shieldConversation(params)

        }, {

            EventBus.with(AppConstant.EventConstant.EVENT_SHIELD_CONVERSATION).postValue(convId)



            mShieldConversationStatus.postValue(convId)

        })

    }





    fun readConversation(convId: String) {

        val params = mutableMapOf<String, Any?>()

        val userId = UserInfoHold.userId

        params[AppConstant.Constant.CONV_ID] = convId


//        launch({
//
//            mApiService.readConversation(params)
//
//        }, {
//
//            EventBus.with(AppConstant.EventConstant.EVENT_UPDATE_CONVERSATION_READ_COUNT).postValue(convId)
//
//        })


        val buildInstructionMessage = buildInstructionMessage(userId.toString(),InstructionType.READ_CONVERSATION,params)

        IMHelper.sendMessage(buildInstructionMessage)

    }


    fun readAllConversation() {

        val params = mutableMapOf<String, Any?>()


        launch({

            mApiService.readAllConversation(params)

        }, {


        })

    }


    fun getMessageHistory(convId: String, lastMsgId: Long? = null,pageSize: Int = AppConstant.Constant.PAGE_SIZE_COUNT) {

        if (convId.isEmpty()) return

        val params = mutableMapOf<String, Any?>()

        params[AppConstant.Constant.CONV_ID] = convId


        if (null != lastMsgId){
            params[AppConstant.Constant.LAST_MSG_ID] = lastMsgId
        }

        params[AppConstant.Constant.PAGE_SIZE] = pageSize

        launch({

            mApiService.getMessageHistory(params)

        }, {

            mMessageListData.postValue(it.data)

        })

    }

    fun alignmentMessageHistory(convId: String,pageSize: Int = 30) {

        if (convId.isEmpty()) return

        val params = mutableMapOf<String, Any?>()

        params[AppConstant.Constant.CONV_ID] = convId

        params[AppConstant.Constant.PAGE_SIZE] = pageSize

        launch({

            mApiService.getMessageHistory(params)

        }, {

            mAlignmentMessageListData.postValue(it.data)

        })

    }


    fun unlockMedia(convId: String, messageId: Long,mediaIds: MutableSet<Long>) {

        if (convId.isEmpty()) return

        val params = mutableMapOf<String, Any?>()

        params[AppConstant.Constant.CONV_ID] = convId

        params[AppConstant.Constant.MESSAGE_ID] = messageId

        params[AppConstant.Constant.MEDIA_IDS] = mediaIds

        launch({

            mApiService.unlockMedia(params)

        }, {

            getUserInfo(UserInfoHold.userId)

            mUnlockMediaMessageData.postValue(it.data)
        }, onErrorHandle = {

            mUnlockMediaErrorStatus.postValue(it)

        })

    }
}