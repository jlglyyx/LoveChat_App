package com.yang.lovechat.helper

import android.util.Log
import com.yang.lovechat.base.bus.EventBus
import com.yang.lovechat.base.bus.EventBus.postValue
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.InstructionType
import com.yang.lovechat.data.MessageData
import com.yang.lovechat.data.MessageResultData
import com.yang.lovechat.data.MessageType
import com.yang.lovechat.http.MResult
import com.yang.lovechat.im.ChatWebSocket
import com.yang.lovechat.im.MessageManager
import com.yang.lovechat.util.fromJson
import com.yang.lovechat.util.showShort
import com.yang.lovechat.util.toJson
import okio.ByteString

object IMHelper {

    private const val TAG = "IMHelper"

    val messageType = arrayOf(MessageType.TEXT.type, MessageType.IMAGE_VIDEO.type)

    val instructionTypeList = arrayOf(InstructionType.MATCH.value, InstructionType.FAST_CONNECT.value,InstructionType.LIKE_I.value,InstructionType.VIEW_I.value)

    var allReadCount = 0

    /**
     * 初始化并启动 IM
     */
    fun startIM(id: String) {
        ChatWebSocket.startClient(id)
    }

    /**
     * 判断当前 IM 是否处于连接状态
     */
    fun isConnect(): Boolean {
        return ChatWebSocket.isConnect()
    }

    /**
     * 断开连接并释放资源
     */
    fun releaseIM() {
        allReadCount = 0
        ChatWebSocket.release()
    }


    fun onReceiveRawText(text: String) {
        try {
            val httpResult = text.fromJson<MResult<MessageResultData<Any>>>()

            val mMessageResultData = httpResult.data

            if (mMessageResultData.msgType == MessageType.SYSTEM.type && mMessageResultData.instructionType == InstructionType.PONG.value) {

//                Log.i(TAG, "收到心跳 PONG 应答 ${text}")

                return
            }

            Log.e(TAG, "收到消息: ${text}")

            MessageManager.dispatcherMessage(httpResult, text)

        } catch (e: Exception) {
            Log.e(TAG, "IMHelper 分发异常: ${e.message}")
            e.printStackTrace()
        }
    }

    fun onReceiveByteString(bytes: ByteString) {
        // 二进制处理留空
    }

    /**
     * 刷新全局未读数
     */
    fun refreshAllReadCount(count: Int, isAdd: Boolean) {
        if (count <= 0) return

        if (isAdd) {
            allReadCount += count
        } else {
            allReadCount -= count
        }

        EventBus.with(AppConstant.EventConstant.EVENT_UPDATE_ALL_CONVERSATION_READ_COUNT)
            .postValue(allReadCount)
    }


    /**
     * 统一发送入口
     */
    fun sendMessage(data: MessageResultData<out Any>?) {
        if (!isConnect()) {
            showShort("not connect")
            return
        }

        if (null == data) {
            showShort("send message error")
            return
        }


        val fullPacket = MResult(
            data = data,
            code = 200,
            message = "success",
            success = true
        )

        val messageJson = fullPacket.toJson()


        ChatWebSocket.sendRawMessage(messageJson)

        Log.e(TAG, "发送消息: ${messageJson}")


    }

    /**
     * 发送文本消息
     */
    fun buildTextMessage(
        sendId: Long?,
        convId: String,
        friendUserId: Long?,
        msgContent: String
    ): MessageResultData<MessageData>? {

        val payload = buildMessageDataEntity(
            convId,
            sendId,
            friendUserId,
            MessageType.TEXT,
            msgContent,
            false
        ) ?: return null

        val message = buildMessage(sendId.toString(), MessageType.TEXT, null, payload)

        return message
    }

    /**
     * 发送图片+视频消息
     */
    fun buildImageVideoMessage(
        sendId: Long?,
        convId: String,
        friendUserId: Long?,
        msgContent: String,
        isPrivate: Boolean
    ): MessageResultData<MessageData>? {

        val payload = buildMessageDataEntity(
            convId,
            sendId,
            friendUserId,
            MessageType.IMAGE_VIDEO,
            msgContent,
            isPrivate
        ) ?: return null

        val message = buildMessage(sendId.toString(), MessageType.IMAGE_VIDEO, null, payload)

        return message
    }


    /**
     * 根据参数直接装配出你原汁原味的 MessageData 实体
     */
    private fun buildMessageDataEntity(
        convId: String,
        userId: Long?,
        friendUserId: Long?,
        messageType: MessageType,
        msgContent: String,
        isPrivate: Boolean
    ): MessageData? {

        val userId = userId ?: return null

        val time = System.currentTimeMillis()

        return MessageData(
            id = null,
            convId = convId,
            userId = userId,
            friendUserId = friendUserId,
            msgType = messageType.type,
            msgContent = msgContent,
            isPrivate = isPrivate,
            createTime = time,
            updateTime = time,
            userName = null,
            userAvatar = null,
            friendName = null,
            friendAvatar = null,
        )
    }

    /**
     * 组装中间层元数据结构体的工厂
     */
    private fun <T : Any> buildMessage(
        sendId: String,
        messageType: MessageType,
        instructionType: String?,
        payload: T?
    ): MessageResultData<T> {

        val currentTimeMillis = System.currentTimeMillis()

        val localId = "ID$currentTimeMillis"

        return MessageResultData(
            sendId = sendId,
            msgType = messageType.type,
            instructionType = instructionType,
            sendMessageId = localId,
            data = payload
        )
    }

    /**
     * 提供给底层心跳或特定指令构建系统指令消息
     */
    fun <T : Any> buildInstructionMessage(
        sendId: String,
        instructionType: InstructionType,
        data: T? = null
    ): MessageResultData<Any> {
        return MessageResultData(
            sendId = sendId,
            msgType = MessageType.SYSTEM.type,
            instructionType = instructionType.value,
            sendMessageId = "ID${System.currentTimeMillis()}",
            data = data,
        )
    }


    fun getMessageData(mMessageResultData: MessageResultData<Any>): MessageData? {

        try {

            if (isNormalMessage(mMessageResultData)) {

                return mMessageResultData.data.toJson().fromJson()
            }

            return null


        } catch (e: Exception) {
            e.printStackTrace()

            return null
        }

    }


    fun isNormalMessage(message: MessageResultData<Any>): Boolean {

        val msgType = message.msgType

        val instructionType = message.instructionType

        return (msgType in messageType) || (instructionType in instructionTypeList)


    }


}