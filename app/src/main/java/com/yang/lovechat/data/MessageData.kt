package com.yang.lovechat.data

import kotlin.random.Random


class MessageData(
    var uid: Long = System.currentTimeMillis()+ Random.nextInt(1,100),
    var id: Long? = null,
    var convId: String? = null,
    var userId: Long? = null,
    var userName: String? = null,
    var userAvatar: String? = null,
    var friendUserId: Long? = null,
    var friendName: String? = null,
    var friendAvatar: String? = null,
    var msgType: Int = 0,
    var msgContent: String = "",
    var isPrivate: Boolean? = null,
    var createTime: Long? = null,
    var updateTime: Long? = null,
    var errorDesc: String? = null
) {
    var msgMediaContent: MutableList<MediaInfoData>? = null
}


data class MessageResultData<T : Any>(
    var data: T?,
    val sendId: String,
    val msgType: Int,
    val instructionType: String? = null,
    val sendMessageId: String? = null
)



