package com.yang.lovechat.viewmodel

import android.net.Uri
import com.yang.lovechat.api.ApiService
import com.yang.lovechat.base.bus.EventBus
import com.yang.lovechat.base.bus.EventBus.postValue
import com.yang.lovechat.base.bus.SingleFlow
import com.yang.lovechat.base.viewmodel.BaseViewModel
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.MediaInfoData
import com.yang.lovechat.data.MessageData
import com.yang.lovechat.data.MessageResultData
import com.yang.lovechat.data.TagConfigData
import com.yang.lovechat.data.UpdateMediaInfoData
import com.yang.lovechat.data.UpdateUserInfoData
import com.yang.lovechat.data.UploadPictureData
import com.yang.lovechat.data.UserInfoData
import com.yang.lovechat.helper.IMHelper
import com.yang.lovechat.helper.MediaHelper.buildMultipartBody
import com.yang.lovechat.helper.UserInfoHold
import com.yang.lovechat.helper.UserInfoHold.updateLocalUserInfo
import com.yang.lovechat.http.HttpClient.createService
import com.yang.lovechat.util.formatListJson
import com.yang.lovechat.util.getCache
import com.yang.lovechat.util.setCache
import com.yang.lovechat.util.showShort
import com.yang.lovechat.util.toJson

open class PublicViewModel : BaseViewModel() {

    val mApiService = createService(ApiService::class.java)

    var pageNum = 1

    val mUserInfoData = SingleFlow<UserInfoData>()

    val mMatchUserInfoData = SingleFlow<UserInfoData>()

    val mUpdateUserInfoStatus = SingleFlow<Int>()

    val mConfigStatus = SingleFlow<Boolean>()


    val mUploadMediaData = SingleFlow<MutableList<MediaInfoData>>()

    val mAlbumMediaListData = SingleFlow<MutableList<MediaInfoData>>()

    val mUploadAlbumMediaListData = SingleFlow<MutableList<MediaInfoData>>()

    val mDeleteAlbumMediaListData = SingleFlow<MutableList<Long>>()

    val mUploadMediaListData = SingleFlow<MutableList<UploadPictureData>>()


    val mOrderNoData = SingleFlow<String>()

    val mPayOrderStatusData = SingleFlow<Boolean>()


    val mUserReportStatus = SingleFlow<Boolean>()


    val mSwipUserData = SingleFlow<Long>()

    val mSwipUserErrorStatus = SingleFlow<Any>()

    fun getUserInfo(userId: Long?) {

        if (null == userId || userId == -1L) return

        val params = mutableMapOf<String, Any?>()

        params[AppConstant.Constant.USER_ID] = userId

        launch({

            mApiService.getUserInfo(params)

        }, {

            if (null == UserInfoHold.userInfo || userId == UserInfoHold.userId) {

                updateLocalUserInfo(it.data)

                EventBus.with(AppConstant.EventConstant.EVENT_UPDATE_USER_INFO_NOW)
                    .postValue(it.data)
            }

            mUserInfoData.postValue(it.data)


        })

    }


    fun updateUserInfo(mUpdateUserInfoData: UpdateUserInfoData) {

        launch(onRequest = {

            mApiService.updateUserInfo(mUpdateUserInfoData)

        }, onSuccess = {

            mUpdateUserInfoStatus.postValue(0)

        }, onError = {


            requestFailEvent.postValue(it.message)

            false


        }, onException = {

            requestFailEvent.postValue(it.message.toString())

            false
        }, onErrorHandle = {

            showShort(it.message)
        })


    }


    fun getMatchUserInfo(userId: Long, convId: String) {

        if (userId == -1L) return

        val params = mutableMapOf<String, Any?>()

        params[AppConstant.Constant.USER_ID] = userId

        launch({

            mApiService.getUserInfo(params)

        }, {

            it.data.convId = convId

            mMatchUserInfoData.postValue(it.data)


        })

    }



    fun swipUser(swipType: Int, userId: Long) {

        val params = mutableMapOf<String, Any?>()

        //操作类型: 0-不喜欢, 1-普通喜欢, 2-付费快速建联 3-查看
        params["swipType"] = swipType

        params[AppConstant.Constant.USER_ID] = userId


        launch({

            mApiService.swipUser(params)

        }, {

            if (swipType == 2){
                mSwipUserData.postValue(userId)

                EventBus.with(AppConstant.EventConstant.EVENT_FAST_CONNECT_SUCCESS).postValue(userId)

            }
        }, onError = {


            false

        }, onErrorHandle = {

            mSwipUserErrorStatus.postValue(it)

        })

    }


    fun getTagConfig(tagType: Int? = null) {

        val params = mutableMapOf<String, Any?>()

        if (null != tagType) {
            params["tagType"] = tagType
        }

        launch({

            mApiService.getTagConfig(params)

        }, {

            val data = it.data

            val groupBy = data.groupBy { item -> item.tagType }

            groupBy.forEach { (key, value) ->

                when (key) {
//                   0 偏好, 1-兴趣, 2-职业, 3-性癖',
                    0 -> {
                        setCache(AppConstant.Constant.WANT, value.toJson())
                    }

                    1 -> {
                        setCache(AppConstant.Constant.INTEREST, value.toJson())
                    }

                    2 -> {
                        setCache(AppConstant.Constant.PROFESSION, value.toJson())

                    }

                    3 -> {
                        setCache(AppConstant.Constant.TURN, value.toJson())
                    }
                }

            }



            mConfigStatus.postValue(true)

        }, {
            mConfigStatus.postValue(false)
            false
        })

    }


    fun getCacheTagConfig(type: String): MutableList<TagConfigData> {

        val cache = getCache(type, "")

        if (cache.isNotEmpty()) {

            val tagConfigData = cache.formatListJson<TagConfigData>()

            return tagConfigData

        }

        return mutableListOf()
    }


    fun uploadMedia(files: MutableList<Uri>) {

        launch({

            val params = buildMultipartBody(files = files)

            mApiService.uploadMedia(params)

        }, {


            mUploadMediaData.postValue(it.data)

        }, onErrorHandle = {

            showShort(it.message)

        })

    }

    fun uploadMedias(list: MutableList<UploadPictureData>, message :MessageResultData<MessageData>? = null) {

        launch({

            val files = list.map { it.uri!! }.toMutableList()

            val params = buildMultipartBody(files = files)

            mApiService.uploadMedia(params)

        }, {

            list.forEachIndexed { index,item ->

                val get = it.data[index]

                item.fileUrl =  get.fileUrl

                item.coverUrl =  get.coverUrl
            }

            mUploadMediaListData.postValue(list)


            val message = message?:return@launch

            val data = message.data ?:return@launch

            data.let { result ->

                result.msgContent =  result.msgMediaContent?.mapIndexed { index, data ->

                    UpdateMediaInfoData(
                        mediaType = data.mediaType,
                        videoDuration = data.videoDuration,
                        fileUrl = list[index].fileUrl,
                        coverUrl = list[index].coverUrl,
                    )

                }.toJson()

//                result.msgMediaContent = null

                IMHelper.sendMessage(message)
            }

        }, onErrorHandle = {

            showShort(it.message)

        })

    }














    fun getAllConversationReadCount() {

        val params = mutableMapOf<String, Any?>()

        params[AppConstant.Constant.USER_ID] = UserInfoHold.userId



        launch({

            mApiService.getAllConversationReadCount(params)

        }, {


            EventBus.with(AppConstant.EventConstant.EVENT_UPDATE_ALL_CONVERSATION_READ_COUNT)
                .postValue(it.data)


        })

    }


    fun userReport(text: String,email: String,eventId: String,eventType: Int) {

        val params = mutableMapOf<String, Any?>()

        params["content"] = text

        params["contactEmail"] = email

        params["eventId"] = eventId

        params["eventType"] = eventType

        launch({

            mApiService.userReport(params)

        }, {

            mUserReportStatus.postValue(true)

        })

    }



    fun getAlbumMediaList() {

        val params = mutableMapOf<String, Any?>()

        params[AppConstant.Constant.PAGE_NUM] = pageNum

        params[AppConstant.Constant.PAGE_SIZE] = AppConstant.Constant.PAGE_SIZE_COUNT

        launch({

            mApiService.getAlbumMediaList(params)

        }, {

            mAlbumMediaListData.postValue(it.data)
        })

    }


    fun saveSourceAlbum(list : MutableList<UpdateMediaInfoData>) {

        val params = mutableMapOf<String, Any?>()

        params[AppConstant.Constant.MEDIAINFOS] = list

        launch({

            mApiService.saveSourceAlbum(params)

        }, {

            mUploadAlbumMediaListData.postValue(it.data)
        })

    }

    fun deleteSourceAlbum(list : MutableList<Long>) {

        val params = mutableMapOf<String, Any?>()

        params[AppConstant.Constant.MEDIAINFO_IDS] = list

        launch({

            mApiService.deleteSourceAlbum(params)

        }, {

            mDeleteAlbumMediaListData.postValue(list)
        })

    }

}