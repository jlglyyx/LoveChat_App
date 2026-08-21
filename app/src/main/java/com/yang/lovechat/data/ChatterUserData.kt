package com.yang.lovechat.data


data class ChatterUserData(
    val userId: Long,
    val avatarUrl: String,
    val userName: String,
    val gender: Int,
    val age: Int,
    var totalUnreadCount: Int,
){

    var isCheck: Boolean = false

}