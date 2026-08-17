package com.yang.lovechat.helper

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.yang.lovechat.data.MediaEnumType

class PhotoPickerHelper(
    private val caller: ActivityResultCaller,
    private val lifecycleOwner: LifecycleOwner,
    private val maxCount: Int = 9,
    private val onPhotosPicked: (List<Uri>) -> Unit
) : DefaultLifecycleObserver {

    private var mCurrentMaxSize = 0
    private val pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private val pickLegacy: ActivityResultLauncher<Intent>

    init {
        // 绑定生命周期，确保在合适时机清理
        lifecycleOwner.lifecycle.addObserver(this)

        // 使用 ActivityResultCaller 自动管理 Key
        pickMedia = caller.registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(maxCount)) { uris ->
            if (uris.isNotEmpty()) {
                onPhotosPicked(uris)
            }
        }

        pickLegacy = caller.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uris = mutableListOf<Uri>()
                val data = result.data

                data?.clipData?.let { clipData ->
                    val limit = minOf(clipData.itemCount, mCurrentMaxSize)
                    for (i in 0 until limit) {
                        uris.add(clipData.getItemAt(i).uri)
                    }
                } ?: data?.data?.let { uris.add(it) }

                if (uris.isNotEmpty()) {
                    onPhotosPicked(uris)
                }
            }
        }
    }

    constructor(activity: AppCompatActivity, maxCount: Int = 9, onPhotosPicked: (List<Uri>) -> Unit) :
            this(activity, activity, maxCount, onPhotosPicked)

    constructor(fragment: Fragment, maxCount: Int = 9, onPhotosPicked: (List<Uri>) -> Unit) :
            this(fragment, fragment, maxCount, onPhotosPicked)

    fun pick(mediaEnumType: MediaEnumType, currentSize: Int) {
        mCurrentMaxSize = maxCount - currentSize
        if (mCurrentMaxSize <= 0) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val request = when (mediaEnumType) {
                MediaEnumType.LOCAL_IMAGE, MediaEnumType.IMAGE ->
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                MediaEnumType.LOCAL_VIDEO, MediaEnumType.VIDEO ->
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                MediaEnumType.IMAGE_AND_VIDEO ->
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
            }
            pickMedia.launch(request)
        } else {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = when (mediaEnumType) {
                    MediaEnumType.LOCAL_IMAGE, MediaEnumType.IMAGE -> "image/*"
                    MediaEnumType.LOCAL_VIDEO, MediaEnumType.VIDEO -> "video/*"
                    MediaEnumType.IMAGE_AND_VIDEO -> {
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                        "*/*"
                    }
                }
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            pickLegacy.launch(Intent.createChooser(intent, "Select Media"))
        }
    }
}