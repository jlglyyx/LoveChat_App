package com.yang.lovechat.ui.activity


import android.net.Uri
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.flexbox.FlexboxLayoutManager
import com.yang.lovechat.R
import com.yang.lovechat.adapter.AddPictureAdapter
import com.yang.lovechat.adapter.InterestAdapter
import com.yang.lovechat.base.activity.BaseActivity
import com.yang.lovechat.base.bus.EventBus
import com.yang.lovechat.base.bus.EventBus.postValue
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.MediaEnumType
import com.yang.lovechat.data.PictureData
import com.yang.lovechat.data.TagConfigData
import com.yang.lovechat.data.UpdateMediaInfoData
import com.yang.lovechat.data.UpdateUserInfoData
import com.yang.lovechat.data.UpdateUserTagData
import com.yang.lovechat.data.UploadPictureData
import com.yang.lovechat.data.UploadStatusEnumType
import com.yang.lovechat.data.UserInfoData
import com.yang.lovechat.databinding.ActEditUserInfoBinding
import com.yang.lovechat.dialog.EditAvatarDialog
import com.yang.lovechat.dialog.EditBirthDialog
import com.yang.lovechat.dialog.EditInputDialog
import com.yang.lovechat.dialog.EditParamDialog
import com.yang.lovechat.dialog.PictureDetailDialog
import com.yang.lovechat.helper.MediaHelper.getVideoDuration
import com.yang.lovechat.helper.MediaHelper.isVideo
import com.yang.lovechat.helper.PhotoPickerHelper
import com.yang.lovechat.helper.UserInfoHold
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.dateFormat
import com.yang.lovechat.util.edgeToEdgeBottom
import com.yang.lovechat.util.loadImage
import com.yang.lovechat.util.toZodiac
import com.yang.lovechat.viewmodel.MineViewModel
import java.util.Date
import java.util.Locale
import kotlin.collections.forEach


class EditUserInfoActivity :
    BaseActivity<ActEditUserInfoBinding, MineViewModel>(ActEditUserInfoBinding::inflate) {

    private val mInterestAdapter by lazy { InterestAdapter() }

    private val mAddPictureAdapter: AddPictureAdapter by lazy { AddPictureAdapter() }


    private var maxCount = 9

    private val mUpdateUserInfoData = UpdateUserInfoData()

    private val photoPicker = PhotoPickerHelper(this, maxCount = 9) { uris ->

        onPhotoPicked(uris.toMutableList())
    }


    override fun initView() {


        withViewBinding {

            root.edgeToEdgeBottom()

            appToolBar.mToolbarBinding.stvRight.visibility = View.VISIBLE

            appToolBar.mToolbarBinding.stvRight.clicks {

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


                val userTagList = mInterestAdapter.items.filter { item -> item.isCheck }.map {item ->

                    UpdateUserTagData(item.id, item.tagType)

                }.toMutableList()

                mUpdateUserInfoData.interest = userTagList

                mUpdateUserInfoData.backgroundMediaList = mPictureList

                mViewModel.updateUserInfo(mUpdateUserInfoData)


            }





            stvBio.clicks {

                EditInputDialog.newInstance(stvBio.text.toString(), 1).apply {

                    onConfirm = {

                        stvBio.text = it

                        mUpdateUserInfoData.bio = it
                    }

                }.show(supportFragmentManager)

            }


            llName.clicks {

                EditInputDialog.newInstance(stvName.text.toString(), 0).apply {

                    onConfirm = {

                        stvName.text = it

                        mUpdateUserInfoData.userName = it
                    }

                }.show(supportFragmentManager)

            }

            llAvatar.clicks {


                val avatar = if (mUpdateUserInfoData.avatarUrl.isNullOrEmpty()) {
                    UserInfoHold.userInfo?.avatarUrl ?: ""
                } else {
                    mUpdateUserInfoData.avatarUrl?:""
                }

                EditAvatarDialog.newInstance(avatar).apply {

                    onConfirm = { remotePath, localUrl ->

                        if (remotePath.isNotEmpty()) {


                            sivAvatar.loadImage(localUrl)

                            mUpdateUserInfoData.avatarUrl = remotePath

                        }

                    }
                }.show(supportFragmentManager)



            }



            llBirth.clicks {

                EditBirthDialog.newInstance(stvBirth.text.toString()).apply {

                    onConfirm = { it, age ->

                        if (null != it) {

                            stvBirth.text = it.dateFormat("MM/dd/yyyy", Locale.US)

                            val toZodiac = it.toZodiac(this.requireContext())

                            stvConstellation.text = toZodiac

                            mUpdateUserInfoData.birth = it.time

                            mUpdateUserInfoData.age = age

                            mUpdateUserInfoData.constellation = toZodiac

                        }

                    }

                }.show(supportFragmentManager)

            }

            llHeight.clicks {

                initEditParamDialog(stvHeight, 1) { it, _ ->

                    val number = it.takeWhile { take -> take.isDigit() }

                    mUpdateUserInfoData.height = number
                }

            }

            llWeight.clicks {

                initEditParamDialog(stvWeight, 0) { it, _ ->

                    val number = it.takeWhile { take -> take.isDigit() }

                    mUpdateUserInfoData.weight = number
                }

            }

            stvIntent.clicks {

                initEditParamDialog(stvIntent, 3) { it, data ->
                    mUpdateUserInfoData.intent = it

//                    data?.let {
//                        mUpdateUserInfoData.interest = mutableListOf(UpdateUserTagData(data.id, data.tagType))
//                    }

                }

            }
            llProfession.clicks {

                initEditParamDialog(stvProfession, 2) { it, _ ->
                    mUpdateUserInfoData.profession = it
                }

            }


            initPictureRecyclerView()

            initInterestRecyclerView()
        }


    }


    override fun initData() {

        UserInfoHold.userId?.let {
            mViewModel.getUserInfo(it)
        }


    }

    override fun initViewModel() {


        mViewModel.mUserInfoData.observe(this) {

            initUserInfo(it)

        }



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

        }


        mViewModel.mUpdateUserInfoStatus.observe(this) {

            EventBus.with(AppConstant.EventConstant.EVENT_REFRESH_MY_USER_INFO).postValue(true)

            finish()
        }

    }


    private fun initUserInfo(mUserInfoData: UserInfoData) {


        withViewBinding {

            val data = mUserInfoData.backgroundMediaList?.map { item ->
                UploadPictureData().apply {
                    fileUrl = item.fileUrl
                    coverUrl = item.coverUrl
                    mediaEnumType =
                        if (item.mediaType == 0) MediaEnumType.IMAGE else MediaEnumType.VIDEO
                    uploadStatus = UploadStatusEnumType.SUCCESS
                }
            }

            mAddPictureAdapter.submitList(data)

            addOrRemoveLastItem()

            stvIntent.text = "${mUserInfoData.intent}"

            stvBio.text = "${mUserInfoData.bio ?: ""}"

            stvName.text = mUserInfoData.userName

            sivAvatar.loadImage(UserInfoHold.userInfo?.avatarUrl ?: R.drawable.iv_avatar)

            stvBirth.text = "${Date(mUserInfoData.birth).dateFormat("MM/dd/yyyy", Locale.US)}"

            stvConstellation.text =
                "${mUserInfoData.constellation ?: Date(mUserInfoData.birth).toZodiac(this@EditUserInfoActivity)}"

            stvHeight.text = "${mUserInfoData.height}cm"

            stvWeight.text = "${mUserInfoData.weight}kg"


            val interest = mUserInfoData.interest?.map { it.id }?.toSet()

            val tagConfigData = mViewModel.getCacheTagConfig(AppConstant.Constant.INTEREST)

            tagConfigData.forEach {

                it.isCheck = interest?.contains(it.id) ?: false
            }


            mInterestAdapter.submitList(tagConfigData)

        }


    }


    private fun initPictureRecyclerView() {


        withViewBinding {


            pictureRecyclerView.adapter = mAddPictureAdapter

            pictureRecyclerView.layoutManager = GridLayoutManager(this@EditUserInfoActivity, 3)

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

                        val position = if (filter.size == mAddPictureAdapter.itemCount) {

                            position

                        } else {

                            position - 1
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

            }


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


    private fun initInterestRecyclerView() {

        withViewBinding {

            interestRecyclerView.adapter = mInterestAdapter

            interestRecyclerView.layoutManager = FlexboxLayoutManager(this@EditUserInfoActivity)


            mInterestAdapter.setOnItemClickListener { adapter, view, position ->

                val item = adapter.getItem(position)

                item.isCheck = !item.isCheck

                mInterestAdapter.notifyItemChanged(position, false)

            }


        }

    }

    private fun initEditParamDialog(textView: TextView, type: Int, block: (String, TagConfigData?) -> Unit) {


        EditParamDialog.newInstance(textView.text.toString(), type).apply {

            onConfirm = { it, data ->

                textView.text = it

                block(it,null)


            }

        }.show(supportFragmentManager)
    }
}