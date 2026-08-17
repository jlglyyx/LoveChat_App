package com.yang.lovechat.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserInfoData(
    val id: Long,
    val token: String,
    val firstLogin: Boolean,
    val userName: String,
    val email: String,
    val password: String,
    val gender: Int,
    val isOnline: Boolean,
    val avatarUrl: String?,
    val cityName: String?,
    val age: Int,
    val birth: Long,
    val height: Int,
    val weight: Int,
    val constellation: String?,
    val bio: String?,
    val intent: String?,
    val interest: List<TagConfigData>? = emptyList(),
    val profession: String?,
    val greetingText: String,
    val backgroundMediaList: List<MediaInfoData>? = emptyList(),
    val userWallet: UserWalletData?,
    var convId: String? = null,
    var viewedCount: Int = 0,
    var vipStatus: Boolean = false,
    var isReview: Boolean = false
) : Parcelable




@Parcelize
data class UserWalletData(
    val id: Int,
    val userId: Int,
    val totalDiamond: Int,
    val balance: Int,
    val bonusBalance: Int,
    val vipExpireTime: Long,
    val isFreeRider: Boolean,
    val freeChatCount: Int,
    val createTime: Long,
    val updateTime: Long
) : Parcelable



class UpdateUserInfoData {

    var userName: String? = null
    var gender: Int? = null

    var age: Int? = null

    var interest: List<UpdateUserTagData>? = null

    var intent: String? = null

    var bio: String? = null

    var avatarUrl: String? = null
    var backgroundMediaList: MutableList<UpdateMediaInfoData>? = null

    var birth: Long? = null

    var constellation: String? = null

    var height: String? = null
    var profession: String? = null
    var weight: String? = null


}



data class UpdateUserTagData(
    val id: Long,
    val tagType: Int,
)
