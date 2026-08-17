package com.yang.lovechat.adapter

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.yang.lovechat.R
import com.yang.lovechat.app.BaseApplication
import com.yang.lovechat.base.adapter.BaseMultiItemAdapter
import com.yang.lovechat.data.MediaEnumType
import com.yang.lovechat.data.MediaInfoData
import com.yang.lovechat.data.UploadStatusEnumType
import com.yang.lovechat.databinding.ItemAddAlbumBinding
import com.yang.lovechat.databinding.ItemAlbumBinding
import com.yang.lovechat.util.getScreenPx
import com.yang.lovechat.util.loadImage
import com.yang.lovechat.util.viewVisibility

class AlbumAdapter : BaseMultiItemAdapter<MediaInfoData>() {

    private val pictureWidth = getScreenPx(BaseApplication.mApplication)[0] / 3

    private val pictureHeight = pictureWidth * 4 / 3

    val selectList = mutableListOf<MediaInfoData>()

    companion object {

        const val ALBUM_ADD = 0

        const val ALBUM_ITEM = 1

    }

    init {


        addItemType(
            ALBUM_ADD,
            object : BaseMultiItemViewHolder<MediaInfoData, ItemAddAlbumBinding>(
                ItemAddAlbumBinding::inflate
            ) {

                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemAddAlbumBinding>,
                    position: Int,
                    item: MediaInfoData?
                ) {
                    try {
                        item?.let {

                            holder.viewBinding.apply {


                            }


                        }
                    } catch (e: Exception) {
                        e.printStackTrace()

                    }
                }

            })
        addItemType(
            ALBUM_ITEM,
            object : BaseMultiItemViewHolder<MediaInfoData, ItemAlbumBinding>(
                ItemAlbumBinding::inflate
            ) {

                override fun onBind(
                    holder: BaseRecyclerViewHolder<ItemAlbumBinding>,
                    position: Int,
                    item: MediaInfoData?
                ) {
                    try {
                        item?.let {

                            holder.viewBinding.apply {

                                val isVideo = item.mediaType != 0 || item.mediaEnumType == MediaEnumType.LOCAL_VIDEO || item.mediaEnumType == MediaEnumType.VIDEO

                                ivVideo.visibility = if (isVideo) VISIBLE else GONE


                                when (item.mediaEnumType) {

                                    MediaEnumType.LOCAL_IMAGE, MediaEnumType.LOCAL_VIDEO  -> {

                                        when(item.uploadStatus) {

                                            UploadStatusEnumType.LOADING ->{

                                                viewVisibility(View.VISIBLE, ivLoad)

                                                viewVisibility(View.GONE, clNum,ivError,stvSendStatus)

                                            }
                                            UploadStatusEnumType.SUCCESS ->{

                                                viewVisibility(View.VISIBLE, clNum)

                                                viewVisibility(View.GONE, ivLoad, ivError,stvSendStatus)


                                            }
                                            UploadStatusEnumType.FAILED ->{

                                                viewVisibility(View.VISIBLE, clNum,ivError)

                                                viewVisibility(View.GONE, ivLoad, stvSendStatus)
                                            }

                                            else -> {
                                                viewVisibility(View.VISIBLE, ivLoad)

                                                viewVisibility(View.GONE, clNum,ivError,stvSendStatus)

                                            }
                                        }

                                        viewVisibility(View.VISIBLE, ivImage)

                                        ivImage.loadImage( item.uri?:item.fileUrl, width = pictureWidth, height =  pictureHeight)
                                    }


                                    MediaEnumType.IMAGE -> {

                                        viewVisibility(View.VISIBLE, clNum,ivImage)

                                        viewVisibility(View.GONE, ivLoad, ivError,stvSendStatus)

                                        ivImage.loadImage(item.fileUrl, width = pictureWidth, height =  pictureHeight)
                                    }

                                    MediaEnumType.VIDEO -> {

                                        viewVisibility(View.VISIBLE, clNum,ivImage)

                                        viewVisibility(View.GONE,  ivLoad, ivError,stvSendStatus)

                                        ivImage.loadImage(item.coverUrl, width = pictureWidth, height =  pictureHeight)
                                    }

                                    else -> {


                                        viewVisibility(View.VISIBLE, ivImage)

                                        viewVisibility(View.GONE, clNum,ivLoad, ivError,stvSendStatus)

                                        ivImage.loadImage(item.fileUrl, width = pictureWidth, height =  pictureHeight)

                                    }
                                }




                                val selectedIndex = selectList.indexOf(it)

                                if (selectedIndex != -1) {
                                    stvNum.text = "${selectedIndex+1}"
                                    stvNum.shapeDrawableBuilder.setSolidColor(context.getColor(R.color.endColor)).intoBackground()
                                } else {
                                    stvNum.text = ""
                                    stvNum.shapeDrawableBuilder.setSolidColor(context.getColor(R.color.transparent)).intoBackground()
                                }

                            }

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()

                    }
                }

            })

        onItemViewType { position, list ->

            list[position].type?:ALBUM_ITEM


        }

    }


}