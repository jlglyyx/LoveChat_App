package com.yang.lovechat.ui.activity

import android.annotation.SuppressLint
import android.net.Uri
import android.text.Html
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ResourceUtils
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.LoadState
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.yang.lovechat.R
import com.yang.lovechat.adapter.EmojiAdapter
import com.yang.lovechat.adapter.MessageAdapter
import com.yang.lovechat.base.activity.BaseActivity
import com.yang.lovechat.base.bus.EventBus
import com.yang.lovechat.base.bus.EventBus.observe
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.MediaEnumType
import com.yang.lovechat.data.MediaInfoData
import com.yang.lovechat.data.MessageData
import com.yang.lovechat.data.MessageResultData
import com.yang.lovechat.data.MessageType
import com.yang.lovechat.data.PictureData
import com.yang.lovechat.data.ResultEnum
import com.yang.lovechat.data.UploadPictureData
import com.yang.lovechat.databinding.ActMessageBinding
import com.yang.lovechat.dialog.AlbumDialog
import com.yang.lovechat.dialog.MoreMenuDialog
import com.yang.lovechat.dialog.PictureDetailDialog
import com.yang.lovechat.helper.ChatHelper
import com.yang.lovechat.helper.IMHelper
import com.yang.lovechat.helper.MediaHelper.getVideoDuration
import com.yang.lovechat.helper.MediaHelper.isVideo
import com.yang.lovechat.helper.PhotoPickerHelper
import com.yang.lovechat.helper.ProductHelper
import com.yang.lovechat.helper.UserInfoHold
import com.yang.lovechat.http.IHttpException
import com.yang.lovechat.im.MessageManager
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.createIntent
import com.yang.lovechat.util.edgeToEdgeTop
import com.yang.lovechat.util.formatListJson
import com.yang.lovechat.util.hideSoftInput
import com.yang.lovechat.util.isUnlockTimeValid
import com.yang.lovechat.util.loadImage
import com.yang.lovechat.util.showShort
import com.yang.lovechat.util.startActivity
import com.yang.lovechat.util.toJson
import com.yang.lovechat.util.topLoadListener
import com.yang.lovechat.util.viewVisibility
import com.yang.lovechat.viewmodel.MessageViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MessageActivity :
    BaseActivity<ActMessageBinding, MessageViewModel>(ActMessageBinding::inflate) {

    private val mMessageAdapter: MessageAdapter by lazy { MessageAdapter() }

    private val mEmojiAdapter: EmojiAdapter by lazy { EmojiAdapter() }

    private lateinit var mQuickAdapterHelper: QuickAdapterHelper
    private var convId: String = ""

    private var friendAvatar: String? = null

    private var friendName: String = ""

    private var friendUserId: Long? = null

    private var mLinearLayoutManager: LinearLayoutManager? = null

    private val sortJob = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val photoPicker = PhotoPickerHelper(this, maxCount = 9) { uris ->

        onPhotoPicked(uris.toMutableList())
    }

    private var isToPhotoPicker = false


    override fun initView() {

        withViewBinding {

            ChatHelper.add(this@MessageActivity)

            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

            llRootContainer.edgeToEdgeTop()

            ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
                val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                val navHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                root.updatePadding(
                    bottom = imeHeight.takeIf { it > 0 } ?: navHeight
                )
                if (imeHeight > 100) {
                    ivEmoji.visibility = View.VISIBLE
                    viewVisibility(View.GONE, ivInput, emojiRecyclerView)
                    ivEmoji.isSelected = false
                    toLastMessage()
                }
                insets
            }

            initRecyclerView()

            initEmojiRecyclerView()


            ivBack.setOnClickListener {

                finish()
            }

            appToolBar.clicks {

                createIntent(UserInfoActivity::class.java)
                    .putExtra(AppConstant.Constant.USER_ID, friendUserId)
                    .startActivity(this@MessageActivity)
            }


            ivMore.setOnClickListener {

                initReportDialog(friendUserId.toString(), 0)
            }

            ivSendMedia.clicks {

                isToPhotoPicker = true

                photoPicker.pick(MediaEnumType.IMAGE_AND_VIDEO, 0)
            }

            ivSendPMedia.setOnClickListener {

                AlbumDialog.newInstance().apply {

                    onConfirm = {

                        sendPrivateMediaMessage(it)
                    }
                }.show(supportFragmentManager)

            }



            setSendMessage.doAfterTextChanged {

                if (it.toString().isBlank()) {
                    ivSendMessage.setImageResource(R.drawable.iv_send)
                } else {
                    ivSendMessage.setImageResource(R.drawable.iv_send_able)
                }

            }

            ivSendMessage.setOnClickListener {

                val message = setSendMessage.text.toString()

                if (message.isBlank()) {

                    return@setOnClickListener
                }


                val buildTextMessage = IMHelper.buildTextMessage(
                    UserInfoHold.userId,
                    convId,
                    friendUserId,
                    message
                )
                    ?: return@setOnClickListener

                val textMessage = buildTextMessage.data ?: return@setOnClickListener

                IMHelper.sendMessage(buildTextMessage)

                mMessageAdapter.add(textMessage)

                setSendMessage.setText("")

                toLastMessage()

            }



            ivInput.setOnClickListener {

                viewVisibility(View.GONE, ivInput)

                ivEmoji.visibility = View.VISIBLE

                setSendMessage.hideSoftInput(this@MessageActivity, true)

            }

            ivEmoji.setOnClickListener {

                ivInput.visibility = View.VISIBLE

                viewVisibility(View.GONE, ivEmoji)

                setSendMessage.hideSoftInput(this@MessageActivity)

                if (ivEmoji.isSelected) {
                    emojiRecyclerView.visibility = View.GONE
                    ivEmoji.isSelected = false
                } else {
                    sortJob.launch {
                        delay(100)
                        emojiRecyclerView.visibility = View.VISIBLE
                        ivEmoji.isSelected = true
                        toLastMessage()
                    }

                }


            }


        }

    }

    override fun initData() {


        convId = intent.getStringExtra(AppConstant.Constant.CONV_ID) ?: convId

        friendAvatar = intent.getStringExtra(AppConstant.Constant.FRIEND_AVATAR)

        friendName = intent.getStringExtra(AppConstant.Constant.FRIEND_NAME) ?: friendName

        friendUserId = intent.getLongExtra(AppConstant.Constant.FRIEND_USER_ID, -1L)

        mViewModel.getMessageHistory(convId)


        onMessageReceive()

        initInfo()


    }

    override fun initViewModel() {


        EventBus.with(AppConstant.EventConstant.EVENT_FOREGROUND_CHANGE).observe(this) {

            Log.i("ZZZZZZZZZZZZZ", "onStart: 进入前台")

            if (!isToPhotoPicker) {
                mViewModel.getMessageHistory(convId, pageSize = 40)
            }

            isToPhotoPicker = false
        }



        mViewModel.mMessageListData.observe(this) {

            if (mQuickAdapterHelper.leadingLoadState == LoadState.Loading) {

                if (it.size < AppConstant.Constant.PAGE_SIZE_COUNT) {
                    mQuickAdapterHelper.leadingLoadState = LoadState.NotLoading(true)
                } else {
                    mQuickAdapterHelper.leadingLoadState = LoadState.NotLoading(false)
                }

                mMessageAdapter.addAll(0, it)

                mMessageAdapter.notifyItemRangeChanged(0, mMessageAdapter.itemCount, false)
            } else {
                if (it.size < AppConstant.Constant.PAGE_SIZE_COUNT) {
                    mQuickAdapterHelper.leadingLoadState = LoadState.NotLoading(true)
                } else {
                    mQuickAdapterHelper.leadingLoadState = LoadState.NotLoading(false)
                }



                mMessageAdapter.submitList(it)

                if (it.size >= 10) {

                    mLinearLayoutManager?.stackFromEnd = true
                } else {

                    mLinearLayoutManager?.stackFromEnd = false
                }

                toLastMessage()
            }

        }







        mViewModel.mUnlockMediaMessageData.observe(this) {


            val index = mMessageAdapter.items.indexOfLast { item -> item.id == it.id }

            if (index != -1) {

                mMessageAdapter[index] = it

                mMessageAdapter.notifyItemChanged(index, false)
            }

        }


        mViewModel.mShieldConversationStatus.observe(this) {




            showShort("Block Success")

            finish()

        }


        mViewModel.mUnlockMediaErrorStatus.observe(this) {

            if (it is IHttpException.HttpErrorException) {

                if (it.code == ResultEnum.NEED_VIP_PERMISSION_ERROR.code) {
                    ProductHelper.showPayProductDialog(this, AppConstant.Constant.PRODUCT_VIP)
                } else if (it.code == ResultEnum.NEED_DIAMOND_ERROR.code) {
                    ProductHelper.showPayProductDialog(this, AppConstant.Constant.PRODUCT_DIAMOND)
                }

            }


        }


    }


    private fun handleMessage(message: MessageData) {

        if (message.userId == UserInfoHold.userId) {

            val index =
                mMessageAdapter.items.indexOfLast { it.uid == message.uid || it.id == message.id }

            if (index != -1) {

                if (message.msgType == MessageType.IMAGE_VIDEO.type) {

                    val item = mMessageAdapter.getItem(index)

                    item.id = message.id

                    item.msgContent = message.msgContent

                    mMessageAdapter.notifyItemChanged(index, false)

                } else {

                    mMessageAdapter[index] = message
                }
            }

        } else {

            mMessageAdapter.add(message)

            toLastMessage()
        }

    }


    private fun initInfo() {

        withViewBinding {

            tvName.text = friendName

            sivAvatar.loadImage(friendAvatar ?: R.drawable.iv_avatar)

        }

    }


    private fun onMessageReceive() {


        lifecycleScope.launch {

            MessageManager.normalMessageFlow.collect { (result, message, text) ->

                try {

                    if (message.convId != convId) return@collect


                    when (message.msgType) {

                        MessageType.TEXT.type -> {

                            handleMessage(message)

                        }

                        MessageType.IMAGE_VIDEO.type -> {


                            handleMessage(message)

                        }

                        MessageType.SYSTEM.type -> {

                        }

                        else -> {

                        }
                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }


        }

        lifecycleScope.launch {

            MessageManager.errorMessageFlow.collect { (result, data, text) ->


                val message = IMHelper.getMessageData(result.data) ?: return@collect

                val index = mMessageAdapter.items.indexOfLast { it.uid == message.uid }

                if (index != -1) {

                    val item = mMessageAdapter.getItem(index)

                    item.errorDesc = result.message

                    mMessageAdapter.notifyItemChanged(index, false)
                }
            }
        }

    }


    @SuppressLint("ClickableViewAccessibility")
    private fun initRecyclerView() {

        mQuickAdapterHelper = mMessageAdapter.topLoadListener(onLoad = {

            if (mMessageAdapter.items.isNotEmpty()) {

                val lastMessageId = mMessageAdapter.items.first().id

                mViewModel.getMessageHistory(convId, lastMessageId)

            } else {
                mViewModel.getMessageHistory(convId)
            }

        })

        withViewBinding {

            mLinearLayoutManager = LinearLayoutManager(this@MessageActivity).apply {
                stackFromEnd = true
                reverseLayout = false
            }
            chatRecyclerView.layoutManager = mLinearLayoutManager

            chatRecyclerView.adapter = mQuickAdapterHelper.adapter

            chatRecyclerView.setOnTouchListener { v, event ->
                try {
                    setSendMessage.hideSoftInput(this@MessageActivity)
                    ivEmoji.visibility = View.VISIBLE
                    viewVisibility(View.GONE, ivInput, emojiRecyclerView)
                    ivEmoji.isSelected = false
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return@setOnTouchListener false
            }


            mMessageAdapter.addOnDebouncedChildClick(R.id.iv_send_text_message_error) { adapter, view, position ->

                val item = mMessageAdapter.getItem(position)

                item.errorDesc = ""


                IMHelper.sendMessage(
                    MessageResultData(
                        item,
                        UserInfoHold.userId.toString(),
                        item.msgType
                    )
                )

            }



            mMessageAdapter.onGridItemClick = { list, position, parentPosition ->


                try {

                    val data = list[position]

                    val mMessage = mMessageAdapter.getItem(parentPosition)

                    if (mMessage.userId == UserInfoHold.userId) {
                        showPictureDetailDialog(list, position)
                    } else {
                        if (null == data.unlockTime) {

                            if (UserInfoHold.isVip) {

                                mViewModel.unlockMedia(
                                    mMessage.convId!!,
                                    mMessage.id!!,
                                    mutableSetOf(data.id!!)
                                )

                            } else {
                                ProductHelper.showPayProductDialog(
                                    this@MessageActivity,
                                    AppConstant.Constant.PRODUCT_VIP
                                )
                            }


                        } else {

                            val item = list[position]

                            if (isUnlockTimeValid(item.unlockTime)) {
                                val validList =
                                    list.filter { filter -> isUnlockTimeValid(filter.unlockTime) }
                                        .toMutableList()


                                if (validList.isNotEmpty()) {

                                    val index = validList.indexOf(item)

                                    showPictureDetailDialog(validList, index)
                                }
                            }

                        }

                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }

    }


    private fun initEmojiRecyclerView() {

        withViewBinding {

            emojiRecyclerView.adapter = mEmojiAdapter

            emojiRecyclerView.layoutManager = GridLayoutManager(this@MessageActivity, 8)

            ResourceUtils.readAssets2String("face_emoji.json").formatListJson<String>().apply {

                mEmojiAdapter.submitList(this)
            }

            mEmojiAdapter.setOnItemClickListener { _, _, position ->

                try {
                    val emojiItem = mEmojiAdapter.getItem(position)
                    val editText = setSendMessage
                    val currentText = editText.text ?: return@setOnItemClickListener
                    val selectionStart = editText.selectionStart.coerceAtLeast(0)
                    val spannedInserted = Html.fromHtml(emojiItem, Html.FROM_HTML_MODE_LEGACY)
                    val insertLength = spannedInserted.length

                    val newText = SpannableStringBuilder(currentText).apply {
                        insert(selectionStart, spannedInserted)
                    }
                    editText.setText(newText)
                    editText.setSelection(selectionStart + insertLength)
                } catch (e: Exception) {
                    e.printStackTrace()
                }


            }

        }

    }


    private fun onPhotoPicked(uris: MutableList<Uri>) {


        val list = uris.map {

            val item = UploadPictureData()

            val isVideo = it.isVideo(this)

            item.mediaEnumType =
                if (isVideo) MediaEnumType.LOCAL_VIDEO else MediaEnumType.LOCAL_IMAGE

            if (isVideo) {
                item.videoDuration = it.getVideoDuration(this).toInt()
            }

            item.uri = it

            item

        }.toMutableList()


        val mMediaInfoList = list.map {

            MediaInfoData(
                id = null,
                mediaType = if (it.mediaEnumType == MediaEnumType.LOCAL_IMAGE) 0 else 1,
                sourceType = 2,
                isPrivate = false,
                fileUrl = null,
                coverUrl = null,
                uri = it.uri,
                videoDuration = it.videoDuration,
                unlockTime = null,
                type = null,
                isSelect = null,
                mediaEnumType = null
            )

        }.toMutableList()

        val messageResultData = sendMediaMessage(mMediaInfoList, false)

        mViewModel.uploadMedias(list, messageResultData)


    }


    private fun toLastMessage() {

        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return

        lifecycleScope.launch(Dispatchers.Main) {

            if (mMessageAdapter.items.isEmpty()) {
                return@launch
            }

            val recyclerView = mViewBinding.chatRecyclerView

            recyclerView.scrollToPosition(mMessageAdapter.items.lastIndex)

        }

    }


    private fun initReportDialog(eventId: String, eventType: Int) {

        MoreMenuDialog.newInstance(true, eventId, eventType)
            .apply {
                onBlock = {

                    mViewModel.shieldConversation(true, convId)

                }
            }
            .show(supportFragmentManager)
    }


    private fun sendMediaMessage(
        list: MutableList<MediaInfoData>,
        isPrivate: Boolean,
        needSend: Boolean = false
    ): MessageResultData<MessageData>? {

        val buildMediaMessage = IMHelper.buildImageVideoMessage(
            UserInfoHold.userId,
            convId,
            friendUserId,
            if (needSend) list.toJson() else "",
            isPrivate
        ) ?: return null

        val mediaMessage = buildMediaMessage.data ?: return null


        if (needSend) {

            IMHelper.sendMessage(buildMediaMessage)

        } else {
            mediaMessage.msgMediaContent = list
        }

        Log.i(TAG, "sendMediaMessage: ${mediaMessage.uid}")

        mMessageAdapter.add(mediaMessage)

        toLastMessage()

        return buildMediaMessage
    }


    private fun sendPrivateMediaMessage(
        content: String,
    ): MessageResultData<MessageData>? {

        val buildMediaMessage = IMHelper.buildImageVideoMessage(
            UserInfoHold.userId,
            convId,
            friendUserId,
            content,
            true
        ) ?: return null

        val mediaMessage = buildMediaMessage.data ?: return null

        IMHelper.sendMessage(buildMediaMessage)

        mMessageAdapter.add(mediaMessage)

        toLastMessage()

        return buildMediaMessage
    }


    private fun showPictureDetailDialog(
        data: MutableList<MediaInfoData>,
        position: Int,
    ) {
        if (data.isEmpty()) return


        val list = data.map { item ->

            val pictureData = PictureData()

            pictureData.url = item.fileUrl

            pictureData.uri = item.uri


            val mediaEnumType = if (null == item.uri) {
                if (item.mediaType == 0) MediaEnumType.IMAGE else MediaEnumType.VIDEO
            } else {
                if (item.mediaType == 0) MediaEnumType.LOCAL_IMAGE else MediaEnumType.LOCAL_VIDEO
            }

            pictureData.mediaEnumType = mediaEnumType

            pictureData.coverUrl = item.coverUrl


            pictureData
        } as ArrayList<PictureData>



        PictureDetailDialog.newInstance(list, position)
            .show(supportFragmentManager)
    }


    override fun finish() {

        mViewModel.readConversation(convId)


        super.finish()

    }


    override fun onDestroy() {

        sortJob.cancel()


        super.onDestroy()
    }

}