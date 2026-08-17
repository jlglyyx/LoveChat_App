package com.yang.lovechat.dialog

import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.LoadState
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.yang.lovechat.R
import com.yang.lovechat.adapter.AlbumAdapter
import com.yang.lovechat.base.dialog.BaseDialog
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.MediaEnumType
import com.yang.lovechat.data.MediaInfoData
import com.yang.lovechat.data.PictureData
import com.yang.lovechat.data.UpdateMediaInfoData
import com.yang.lovechat.data.UploadPictureData
import com.yang.lovechat.data.UploadStatusEnumType
import com.yang.lovechat.databinding.DialogAlbumBinding
import com.yang.lovechat.helper.MediaHelper.getVideoDuration
import com.yang.lovechat.helper.MediaHelper.isVideo
import com.yang.lovechat.helper.PhotoPickerHelper
import com.yang.lovechat.util.bottomLoadListener
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.edgeToEdgeBottom
import com.yang.lovechat.util.showShort
import com.yang.lovechat.util.toJson
import com.yang.lovechat.viewmodel.PublicViewModel


class AlbumDialog : BaseDialog<DialogAlbumBinding>(DialogAlbumBinding::inflate) {

    private val mViewModel by activityViewModels<PublicViewModel>()

    private val mAlbumAdapter by lazy { AlbumAdapter() }

    private lateinit var mQuickAdapterHelper: QuickAdapterHelper


    var onConfirm: ((content: String) -> Unit)? = null

    var onCancel: (() -> Unit)? = null

    private val photoPicker = PhotoPickerHelper(this, maxCount = 9) { uris ->

        onPhotoPicked(uris.toMutableList())
    }


    companion object {

        fun newInstance() = AlbumDialog().apply {

            arguments = Bundle()
        }
    }


    override fun initView() {

        withViewBinding {

            root.edgeToEdgeBottom()

            ivClose.setOnClickListener {

                dismissAllowingStateLoss()
            }

            stvConfirm.clicks {

                if (mAlbumAdapter.selectList.isEmpty()) return@clicks

               val content =  mAlbumAdapter.selectList.map {item ->

                    UpdateMediaInfoData(
                        id = item.id,
                        mediaType = item.mediaType,
                        videoDuration = item.videoDuration,
                        fileUrl = item.fileUrl,
                        coverUrl = item.coverUrl
                    )

                }.toJson()



                onConfirm?.invoke(content)

                dismissAllowingStateLoss()
            }


            stvDelete.clicks {

                if (mAlbumAdapter.selectList.isEmpty()) return@clicks

                if (mAlbumAdapter.selectList.any { it.id == null }) return@clicks

                val content =  mAlbumAdapter.selectList.map {item -> item.id!! }.toMutableList()

                onCancel?.invoke()

                mViewModel.deleteSourceAlbum(content)

            }

            initRecyclerView()
        }

    }

    override fun initData() {

        mViewModel.pageNum = 1

        mViewModel.getAlbumMediaList()

    }


    private fun initRecyclerView() {

        withViewBinding {


            recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

            mQuickAdapterHelper = mAlbumAdapter.bottomLoadListener(onLoad = {

                mViewModel.pageNum++

                mViewModel.getAlbumMediaList()

            })
            recyclerView.adapter = mQuickAdapterHelper.adapter

            mAlbumAdapter.setOnDebouncedItemClick { adapter, view, position ->

                val data = adapter.getItem(position)

                if (data.type == AlbumAdapter.ALBUM_ADD) {

                    photoPicker.pick(MediaEnumType.IMAGE_AND_VIDEO, 0)

                } else {

                    val pictureData = PictureData()

                    pictureData.uri = data.uri

                    pictureData.url = data.fileUrl

                    pictureData.mediaEnumType = data.mediaEnumType

                    pictureData.coverUrl = data.coverUrl

                    PictureDetailDialog.newInstance(arrayListOf(pictureData), 1)
                        .show(parentFragmentManager)

                }

            }


            mAlbumAdapter.addOnItemChildClickListener(R.id.cl_num) { adapter, view, position ->

                val data = adapter.getItem(position)

                if (data.isSelect == true) {

                    data.isSelect = false

                    mAlbumAdapter.selectList.remove(data)

                    for (i in 0 until mAlbumAdapter.selectList.size) {
                        val pos = mAlbumAdapter.items.indexOf(mAlbumAdapter.selectList[i])
                        if (pos != -1) mAlbumAdapter.notifyItemChanged(pos, false)
                    }
                } else {

                    if (mAlbumAdapter.selectList.size >= 9) {

                        showShort("Max select 9")

                        return@addOnItemChildClickListener
                    }

                    data.isSelect = true

                    mAlbumAdapter.selectList.add(data)

                }

                adapter.notifyItemChanged(position, false)

            }


            mAlbumAdapter.add(
                0, MediaInfoData(
                    id = null,
                    mediaType = 0,
                    sourceType = 2,
                    isPrivate = false,
                    fileUrl = null,
                    coverUrl = null,
                    uri = null,
                    videoDuration = null,
                    unlockTime = null,
                    type = AlbumAdapter.ALBUM_ADD,
                    isSelect = null,
                    mediaEnumType = null
                )
            )

        }
    }


    override fun initViewModel() {
        super.initViewModel()

        mViewModel.mAlbumMediaListData.observe(this) {

            withViewBinding {
                if (mQuickAdapterHelper.trailingLoadState == LoadState.Loading) {

                    if (it.size < AppConstant.Constant.PAGE_SIZE_COUNT) {
                        mQuickAdapterHelper.trailingLoadState = LoadState.NotLoading(true)
                    } else {
                        mQuickAdapterHelper.trailingLoadState = LoadState.NotLoading(false)
                    }

                    it.forEach { item ->
                        item.mediaEnumType =
                            if (item.mediaType == 0) MediaEnumType.IMAGE else MediaEnumType.VIDEO
                    }

                    mAlbumAdapter.addAll(it)

                } else {
                    if (it.size < AppConstant.Constant.PAGE_SIZE_COUNT) {
                        mQuickAdapterHelper.trailingLoadState = LoadState.NotLoading(true)
                    } else {
                        mQuickAdapterHelper.trailingLoadState = LoadState.NotLoading(false)
                    }

                    it.forEach { item ->
                        item.mediaEnumType =
                            if (item.mediaType == 0) MediaEnumType.IMAGE else MediaEnumType.VIDEO
                    }
                    mAlbumAdapter.addAll(it)
                }

            }
        }


        mViewModel.mUploadMediaListData.observe(this) {

            it.forEach { item ->
                val index =
                    mAlbumAdapter.items.indexOfFirst { find -> find.id == item.id }

                if (index != -1) {

                    val itemOrNull = mAlbumAdapter.getItemOrNull(index)

                    itemOrNull?.apply {

                        fileUrl = item.fileUrl

                        coverUrl = item.coverUrl

                        uploadStatus = UploadStatusEnumType.SUCCESS

                    }

                }

            }


            val mPictureList = it.map { map ->

                UpdateMediaInfoData(
                    mediaType = if (map.mediaEnumType == MediaEnumType.IMAGE || map.mediaEnumType == MediaEnumType.LOCAL_IMAGE) 0 else 1,
                    isPrivate = map.isPrivate,
                    fileUrl = map.fileUrl!!,
                    coverUrl = map.coverUrl,
                    videoDuration = map.videoDuration,
                )

            }.toMutableList()

            mViewModel.saveSourceAlbum(mPictureList)

        }


        mViewModel.mUploadAlbumMediaListData.observe(this) {

            it.forEach { item ->
                val index =
                    mAlbumAdapter.items.indexOfFirst { find -> find.fileUrl == item.fileUrl }

                if (index != -1) {

                    val itemOrNull = mAlbumAdapter.getItemOrNull(index)

                    itemOrNull?.apply {

                        id = item.id
                    }

                    mAlbumAdapter.notifyItemChanged(index, false)
                }

            }

        }

        mViewModel.mDeleteAlbumMediaListData.observe(this) {

            it.forEach { item ->
                val index =
                    mAlbumAdapter.items.indexOfFirst { find -> find.id == item }

                if (index != -1) {

                    val itemOrNull = mAlbumAdapter.getItemOrNull(index)

                    itemOrNull?.apply {

                        mAlbumAdapter.remove(itemOrNull)

                        mAlbumAdapter.selectList.remove(itemOrNull)
                    }

                }

            }

        }

    }


    private fun onPhotoPicked(uris: MutableList<Uri>) {


        val list = uris.map {

            val item = UploadPictureData()

            val isVideo = it.isVideo(requireContext())

            item.mediaEnumType =
                if (isVideo) MediaEnumType.LOCAL_VIDEO else MediaEnumType.LOCAL_IMAGE

            if (isVideo) {
                item.videoDuration = it.getVideoDuration(requireContext()).toInt()
            }

            item.uri = it

            item

        }.toMutableList()




        mViewModel.uploadMedias(list)


        val mediaList = list.map { item ->

            MediaInfoData(
                id = item.id,
                mediaType = if (item.mediaEnumType == MediaEnumType.LOCAL_IMAGE) 0 else 1,
                sourceType = 3,
                isPrivate = false,
                fileUrl = null,
                coverUrl = null,
                uri = item.uri,
                videoDuration = null,
                unlockTime = null,
                type = AlbumAdapter.ALBUM_ITEM,
                isSelect = null,
                mediaEnumType = item.mediaEnumType,
                uploadStatus = UploadStatusEnumType.LOADING
            )
        }

        mAlbumAdapter.addAll(1, mediaList)


    }


    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }

    override fun setDialogHeight(): Int {
        return mScreenHeight * 8 / 9
    }


}