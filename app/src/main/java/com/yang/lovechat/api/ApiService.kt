package com.yang.lovechat.api

import com.yang.lovechat.data.AppVersion
import com.yang.lovechat.data.ConversationData
import com.yang.lovechat.data.MediaInfoData
import com.yang.lovechat.data.MessageData
import com.yang.lovechat.data.TagConfigData
import com.yang.lovechat.data.UpdateUserInfoData
import com.yang.lovechat.data.UserInfoData
import com.yang.lovechat.http.MResult
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @POST("api/app/checkNewVersion")
    suspend fun checkNewVersion(): MResult<AppVersion>

    @POST("api/chatter/login")
    suspend fun login(@Body mutableMap: MutableMap<String,Any?>): MResult<UserInfoData>

    @POST("api/user/getUserInfo")
    suspend fun getUserInfo(@Body mutableMap: MutableMap<String,Any?>): MResult<UserInfoData>

    @POST("api/user/updateUserInfo")
    suspend fun updateUserInfo(@Body mutableMap: UpdateUserInfoData): MResult<Any>

    @POST("api/user/getRecommendUserList")
    suspend fun getRecommendUserList(@Body mutableMap: MutableMap<String,Any?>): MResult<MutableList<UserInfoData>>

    @POST("api/user/swipUser")
    suspend fun swipUser(@Body mutableMap: MutableMap<String,Any?>): MResult<Any>





    @POST("api/user/getLikeIList")
    suspend fun getLikeIList(@Body mutableMap: MutableMap<String,Any?>): MResult<MutableList<UserInfoData>>

    @POST("api/user/getILikeList")
    suspend fun getILikeList(@Body mutableMap: MutableMap<String,Any?>): MResult<MutableList<UserInfoData>>

    @POST("api/user/getViewedIList")
    suspend fun getViewedIList(@Body mutableMap: MutableMap<String,Any?>): MResult<MutableList<UserInfoData>>

    @POST("api/user/getTagConfig")
    suspend fun getTagConfig(@Body mutableMap: MutableMap<String,Any?>): MResult<MutableList<TagConfigData>>

    @POST("api/user/upload")
    @Multipart
    suspend fun uploadMedia(@Part files: List<MultipartBody.Part>): MResult<MutableList<MediaInfoData>>

    @POST("api/user/userFeedback")
    suspend fun userFeedback(@Body mutableMap: MutableMap<String,Any?>): MResult<Any>

    @POST("api/user/userReport")
    suspend fun userReport(@Body mutableMap: MutableMap<String,Any?>): MResult<Any>


    @POST("api/user/getAlbumMediaList")
    suspend fun getAlbumMediaList(@Body mutableMap: MutableMap<String,Any?>): MResult<MutableList<MediaInfoData>>

    @POST("api/user/saveSourceAlbum")
    suspend fun saveSourceAlbum(@Body mutableMap: MutableMap<String,Any?>): MResult<MutableList<MediaInfoData>>


    @POST("api/user/deleteSourceAlbum")
    suspend fun deleteSourceAlbum(@Body mutableMap: MutableMap<String,Any?>): MResult<Boolean>







    @POST("api/chat/getConversationList")
    suspend fun getConversationList(@Body mutableMap: MutableMap<String,Any?>): MResult<MutableList<ConversationData>>

    @POST("api/chat/getConversationDetail")
    suspend fun getConversationDetail(@Body mutableMap: MutableMap<String,Any?>): MResult<ConversationData>

    @POST("api/chat/topConversation")
    suspend fun topConversation(@Body mutableMap: MutableMap<String,Any?>): MResult<Any>

    @POST("api/chat/deleteConversation")
    suspend fun deleteConversation(@Body mutableMap: MutableMap<String,Any?>): MResult<Any>

    @POST("api/chat/shieldConversation")
    suspend fun shieldConversation(@Body mutableMap: MutableMap<String,Any?>): MResult<Any>


    @POST("api/chat/getAllConversationReadCount")
    suspend fun getAllConversationReadCount(@Body mutableMap: MutableMap<String,Any?>): MResult<Int>


    @POST("api/chat/readConversation")
    suspend fun readConversation(@Body mutableMap: MutableMap<String,Any?>): MResult<Any>

    @POST("api/chat/readAllConversation")
    suspend fun readAllConversation(@Body mutableMap: MutableMap<String,Any?>): MResult<Any>


    @POST("api/message/getMessageHistory")
    suspend fun getMessageHistory(@Body mutableMap: MutableMap<String,Any?>): MResult<MutableList<MessageData>>

    @POST("api/message/unlockMedia")
    suspend fun unlockMedia(@Body mutableMap: MutableMap<String,Any?>): MResult<MessageData>




    @POST("api/user/deleteUser")
    suspend fun deleteUser(): MResult<Boolean>
}