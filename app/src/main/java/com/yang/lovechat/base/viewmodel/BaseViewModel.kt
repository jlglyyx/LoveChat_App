package com.yang.lovechat.base.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yang.lovechat.base.bus.SingleFlow
import com.yang.lovechat.http.ErrorHandle
import com.yang.lovechat.http.IHttpException
import com.yang.lovechat.http.MResult
import com.yang.lovechat.util.showShort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class BaseViewModel : ViewModel() {

    val TAG = this.javaClass.simpleName

    val finishRefreshEvent: SingleFlow<Any> = SingleFlow()

    val requestFailEvent:SingleFlow<Any> = SingleFlow()

    val requestExceptionEvent:SingleFlow<Any> = SingleFlow()



    fun MutableStateFlow<MutableMap<String, Any?>>.updateParams(block: MutableMap<String, Any?>.() -> Unit) {
        val newParams = this.value.toMutableMap().apply(block)
        this.value = newParams
    }



    suspend fun <T : Any> withContextIO(mResult: suspend () -> MResult<T>): MResult<T> {
        return withContext(Dispatchers.IO) {
            mResult().isSuccess()
        }
    }

    private fun <T : MResult<*>> T.isSuccess(): T {
        if (!this.success) {
            throw IHttpException.HttpErrorException(this.message, this.code)
        }
        return this
    }


    fun <T : Any> launch(
        onRequest:suspend () -> MResult<T>,
        onSuccess:suspend (MResult<T>) -> Unit = {},
        onError:suspend (IHttpException.HttpErrorException) -> Boolean = {false},
        onErrorHandle:suspend (Throwable) -> Unit = {},
        onException:suspend (Throwable) -> Boolean = { true },
    ) {
        viewModelScope.launch {
            try {

                onSuccess(withContextIO {
                    onRequest()
                })
            } catch (t: Throwable) {
                t.printStackTrace()

                val handleMessage = ErrorHandle.handle(t)

                when(t){
                    is IHttpException.HttpErrorException -> {

                        val needShowToast = onError(t)

                        if (needShowToast && handleMessage.isNotEmpty()){
                            showShort(handleMessage)
                        }
                    }

                    else -> {
                        val needShowToast = onException(t)

                        if (needShowToast && handleMessage.isNotEmpty()){
                            showShort(handleMessage)
                        }
                    }
                }

                onErrorHandle(t)


            }finally {

                this.cancel()

            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }


}