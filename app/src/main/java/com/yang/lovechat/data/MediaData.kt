package com.yang.lovechat.data

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize




class UploadPictureData {

    var uri: Uri? = null

    var fileUrl: String? = null

    var coverUrl: String? = null

    var mediaEnumType: MediaEnumType? = null

    var uploadStatus: UploadStatusEnumType? = null

    var sourceType: Int? = null

    var isPrivate: Boolean = false

    var videoDuration: Int? = null

    var id: Long = System.currentTimeMillis()+(Math.random() * 1000).toLong()
}




@Parcelize
class PictureData(

    var url: String? = null,

    var uri: Uri? = null,

    var coverUrl: String? = null,

    var mediaEnumType: MediaEnumType? = null,

    ) : Parcelable