package com.yang.lovechat.dialog

import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.fragment.app.activityViewModels
import com.yang.lovechat.app.BaseApplication
import com.yang.lovechat.base.dialog.BaseDialog
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.MediaEnumType
import com.yang.lovechat.data.UploadPictureData
import com.yang.lovechat.databinding.DialogEditAvatarBinding
import com.yang.lovechat.helper.PhotoPickerHelper
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.edgeToEdgeBottom
import com.yang.lovechat.util.getScreenPx
import com.yang.lovechat.util.loadImage
import com.yang.lovechat.viewmodel.PublicViewModel

class EditAvatarDialog : BaseDialog<DialogEditAvatarBinding>(DialogEditAvatarBinding::inflate) {

    private val pictureWidth = getScreenPx(BaseApplication.mApplication)[0] / 2

    private val pictureHeight = pictureWidth * 4 / 3

    var onConfirm: ((String, Uri?) -> Unit)? = null

    private var mFileUri: Uri? = null

    private var url: String = ""


    private val mViewModel by activityViewModels<PublicViewModel>()

    private val photoPicker = PhotoPickerHelper(this, maxCount = 2) { uris ->

        onPhotoPicked(uris.toMutableList())
    }

    companion object {
        fun newInstance(data: String): EditAvatarDialog {
            return EditAvatarDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.URL, data)
                }
            }
        }
    }


    override fun initView() {


        withViewBinding {

            root.edgeToEdgeBottom()

            ivClose.clicks {

                dismissAllowingStateLoss()
            }

            tvSave.clicks {

                if (null != mFileUri) {

                    onConfirm?.invoke(url, mFileUri)

                }

                dismissAllowingStateLoss()

            }

            stvEdit.clicks {

                photoPicker.pick(MediaEnumType.IMAGE, 0)
            }

        }

    }

    override fun initData() {

        arguments?.let {

            url = it.getString(AppConstant.Constant.URL) ?: url

            mDialogBinding.ivImage.loadImage(url, width = pictureWidth, height = pictureHeight)
        }

    }

    override fun initViewModel() {
        super.initViewModel()


        mViewModel.mUploadMediaListData.observe(this) {

            if (it.isNotEmpty()) {

                val item = it.first()

                if (isVisible) {
                    withViewBinding {

                        url = item.fileUrl.toString()

                        ivLoad.visibility = View.GONE

                        tvSave.isEnabled = true

                        ivLoad.stop()
                    }

                }
            }

        }


    }


    private fun onPhotoPicked(uris: List<Uri>) {

        if (uris.isEmpty()) {
            return
        }

        val list = uris.map {

            val item = UploadPictureData()

            item.mediaEnumType = MediaEnumType.LOCAL_IMAGE

            item.uri = it

            item

        }.toMutableList()

        withViewBinding {

            ivImage.loadImage(uris.first())

            ivLoad.visibility = View.VISIBLE
        }

        mFileUri = uris.first()

        mViewModel.uploadMedias(list)
    }





    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }


}