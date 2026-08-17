package com.yang.lovechat.im

import com.yang.lovechat.data.MessageData
import com.yang.lovechat.data.MessageResultData
import com.yang.lovechat.helper.IMHelper
import com.yang.lovechat.http.MResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object MessageManager {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // 1. 普通聊天消息流
    private val _normalMessageFlow = MutableSharedFlow<Triple<MResult<MessageResultData<Any>>,MessageData, String>>(extraBufferCapacity = 64)
    val normalMessageFlow: SharedFlow<Triple<MResult<MessageResultData<Any>>, MessageData, String>> = _normalMessageFlow.asSharedFlow()

    // 2. 业务消息流
    private val _businessMessageFlow = MutableSharedFlow<Triple<MResult<MessageResultData<Any>>,MessageResultData<Any>, String>>(extraBufferCapacity = 64)
    val businessMessageFlow: SharedFlow<Triple<MResult<MessageResultData<Any>>,MessageResultData<Any>, String>> = _businessMessageFlow.asSharedFlow()

    // 3. 普通消息错误流
    private val _errorMessageFlow = MutableSharedFlow<Triple<MResult<MessageResultData<Any>>,MessageResultData<Any>, String>>(extraBufferCapacity = 64)
    val errorMessageFlow: SharedFlow<Triple<MResult<MessageResultData<Any>>,MessageResultData<Any>, String>> = _errorMessageFlow.asSharedFlow()

    // 4. 业务消息错误流
    private val _businessErrorFlow = MutableSharedFlow<Triple<MResult<MessageResultData<Any>>,MessageResultData<Any>, String>>(extraBufferCapacity = 64)
    val businessErrorFlow: SharedFlow<Triple<MResult<MessageResultData<Any>>,MessageResultData<Any>, String>> = _businessErrorFlow.asSharedFlow()

    // 分发逻辑
    fun dispatcherMessage(result: MResult<MessageResultData<Any>>, text: String) {
        scope.launch {
            try {
                val normalMessage = IMHelper.isNormalMessage(result.data)

                if (result.success) {
                    if (normalMessage) {
                        val message = IMHelper.getMessageData(result.data)
                        if (message != null) {
                            // 往“普通消息流”塞数据
                            _normalMessageFlow.emit(Triple(result,message, text))
                        }
                    } else {
                        // 往“业务消息流”塞数据
                        _businessMessageFlow.emit(Triple(result,result.data, text))
                    }
                } else {
                    if (normalMessage) {
                        // 往“普通错误流”塞数据 (code, message, result)
                        _errorMessageFlow.emit(Triple(result,result.data, text))
                    } else {
                        // 往“业务错误流”塞数据
                        _businessErrorFlow.emit(Triple(result,result.data, text))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}