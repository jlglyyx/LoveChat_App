package com.yang.lovechat.ui.activity

import android.net.Uri
import androidx.recyclerview.widget.GridLayoutManager
import com.yang.lovechat.R
import com.yang.lovechat.adapter.AddPictureAdapter
import com.yang.lovechat.base.activity.BaseActivity
import com.yang.lovechat.data.MediaEnumType
import com.yang.lovechat.data.PictureData
import com.yang.lovechat.data.UpdateMediaInfoData
import com.yang.lovechat.data.UpdateUserInfoData
import com.yang.lovechat.data.UploadPictureData
import com.yang.lovechat.data.UploadStatusEnumType
import com.yang.lovechat.databinding.ActCStepBinding
import com.yang.lovechat.dialog.PictureDetailDialog
import com.yang.lovechat.helper.MediaHelper.getVideoDuration
import com.yang.lovechat.helper.MediaHelper.isVideo
import com.yang.lovechat.helper.PhotoPickerHelper
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.createIntent
import com.yang.lovechat.util.edgeToEdgeAll
import com.yang.lovechat.util.startActivity
import com.yang.lovechat.viewmodel.MainViewModel


class CStepActivity :
    BaseActivity<ActCStepBinding, MainViewModel>(ActCStepBinding::inflate) {


    private val mAddPictureAdapter: AddPictureAdapter by lazy { AddPictureAdapter() }


    private var maxCount = 9

    private val photoPicker = PhotoPickerHelper(this, maxCount = maxCount) { uris ->

        onPhotoPicked(uris.toMutableList())
    }


    override fun initView() {

        mViewBinding.root.edgeToEdgeAll()

        mViewBinding.apply {


            initPictureRecyclerView()


            stvNext.clicks {

                val mPictureList =
                    mAddPictureAdapter.items.filter { it.uploadStatus == UploadStatusEnumType.SUCCESS }
                        .map { map ->

                            UpdateMediaInfoData(
                                mediaType = if (map.mediaEnumType == MediaEnumType.IMAGE || map.mediaEnumType == MediaEnumType.LOCAL_IMAGE) 0 else 1,
                                isPrivate = map.isPrivate,
                                fileUrl = map.fileUrl!!,
                                coverUrl =  map.coverUrl,
                                videoDuration =  map.videoDuration,
                            )

                        }.toMutableList()

                val mUpdateUserInfoData = UpdateUserInfoData()

                if (mPictureList.isEmpty()) return@clicks

                mUpdateUserInfoData.avatarUrl = mPictureList[0].fileUrl

                mUpdateUserInfoData.backgroundMediaList = mPictureList

                mViewModel.updateUserInfo(mUpdateUserInfoData)

                showLoading()

            }
        }


    }

    override fun initViewModel() {



        mViewModel.mUploadMediaListData.observe(this) {

            it.forEach { item ->
                val findLast =
                    mAddPictureAdapter.items.findLast { find -> find.id == item.id }
                findLast?.apply {
                    fileUrl = item.fileUrl

                    coverUrl = item.coverUrl

                    uploadStatus = UploadStatusEnumType.SUCCESS
                }

            }

            mAddPictureAdapter.notifyItemRangeChanged(0, mAddPictureAdapter.itemCount, false)

            canNext()

        }



        mViewModel.mUpdateUserInfoStatus.observe(this) {

            dismissLoading()

            createIntent(MainActivity::class.java).startActivity(this@CStepActivity)
        }

        mViewModel.requestFailEvent.observe(this){

            dismissLoading()
        }


    }


    override fun initData() {


    }

    private fun initPictureRecyclerView() {


        withViewBinding {


            pictureRecyclerView.adapter = mAddPictureAdapter

            pictureRecyclerView.layoutManager = GridLayoutManager(this@CStepActivity, 3)

            mAddPictureAdapter.setOnItemClickListener { _, _, position ->

                val item = mAddPictureAdapter.getItem(position)

                when (item.mediaEnumType) {

                    null -> {

                        photoPicker.pick(
                            MediaEnumType.IMAGE,
                            mAddPictureAdapter.items.filter { null != it.mediaEnumType }.size
                        )

                    }

                    else -> {

                        val filter =
                            mAddPictureAdapter.items.filter { null != it.mediaEnumType }.map { map ->

                                val pictureData = PictureData()

                                pictureData.uri = map.uri

                                pictureData.url = map.fileUrl

                                pictureData.mediaEnumType = map.mediaEnumType

                                pictureData.coverUrl = map.coverUrl

                                pictureData

                            }

                        val position =  if (filter.size == mAddPictureAdapter.itemCount){

                            position

                        }else{

                            position-1
                        }

                        PictureDetailDialog.newInstance(filter, position)
                            .show(supportFragmentManager)


                    }
                }

            }

            mAddPictureAdapter.addOnItemChildClickListener(R.id.iv_delete) { _, _, position ->


                val item = mAddPictureAdapter.getItem(position)

                mAddPictureAdapter.remove(item)

                addOrRemoveLastItem()

                canNext()
            }

            addOrRemoveLastItem()


        }

    }





    private fun onPhotoPicked(uris: MutableList<Uri>) {

        val list = uris.map {

            val item = UploadPictureData()

            val isVideo = it.isVideo(this)

            item.mediaEnumType = if (isVideo) MediaEnumType.LOCAL_VIDEO else MediaEnumType.LOCAL_IMAGE

            if (isVideo) {
                item.videoDuration = it.getVideoDuration(this).toInt()
            }

            item.uri = it

            item

        }.toMutableList()

        mAddPictureAdapter.addAll(list)


        mViewModel.uploadMedias(list)

        addOrRemoveLastItem()


    }

    private fun addOrRemoveLastItem() {

        val items = mAddPictureAdapter.items

        val addBtnIndex = items.indexOfFirst { it.mediaEnumType == null }

        val realImageCount = items.count { it.mediaEnumType != null }

        if (realImageCount >= maxCount) {
            if (addBtnIndex != -1) {
                mAddPictureAdapter.removeAt(addBtnIndex)
            }
        } else {
            if (addBtnIndex == -1) {
                mAddPictureAdapter.add(0, UploadPictureData())
            }
        }

    }


    private fun canNext(){

        mViewBinding.stvNext.isEnabled =  mAddPictureAdapter.items.any { it.uploadStatus == UploadStatusEnumType.SUCCESS }
    }

}