package com.yang.lovechat.http

import android.util.Log
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.http.HttpEncrypt.generateSessionKey
import okhttp3.Interceptor
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8

object InterceptorManager {

    private val UTF8_SET = UTF_8

    private val TAG = "LogInterceptor"
    class HeadInterceptor : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {


            val request = chain.request()

            val sessionKey = generateSessionKey()

            val newBuilder = request.newBuilder()
                .addHeader(AppConstant.Constant.KEY, HttpEncrypt.rsaSealKey(sessionKey))
                .addHeader(AppConstant.Constant.INFO,HttpEncrypt.aesEncrypt(sessionKey,HttpEncrypt.getInfoMap()))

            request.body?.let { body ->

                val contentType = body.contentType()

                if (contentType != null && contentType.type == "multipart") {
                    return@let
                }


                val buffer = Buffer()

                body.writeTo(buffer)

                if (HttpEncrypt.run { buffer.checkIsUtf8() }) {

                    val rawContent = buffer.readString(body.contentType()?.charset(UTF8_SET) ?: UTF8_SET)

                    val content = HttpEncrypt.aesEncrypt(sessionKey,rawContent)

                    Log.e(TAG, "request: ${request.url} ->\n  ${rawContent}")

//                    if (BuildConfig.DEBUG) {

//                    }

                    val data = "{\"data\":\"$content\"}"

                    val encryptedBody = data.toRequestBody(body.contentType())

                    newBuilder.method(request.method,encryptedBody)

                }
            }

            val newRequest = newBuilder.build()

            val response = chain.proceed(newRequest)


            if (response.isSuccessful) {

                val contentType = response.body.contentType()

                val rawBody = response.body.string()

                val jsonObject = JSONObject(rawBody)

                val encryptedData = jsonObject.optString("data")

                if (encryptedData.isNotEmpty()) {

                    val decryptedContent = HttpEncrypt.aesDecrypt(sessionKey,encryptedData)

                    val newBody = decryptedContent.toResponseBody(contentType)

                    Log.e(TAG, "response: ${request.url} ->\n  ${decryptedContent}")

//                    if (BuildConfig.DEBUG) {

//                    }

                    return response.newBuilder().body(newBody).build()
                }
            }

            return response
        }

    }


    class LogInterceptor : Interceptor {

        companion object {
            private const val TAG = "API_LOG"
            private const val MAX_LOG_LENGTH = 3000 // 限制单条日志长度，防止截断
        }

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val startTime = System.currentTimeMillis()

            // --- 打印请求 ---
            val requestLog = StringBuilder()
            requestLog.append("\n┌──────────────────── Request Start ────────────────────\n")
            requestLog.append("│ URL: ${request.url}\n")
            requestLog.append("│ Method: ${request.method}\n")

            // 过滤文件上传：如果包含 multipart，不打印 Body
            val isMultipart = request.body?.contentType()?.type == "multipart"
            if (!isMultipart) {
                request.body?.let { body ->
                    val buffer = Buffer()
                    body.writeTo(buffer)
                    val charset = body.contentType()?.charset(Charset.forName("UTF-8")) ?: Charset.forName("UTF-8")
                    requestLog.append("│ Body: ${formatJson(buffer.readString(charset))}\n")
                }
            } else {
                requestLog.append("│ Body: [File Uploading - Ignored]\n")
            }
            requestLog.append("└──────────────────────────────────────────────────────")
            printLog(requestLog.toString())

            // --- 执行请求 ---
            val response = chain.proceed(request)
            val endTime = System.currentTimeMillis()

            // --- 打印响应 ---
            val responseLog = StringBuilder()
            responseLog.append("\n┌──────────────────── Response (${endTime - startTime}ms) ────────────────────\n")
            responseLog.append("│ Code: ${response.code} / Message: ${response.message}\n")

            val responseBody = response.body
            val source = responseBody?.source()
            source?.request(Long.MAX_VALUE)
            val buffer = source?.buffer
            val charset = responseBody?.contentType()?.charset(Charset.forName("UTF-8")) ?: Charset.forName("UTF-8")

            val bodyString = buffer?.clone()?.readString(charset) ?: ""
            responseLog.append("│ Body: \n${formatJson(bodyString)}\n")
            responseLog.append("└────────────────────────────────────────────────────────────────────────────────")
            printLog(responseLog.toString())

            return response
        }

        /**
         * 分段打印日志，解决 Android Studio 日志溢出问题
         */
        private fun printLog(log: String) {
            if (log.length <= MAX_LOG_LENGTH) {
                Log.d(TAG, log)
            } else {
                // 按行切割打印，或者直接按长度截取打印
                var i = 0
                while (i < log.length) {
                    val end = (i + MAX_LOG_LENGTH).coerceAtMost(log.length)
                    Log.d(TAG, log.substring(i, end))
                    i = end
                }
            }
        }

        private fun formatJson(json: String): String {
            return try {
                val trimmed = json.trim()
                when {
                    trimmed.startsWith("{") -> JSONObject(trimmed).toString(4)
                    trimmed.startsWith("[") -> JSONArray(trimmed).toString(4)
                    else -> trimmed
                }
            } catch (e: Exception) {
                json
            }
        }
    }
}