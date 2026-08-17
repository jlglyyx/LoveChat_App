package com.yang.lovechat.http

import android.os.Build
import android.util.Base64
import android.util.Log
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.NetworkUtils
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.util.getCache
import com.yang.lovechat.util.toJson
import okio.Buffer
import java.io.EOFException
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object HttpEncrypt {

    private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding"
    private const val IV_SIZE = 12     // GCM 标准 IV 长度
    private const val TAG_SIZE = 128    // 认证标签长度 (位)
    private const val AES_KEY_BIT = 256 // 使用 AES-256

    private const val PUBLIC_KEY = AppConstant.Constant.PUBLIC_KEY

    /**
     * 1. 生成 256 位强随机 SessionKey (返回原始字节)
     * 不再使用 String 包装，保证最大安全性
     */
    fun generateSessionKey(): ByteArray {
        val key = ByteArray(AES_KEY_BIT / 8)
        SecureRandom().nextBytes(key)
        return key
    }

    /**
     * 2. RSA 加密 SessionKey (x-arg)
     * 将生成的 AES 密钥发送给服务器
     */
    fun rsaSealKey(sessionKey: ByteArray): String {
        val keyBytes = Base64.decode(PUBLIC_KEY, Base64.NO_WRAP)
        val spec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKey = keyFactory.generatePublic(spec)

        val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encrypted = cipher.doFinal(sessionKey)
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }


    /**
     * 3. AES-GCM 加密业务数据
     * 返回结果包含: IV(12B) + CipherText + Tag
     */
    fun aesEncrypt(sessionKey: ByteArray, plainText: String): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val iv = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(iv) // 每次加密生成新 IV

        val spec = GCMParameterSpec(TAG_SIZE, iv)
        val secretKey = SecretKeySpec(sessionKey, "AES")

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // 拼接 IV + 密文 (GCM 会自动把 Tag 附在密文后面)
        val combined = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }


    /**
     * 4. AES-GCM 解密服务端响应
     */
    fun aesDecrypt(sessionKey: ByteArray, encryptedBase64: String): String {
        val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
        if (combined.size < IV_SIZE) throw IllegalArgumentException("Invalid data")

        val iv = combined.copyOfRange(0, IV_SIZE)
        val cipherText = combined.copyOfRange(IV_SIZE, combined.size)

        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_SIZE, iv)
        val secretKey = SecretKeySpec(sessionKey, "AES")

        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return String(cipher.doFinal(cipherText), Charsets.UTF_8)
    }





    data class ClientInfoData(
        val requestId: String, // 每次请求唯一的 ID，方便后端链路追踪
        val timestamp: Long,// 请求发起时间
        val appName: String,// 应用名称标识
        val token: String,// 用户登录凭证
        val platform: String, // 平台
        val osVersion: String, // 安卓系统版本号 (如: 14)
        val appVersion: String,
        val appVersionCode: String,
        val deviceBrand: String,// 手机品牌
        val deviceModel: String,  // 手机型号
        val androidId: String?, // 可空字段
        val language: String,// 系统语言
        val timezone: String,// 设备时区 (如: Asia/Shanghai)
        val networkType: String,// 网络类型 (4G, 5G, WIFI)
        val operator: String// 运营商
    )

    fun getInfoMap(): String {
        try {
           val clientInfoData =  ClientInfoData(
                requestId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                appName = "Pulse",
                token = getCache(AppConstant.Constant.TOKEN, ""),
                platform = "Android",
                osVersion = Build.VERSION.RELEASE,
                appVersion = AppUtils.getAppVersionName(),
                appVersionCode = "${AppUtils.getAppVersionCode()}",
                deviceBrand = Build.BRAND,
                deviceModel = DeviceUtils.getModel(),
                androidId =  "${DeviceUtils.getUniqueDeviceId()}_${DeviceUtils.getAndroidID()}",
//                androidId = if (getCache(AppConstant.Constant.IS_ALLOW, false)) DeviceUtils.getAndroidID() else null,
                language = Locale.getDefault().language,
                timezone = TimeZone.getDefault().id,
                networkType = NetworkUtils.getNetworkType().name,
                operator = NetworkUtils.getNetworkOperatorName()
            )

            val data = clientInfoData.toJson()

            Log.e("TAG", "intercept: $data")

            return data

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    internal fun Buffer.checkIsUtf8(): Boolean {
        return try {
            val prefix = Buffer()
            val byteCount = this.size.coerceAtMost(64)
            this.copyTo(prefix, 0, byteCount)

            for (i in 0 until 16) {
                if (prefix.exhausted()) break
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            true
        } catch (e: EOFException) {
            false
        }
    }
}