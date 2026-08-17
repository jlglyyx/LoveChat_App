package com.yang.lovechat.util

import com.yang.lovechat.constant.AppConstant



fun isMediaLocked(unlockTime: Long?): Boolean {

    return unlockTime == null

}

fun isMediaExpired(unlockTime: Long): Boolean {

    val passMs = System.currentTimeMillis() - unlockTime

    return passMs !in 0..(AppConstant.Constant.totalExpiredSecond * 1000L)
}

fun isUnlockTimeValid(unlockTime: Long?): Boolean {

    val mediaLocked = isMediaLocked(unlockTime)

    if (mediaLocked) return  false

    if (null == unlockTime) return  false

    val mediaExpired = isMediaExpired(unlockTime)

    return !mediaExpired
}
