package com.yang.lovechat.adapter

import android.view.View
import com.yang.lovechat.app.BaseApplication
import com.yang.lovechat.base.adapter.BaseRecyclerAdapter
import com.yang.lovechat.data.MediaEnumType
import com.yang.lovechat.data.UploadPictureData
import com.yang.lovechat.data.UploadStatusEnumType

import com.yang.lovechat.databinding.ItemAddPhotoBinding
import com.yang.lovechat.util.getScreenPx
import com.yang.lovechat.util.loadImage
import com.yang.lovechat.util.viewVisibility
import kotlin.apply

class AddPictureAdapter :
    BaseRecyclerAdapter<UploadPictureData, ItemAddPhotoBinding>(ItemAddPhotoBinding::inflate) {

    private val pictureWidth = getScreenPx(BaseApplication.mApplication)[0] / 3

    private val pictureHeight = pictureWidth * 4 / 3



    override fun convert(
        holder: BaseRecyclerViewHolder<ItemAddPhotoBinding>,
        itemView: ItemAddPhotoBinding,
        item: UploadPictureData,
        position: Int
    ) {

        itemView.apply {

            when (item.mediaEnumType) {

                MediaEnumType.LOCAL_IMAGE, MediaEnumType.LOCAL_VIDEO  -> {

                    when(item.uploadStatus) {

                        UploadStatusEnumType.LOADING ->{

                            viewVisibility(View.VISIBLE, ivLoad)

                            viewVisibility(View.GONE, ivDelete,ivAdd,ivError,stvSendStatus)

                        }
                        UploadStatusEnumType.SUCCESS ->{

                            viewVisibility(View.VISIBLE, ivDelete)

                            viewVisibility(View.GONE, ivAdd, ivLoad, ivError,stvSendStatus)


                        }
                        UploadStatusEnumType.FAILED ->{

                            viewVisibility(View.VISIBLE, ivDelete,ivError)

                            viewVisibility(View.GONE, ivAdd, ivLoad, stvSendStatus)
                        }

                        else -> {
                            viewVisibility(View.VISIBLE, ivLoad)

                            viewVisibility(View.GONE, ivDelete,ivAdd,ivError,stvSendStatus)

                        }
                    }

                    viewVisibility(View.VISIBLE, ivImage)

                    ivImage.loadImage( item.uri?:item.fileUrl, width = pictureWidth, height =  pictureHeight)
                }


                MediaEnumType.IMAGE -> {

                    viewVisibility(View.VISIBLE, ivImage, ivDelete)

                    viewVisibility(View.GONE, ivAdd, ivLoad, ivError,stvSendStatus)

                    ivImage.loadImage(item.fileUrl, width = pictureWidth, height =  pictureHeight)
                }

                MediaEnumType.VIDEO -> {

                    viewVisibility(View.VISIBLE, ivImage, ivDelete)

                    viewVisibility(View.GONE, ivAdd, ivLoad, ivError,stvSendStatus)

                    ivImage.loadImage(item.coverUrl, width = pictureWidth, height =  pictureHeight)
                }

                else -> {

                    viewVisibility(View.VISIBLE, ivAdd)

                    viewVisibility(View.GONE, ivImage, ivLoad, ivDelete, ivError,svCover,stvSendStatus)

                }
            }

        }

    }



}