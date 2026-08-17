package com.yang.lovechat.data


data class ConversationData(
    val id: Long,
    var convId: String,
    var convType: Int,
    var lastMsgId: Long,
    var lastMsgContent: String,
    var lastMsgType: Int,
    var lastMsgUserId: Int,
    var lastMsgTime: Long,
    var friendAvatar: String?,
    var friendName: String,
    var friendUserId: Long,
    var unreadCount: Int,
    var isTop: Boolean,
    val isOnline: Boolean,
)