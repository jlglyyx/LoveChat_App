package com.yang.lovechat.im

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.util.Log
import com.yang.lovechat.app.BaseApplication
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.InstructionType
import com.yang.lovechat.helper.IMHelper
import com.yang.lovechat.http.HttpClient.mChatOkHttpClient
import com.yang.lovechat.http.HttpEncrypt
import com.yang.lovechat.http.MResult
import com.yang.lovechat.util.fromJson
import com.yang.lovechat.util.toJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

//object ChatWebSocket {
//
//    private const val TAG = "ChatWebSocket"
//    private const val BASE_DELAY_TIME = 3000L // 稍微缩短初始重连时间
//    private const val MAX_DELAY_TIME = 5000L
//
//    private var mWebSocket: WebSocket? = null
//    private var socketScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
//
//    private var reconnectJob: Job? = null
//    private var heartJob: Job? = null
//    private var timeoutCheckJob: Job? = null
//
//    @Volatile private var isConnecting = false
//    @Volatile private var isConnected = false // 新增：明确标记是否真正连接成功
//    private var reconnectDelay = BASE_DELAY_TIME
//    private var canSendHeart = true
//
//    @Volatile private var lastReceiveTime = System.currentTimeMillis()
//    private var id: String? = null
//    private var networkCallback: ConnectivityManager.NetworkCallback? = null
//
//    // 用于加锁，避免并发重复触发重连
//    private val connectMutex = Mutex()
//
//    lateinit var sessionKey : ByteArray
//
//    fun sendRawMessage(message: String): Boolean {
//        val buildEncryptMessage = buildEncryptMessage(message)
//        Log.i(TAG, "sendRawMessage: $message")
//        return mWebSocket?.send(buildEncryptMessage) ?: false
//    }
//
//    fun init(context: Context) {
//        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        networkCallback = object : ConnectivityManager.NetworkCallback() {
//            override fun onAvailable(network: Network) {
//                Log.i(TAG, "网络已恢复，立即尝试重连...")
//                // 网络恢复时，不管三七二十一，如果没连上，立即触发重连（清空退避延迟）
//                socketScope.launch {
//                    reconnectDelay = BASE_DELAY_TIME
//                    startClient(id, force = true)
//                }
//            }
//        }
//        cm.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback!!)
//    }
//
//    // 加上 force 参数，网络恢复时可以强制打断当前的延迟重连
//    fun startClient(id: String?, force: Boolean = false) {
//        if (id.isNullOrEmpty()) return
//        this.id = id
//
//        socketScope.launch {
//            // 使用互斥锁，防止多个地方（网络恢复、定时重连、失败回调）同时触发连接
//            connectMutex.withLock {
//                if (isConnect() && !force) {
//                    Log.i(TAG, "当前已经是连接状态，跳过重连")
//                    return@withLock
//                }
//
//                if (isConnecting && !force) {
//                    Log.i(TAG, "正在连接中，请勿重复触发")
//                    return@withLock
//                }
//
//                isConnecting = true
//                isConnected = false
//
//                Log.i(TAG, "开始执行 WebSocket 连接逻辑...")
//
//                reconnectJob?.cancel()
//                closeClient()
//
//                sessionKey = HttpEncrypt.generateSessionKey()
//                val key = HttpEncrypt.rsaSealKey(sessionKey)
//                val info = HttpEncrypt.aesEncrypt(sessionKey, HttpEncrypt.getInfoMap())
//
//                val request = Request.Builder()
//                    .url("${AppConstant.ClientInfo.BASE_WS_URL}?${AppConstant.Constant.KEY}=${key}&${AppConstant.Constant.INFO}=${info}")
//                    .build()
//
//                mWebSocket = mChatOkHttpClient.newWebSocket(request, object : WebSocketListener() {
//                    override fun onOpen(webSocket: WebSocket, response: Response) {
//                        isConnecting = false
//                        isConnected = true
//                        reconnectDelay = BASE_DELAY_TIME // 成功后重置退避时间
//                        canSendHeart = true
//                        lastReceiveTime = System.currentTimeMillis()
//                        startHeart()
//                        Log.i(TAG, "连接成功")
//                    }
//
//                    override fun onMessage(webSocket: WebSocket, text: String) {
//                        lastReceiveTime = System.currentTimeMillis()
//                        val decryptMessage = decryptMessage(text)
//                        if (!decryptMessage.isNullOrEmpty()) {
//                            IMHelper.onReceiveRawText(decryptMessage)
//                        }
//                    }
//
//                    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
//                        lastReceiveTime = System.currentTimeMillis()
//                        IMHelper.onReceiveByteString(bytes)
//                    }
//
//                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
//                        isConnecting = false
//                        isConnected = false
//                        handleConnectionFailure("Failure: ${t.message}")
//                    }
//
//                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
//                        isConnecting = false
//                        isConnected = false
//                        handleConnectionFailure("Closed: $reason")
//                    }
//                })
//            }
//        }
//    }
//
//    private fun handleConnectionFailure(reason: String?) {
//        canSendHeart = false
//        heartJob?.cancel()
//        timeoutCheckJob?.cancel()
//        closeClient()
//
//        Log.e(TAG, "连接断开: $reason，将在 ${reconnectDelay}ms 后重连")
//
//        reconnectJob?.cancel()
//        reconnectJob = socketScope.launch {
//            delay(reconnectDelay)
//            // 指数退避，但有上限
//            reconnectDelay = (reconnectDelay * 1.5).toLong().coerceAtMost(MAX_DELAY_TIME)
//            startClient(id)
//        }
//    }
//
//    private fun startHeart() {
//        heartJob?.cancel()
//        timeoutCheckJob?.cancel()
//
//        heartJob = socketScope.launch {
//            val pingMsg = MResult(IMHelper.buildInstructionMessage<String>(id!!, InstructionType.PING), 200, "success", true).toJson()
//            while (canSendHeart && isActive) {
//                delay(10000)
//                if (!sendRawMessage(pingMsg)) break
//            }
//        }
//
//        timeoutCheckJob = socketScope.launch {
//            while (canSendHeart && isActive) {
//                delay(5000)
//                if (System.currentTimeMillis() - lastReceiveTime > 25000L) {
//                    Log.e(TAG, "心跳超时，强制关闭")
//                    closeClient()
//                    handleConnectionFailure("Heartbeat Timeout")
//                    break
//                }
//            }
//        }
//    }
//
//    // 严谨的连接状态判断：必须 WebSocket 对象不为空 且 标志位为已连接
//    fun isConnect(): Boolean{
//
//        val isConnect = mWebSocket != null && isConnected
//
//        return isConnect
//    }
//
//    fun buildEncryptMessage(body: String): String {
//        val content = HttpEncrypt.aesEncrypt(sessionKey, body)
//        return "{\"data\":\"$content\"}"
//    }
//
//    fun decryptMessage(message: String): String? {
//        val httpResult = message.fromJson<MResult<String>>()
//        val encryptedData = httpResult.data
//        if (encryptedData.isEmpty()) return null
//        return HttpEncrypt.aesDecrypt(sessionKey, encryptedData)
//    }
//
//    fun closeClient() {
//        mWebSocket?.cancel()
//        mWebSocket = null
//        isConnected = false
//    }
//
//    fun release(context: Context = BaseApplication.mApplication) {
//        networkCallback?.let {
//            try {
//                (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).unregisterNetworkCallback(it)
//            } catch (e: Exception) { Log.e(TAG, "Unregister error: ${e.message}") }
//        }
//        reconnectJob?.cancel()
//        heartJob?.cancel()
//        timeoutCheckJob?.cancel()
//        closeClient()
//        socketScope.cancel()
//    }
//}


object ChatWebSocket {

    private const val TAG = "ChatWebSocket"
    private const val BASE_DELAY_TIME = 3000L
    private const val MAX_DELAY_TIME = 10000L

    private var mWebSocket: WebSocket? = null
    private var socketScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var monitorJob: Job? = null // 唯一的常驻状态监视守护协程
    private var heartJob: Job? = null
    private var timeoutCheckJob: Job? = null

    @Volatile private var isConnecting = false
    @Volatile private var isConnected = false
    private var reconnectDelay = BASE_DELAY_TIME
    private var canSendHeart = true

    @Volatile private var lastReceiveTime = System.currentTimeMillis()
    private var id: String? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private val connectMutex = Mutex()

    lateinit var sessionKey : ByteArray

    fun sendRawMessage(message: String): Boolean {
        val buildEncryptMessage = buildEncryptMessage(message)
        Log.i(TAG, "sendRawMessage: $message")
        return mWebSocket?.send(buildEncryptMessage) ?: false
    }

    fun init(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.i(TAG, "网络已恢复，重置退避时间并触发连接...")
                socketScope.launch {
                    reconnectDelay = BASE_DELAY_TIME
                    if (!isConnect() && !isConnecting) {
                        startClient(id, force = true)
                    }
                }
            }
        }
        cm.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback!!)
    }

    fun startClient(id: String?, force: Boolean = false) {
        if (id.isNullOrEmpty()) return
        this.id = id

        // 启动全局守护监视器
        startMonitorGuard()

        socketScope.launch {
            connectMutex.withLock {
                if (isConnect() && !force) {
                    Log.i(TAG, "当前已经是连接状态，跳过重连")
                    return@withLock
                }

                if (isConnecting && !force) {
                    Log.i(TAG, "正在连接中，请勿重复触发")
                    return@withLock
                }

                isConnecting = true
                isConnected = false

                Log.i(TAG, "开始执行 WebSocket 连接逻辑...")

                closeClient()

                sessionKey = HttpEncrypt.generateSessionKey()
                val key = HttpEncrypt.rsaSealKey(sessionKey)
                val info = HttpEncrypt.aesEncrypt(sessionKey, HttpEncrypt.getInfoMap())

                val request = Request.Builder()
                    .url("${AppConstant.ClientInfo.BASE_WS_URL}?${AppConstant.Constant.KEY}=${key}&${AppConstant.Constant.INFO}=${info}")
                    .build()

                mWebSocket = mChatOkHttpClient.newWebSocket(request, object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        isConnecting = false
                        isConnected = true
                        reconnectDelay = BASE_DELAY_TIME
                        canSendHeart = true
                        lastReceiveTime = System.currentTimeMillis()
                        startHeart()
                        Log.i(TAG, "连接成功")
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        lastReceiveTime = System.currentTimeMillis()
                        val decryptMessage = decryptMessage(text)
                        if (!decryptMessage.isNullOrEmpty()) {
                            IMHelper.onReceiveRawText(decryptMessage)
                        }
                    }

                    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                        lastReceiveTime = System.currentTimeMillis()
                        IMHelper.onReceiveByteString(bytes)
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        isConnecting = false
                        isConnected = false
                        handleConnectionFailure("Failure: ${t.message}")
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        isConnecting = false
                        isConnected = false
                        handleConnectionFailure("Closed: $reason")
                    }
                })
            }
        }
    }

    private fun handleConnectionFailure(reason: String?) {
        canSendHeart = false
        heartJob?.cancel()
        timeoutCheckJob?.cancel()
        closeClient()
        Log.e(TAG, "连接断开: $reason，等待守护线程下一次重连调度")
    }

    /**
     * 常驻状态监视与重连守护协程
     */
    private fun startMonitorGuard() {
        if (monitorJob?.isActive == true) return

        monitorJob = socketScope.launch {
            while (isActive) {
                if (id.isNullOrEmpty()) {
                    delay(3000)
                    continue
                }

                if (!isConnect() && !isConnecting) {
                    Log.w(TAG, "监视器发现 WebSocket 处于断开状态，准备在 ${reconnectDelay}ms 后尝试重新连接...")
                    delay(reconnectDelay)
                    reconnectDelay = (reconnectDelay * 1.5).toLong().coerceAtMost(MAX_DELAY_TIME)
                    startClient(id, force = true)
                } else {
                    reconnectDelay = BASE_DELAY_TIME
                    delay(3000)
                }
            }
        }
    }

    private fun startHeart() {
        heartJob?.cancel()
        timeoutCheckJob?.cancel()

        heartJob = socketScope.launch {
            val pingMsg = MResult(IMHelper.buildInstructionMessage<String>(id!!, InstructionType.PING), 200, "success", true).toJson()
            while (canSendHeart && isActive) {
                delay(10000)
                if (!sendRawMessage(pingMsg)) {
                    Log.e(TAG, "发送心跳失败，触发断开")
                    break
                }
            }
        }

        timeoutCheckJob = socketScope.launch {
            while (canSendHeart && isActive) {
                delay(5000)
                if (System.currentTimeMillis() - lastReceiveTime > 25000L) {
                    Log.e(TAG, "心跳超时（长时间未收到服务端数据），强制关闭连接")
                    closeClient()
                    handleConnectionFailure("Heartbeat Timeout")
                    break
                }
            }
        }
    }

    fun isConnect(): Boolean {
        return mWebSocket != null && isConnected
    }

    fun buildEncryptMessage(body: String): String {
        val content = HttpEncrypt.aesEncrypt(sessionKey, body)
        return "{\"data\":\"$content\"}"
    }

    fun decryptMessage(message: String): String? {
        val httpResult = message.fromJson<MResult<String>>()
        val encryptedData = httpResult.data
        if (encryptedData.isEmpty()) return null
        return HttpEncrypt.aesDecrypt(sessionKey, encryptedData)
    }

    fun closeClient() {
        try {
            mWebSocket?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Close client error: ${e.message}")
        }
        mWebSocket = null
        isConnected = false
        canSendHeart = false
    }

    fun release(context: Context = BaseApplication.mApplication) {
        id = null
        networkCallback?.let {
            try {
                (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).unregisterNetworkCallback(it)
            } catch (e: Exception) { Log.e(TAG, "Unregister error: ${e.message}") }
        }
        monitorJob?.cancel()
        heartJob?.cancel()
        timeoutCheckJob?.cancel()
        closeClient()
        socketScope.cancel()
        socketScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }
}