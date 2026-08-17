package com.yang.lovechat.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TagConfigData(
    val id: Long,
    val tagType: Int,
    val tagName: String,
    val tagDesc: String,
    val sortOrder: Int,
    var isCheck: Boolean = false
) : Parcelable

