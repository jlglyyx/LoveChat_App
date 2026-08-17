@file:JvmName("GlideUtil")

package com.yang.lovechat.util

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.yang.lovechat.R
import com.yang.lovechat.app.BaseApplication
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.widget.BlurTransformation


/**
 * @ClassName: GlideUtil
 * @Description:
 * @Author: yxy
 * @Date: 2022/10/14 15:53
 */

private val placeholderImage = R.drawable.iv_placeholder

private val errorImage = R.drawable.iv_placeholder

val blurImageRequestOptions = RequestOptions.bitmapTransform(BlurTransformation(30,2))

val screenPx = getScreenPx(BaseApplication.mApplication)

val screenPxWidth = screenPx[0]

val screenPxHeight = screenPx[1]

val videoMimeTypes = arrayOf(
    "video/mp4",       // MP4
    "video/quicktime", // MOV
    "video/webm",      // WEBM
    "video/3gpp",      // 3GP
    "video/x-msvideo"  // AVI
)

val imageMimeTypes = arrayOf(
    "image/jpeg", // JPEG / JPG
    "image/png",  // PNG
    "image/webp", // WEBP
    "image/gif",  // GIF
    "image/bmp"   // BMP
)

//private val placeholderImage = R.drawable.iv_logo
//
//private val errorImage = R.drawable.iv_logo

private fun preloadRequestOptions(
    width: Int,
    height: Int,
    placeholder: Int = placeholderImage,
    error: Int = errorImage
): RequestOptions {
    return RequestOptions()
        .override(width, height)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .format(DecodeFormat.PREFER_RGB_565)
        .placeholder(placeholder)
        .error(error)
}

fun ImageView.loadImage(
    url: Any?,
    mContext: Context = this.context,
    isVideo: Boolean = false,
    width: Int = -1, height: Int = -1,
    placeholder: Int = placeholderImage,
    error: Int = errorImage,
    customOption: RequestOptions? = null
) {
    var requestOptions = preloadRequestOptions(width, height, placeholder, error)

    customOption?.let {
        requestOptions = requestOptions.apply(it)
    }

    Glide.with(mContext).apply {
        if (isVideo){
            asBitmap()
        } }
        .load(getRealUrl(url))
        .apply(requestOptions)
        .thumbnail(0.2f) // 先显示低清快图
        .into(this)
}


fun getRealUrl(url: Any?): Any {

    if (null == url) return placeholderImage

    return when (url) {
        is Uri -> url
        is String -> {
            if (url.contains("http")) {
                url
            } else if (url.contains("/storage") || url.startsWith("file://")) {
                url
            } else {

                if (url.startsWith("/")){
                    AppConstant.ClientInfo.BASE_FILE_URL.substring(
                        0,
                        AppConstant.ClientInfo.BASE_FILE_URL.length - 1
                    ) + url
                }else{
                    AppConstant.ClientInfo.BASE_FILE_URL+ url
                }
            }
        }

        else -> url
    }


}