package com.yang.lovechat.http

/**
 * @ClassName IHttpException
 *
 * @Date 2020/11/26 17:57
 */

interface IHttpException {

    class HttpErrorException(override var message: String, var code: Int) : Exception()

    enum class HttpException(var code: Int,var  message: String){

        NO_FIND(404,"资源不存在"),

        NO_JURISDICTION(401,"登录已过期，请重新登录"),

        NO_403_JURISDICTION(403,"登录已过期，请重新登录")

    }
    enum class OtherException(var message: String){

        SOCKET_TIME_OUT_ERROR("连接超时，请稍后重试"),

        NETWORK_ERROR("网络异常，请稍后重试"),

        NO_NETWORK_ERROR("当前网络不可用"),

        JSON_SYNTAX_ERROR("数据解析失败"),

        UN_KNOWN_ERROR("未知异常, 错误信息 : ")
    }

}