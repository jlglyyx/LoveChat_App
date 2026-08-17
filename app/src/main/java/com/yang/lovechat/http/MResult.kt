package com.yang.lovechat.http

data class MResult<T : Any>(
    val data: T, val code: Int, val message: String = "",val success : Boolean = false,val total:Int? = null,val count:Int? = null
)

