package com.yang.lovechat.http

import com.google.gson.GsonBuilder
import com.yang.lovechat.constant.AppConstant
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object HttpClient {

    val mOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(InterceptorManager.HeadInterceptor())
        .addInterceptor(InterceptorManager.LogInterceptor())
        .connectTimeout(AppConstant.ClientInfo.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
        .readTimeout(AppConstant.ClientInfo.READ_TIMEOUT, TimeUnit.MILLISECONDS)
        .writeTimeout(AppConstant.ClientInfo.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
//        .hostnameVerifier { hostname, session -> true }
//        .ignoreAllSSL()
        .connectionPool(ConnectionPool())
        .build()

    val mChatOkHttpClient = OkHttpClient.Builder()
//        .addInterceptor(InterceptorManager.HeadInterceptor())
//        .addInterceptor(InterceptorManager.LogInterceptor())
        .connectTimeout(AppConstant.ClientInfo.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
        .readTimeout(AppConstant.ClientInfo.READ_TIMEOUT, TimeUnit.MILLISECONDS)
        .writeTimeout(AppConstant.ClientInfo.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
        .connectionPool(ConnectionPool())
        .build()

    private val mRetrofit = Retrofit.Builder().baseUrl(AppConstant.ClientInfo.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().disableHtmlEscaping().setDateFormat("yyyy-MM-dd HH:mm:ss").create()))
        .client(mOkHttpClient)
        .build()



    fun <T> createService(mService: Class<T>): T {
        return mRetrofit.create(mService)
    }

    fun OkHttpClient.Builder.ignoreAllSSL():OkHttpClient.Builder{

        val naiveTrustManager = object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) = Unit
            override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) = Unit
        }

        val insecureSocketFactory = SSLContext.getInstance("SSL").apply {
            val trustAllCerts = arrayOf<TrustManager>(naiveTrustManager)
            init(null, trustAllCerts, SecureRandom())
        }.socketFactory

        sslSocketFactory(insecureSocketFactory, naiveTrustManager)
        hostnameVerifier { _, _ -> true }

        return this
    }

}