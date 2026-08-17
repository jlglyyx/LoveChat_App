package com.yang.lovechat.http

import android.util.Log
import com.yang.lovechat.base.bus.EventBus
import com.yang.lovechat.base.bus.EventBus.postValue
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.helper.UserInfoHold
import com.yang.lovechat.util.showShort
import org.json.JSONException
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.toString


object ErrorHandle {

    private const val TAG = "ErrorHandle"

    fun handle(t: Throwable): String {
        Log.i(TAG, "handle: ${t.message.toString()}")
        return when (t) {
            is HttpException -> {
                return when (t.code()) {
                    IHttpException.HttpException.NO_FIND.code -> {

                        IHttpException.HttpException.NO_FIND.message
                    }

                    IHttpException.HttpException.NO_JURISDICTION.code -> {
                        UserInfoHold.loginOut()
                        EventBus.with(AppConstant.EventConstant.EVENT_RE_LOGIN).postValue(t)

                        showShort(IHttpException.HttpException.NO_JURISDICTION.message)
                        IHttpException.HttpException.NO_JURISDICTION.message
                    }

                    IHttpException.HttpException.NO_403_JURISDICTION.code -> {
                        UserInfoHold.loginOut()
                        EventBus.with(AppConstant.EventConstant.EVENT_RE_LOGIN).postValue(t)

                        showShort(IHttpException.HttpException.NO_403_JURISDICTION.message)

                        IHttpException.HttpException.NO_403_JURISDICTION.message
                    }

                    else -> {

                        IHttpException.OtherException.NETWORK_ERROR.message
                    }
                }
            }

            is IHttpException.HttpErrorException -> {
                if (t.code == 401) {
                    UserInfoHold.loginOut()
                    EventBus.with(AppConstant.EventConstant.EVENT_RE_LOGIN).postValue(t)

                    showShort(IHttpException.HttpException.NO_JURISDICTION.message)
                }
                t.message
            }

            is CancellationException -> {
                ""
            }

            is UnknownHostException -> {

                IHttpException.OtherException.NO_NETWORK_ERROR.message
            }

            is SocketTimeoutException -> {

                IHttpException.OtherException.SOCKET_TIME_OUT_ERROR.message
            }

            is JSONException -> {

                IHttpException.OtherException.JSON_SYNTAX_ERROR.message
            }

            else -> {

                IHttpException.OtherException.UN_KNOWN_ERROR.message + "${t.message}"

            }
        }

    }


}