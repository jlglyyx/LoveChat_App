package com.yang.lovechat.data

import android.net.Uri
import android.os.Parcelable
import com.yang.lovechat.adapter.AlbumAdapter
import kotlinx.parcelize.Parcelize


@Parcelize
class MediaInfoData(

    var id: Long? = null,

    var mediaType: Int,

    var sourceType: Int,

    var isPrivate: Boolean = false,

    var fileUrl: String? = null,

    var coverUrl: String? = null,

    var uri: Uri? = null,

    var videoDuration: Int? = null,

    var unlockTime : Long? = null,

    var type: Int? = AlbumAdapter.ALBUM_ITEM,

    var isSelect: Boolean? = false,

    var mediaEnumType: MediaEnumType? = null,

    var uploadStatus: UploadStatusEnumType? = null

    ) : Parcelable{



}


class UpdateMediaInfoData(

    var id: Long? = null,

    var mediaType: Int,

    var isPrivate: Boolean? = null,

    var fileUrl: String? = null,

    var coverUrl: String? = null,

    var videoDuration: Int? = null,


    )
