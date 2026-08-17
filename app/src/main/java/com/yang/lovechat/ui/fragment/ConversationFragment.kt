package com.yang.lovechat.ui.fragment

import android.Manifest
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.yang.lovechat.R
import com.yang.lovechat.adapter.ConversationAdapter
import com.yang.lovechat.adapter.TopConversationAdapter
import com.yang.lovechat.base.bus.EventBus
import com.yang.lovechat.base.bus.EventBus.observe
import com.yang.lovechat.base.fragment.BaseFragment
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.ConversationData
import com.yang.lovechat.data.InstructionType
import com.yang.lovechat.data.MessageData
import com.yang.lovechat.data.MessageType
import com.yang.lovechat.databinding.FraConversationBinding
import com.yang.lovechat.databinding.ViewEmptyLikeBinding
import com.yang.lovechat.databinding.ViewNoNetworkBinding
import com.yang.lovechat.dialog.OpenNoticeDialog
import com.yang.lovechat.helper.IMHelper
import com.yang.lovechat.im.MessageManager
import com.yang.lovechat.ui.activity.MessageActivity
import com.yang.lovechat.ui.activity.SystemMessageActivity
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.createIntent
import com.yang.lovechat.util.getCache
import com.yang.lovechat.util.hasNotificationPermission
import com.yang.lovechat.util.openNoticePermissionDetail
import com.yang.lovechat.util.refreshLoadDataListener
import com.yang.lovechat.util.refreshLoadListener
import com.yang.lovechat.util.setCache
import com.yang.lovechat.util.startActivity
import com.yang.lovechat.viewmodel.MessageViewModel
import com.yang.lovechat.widget.ErrorReLoadView
import com.yang.lovechat.widget.SwipeMenuLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConversationFragment :
    BaseFragment<FraConversationBinding, MessageViewModel>(FraConversationBinding::inflate){

    private val mConversationAdapter by lazy { ConversationAdapter() }

    private val mTopConversationAdapter by lazy { TopConversationAdapter(lifecycle) }

    private lateinit var mQuickAdapterHelper: QuickAdapterHelper


    private val sortJob = CoroutineScope(Dispatchers.Main+ SupervisorJob())

    private var isCloseNotice = false

    private val registerForActivityResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            if (permissions.all { it.value }) {

                withViewBinding {

                    sclNotice.visibility = View.GONE
                }

            } else {
                withViewBinding {

                    sclNotice.visibility = View.VISIBLE
                }

            }

        }


    override fun onResume() {
        super.onResume()

        withViewBinding {

            if (!isCloseNotice) {

                if (requireContext().hasNotificationPermission()) {

                    sclNotice.visibility = View.GONE

                } else {

                    sclNotice.visibility = View.VISIBLE

                }
            }

            initNoticeDialog()

        }


    }


    override fun initView() {



        withViewBinding {


            stvEnable.clicks {


                requestNoticePermission()

            }

            ivX.clicks {


                isCloseNotice = true

                sclNotice.visibility = View.GONE

            }

            stvEnable.clicks {

                requestNoticePermission()

            }

            ivSystemNotice.clicks {

                createIntent(SystemMessageActivity::class.java).startActivity(requireActivity())
            }



            initRecyclerView()

            initTopConversationAdapter()

            onMessageReceive()
        }
    }

    override fun initData() {

        onRefresh()


    }


    override fun initViewModel() {

        mViewModel.mConversationListData.observe(this) {

            withViewBinding {
                mConversationAdapter.refreshLoadDataListener(
                    mSwipeRefreshLayout,
                    mQuickAdapterHelper,
                    errorReLoadView,
                    it.toMutableList()
                )

            }
        }


        mViewModel.mConversationData.observe(this) {

            withViewBinding {
                mConversationAdapter.add(0, it)

                IMHelper.refreshAllReadCount(1, true)

                sortByDescending()

            }
        }

        mViewModel.mMatchUserInfoData.observe(this) {

            withViewBinding {

                // TODO:

//                FastConnectDialog.newInstance(it).apply {
//
//                    onConfirm = { text ->
//
//                        createIntent(MessageActivity::class.java)
//                            .putExtra(AppConstant.Constant.CONV_ID, it.convId)
//                            .putExtra(AppConstant.Constant.FRIEND_AVATAR, it.avatarUrl)
//                            .putExtra(AppConstant.Constant.FRIEND_NAME, it.userName)
//                            .putExtra(AppConstant.Constant.USER_ID, it.id)
//                            .startActivity(requireActivity())
//                    }
//
//                }.show(parentFragmentManager)

            }
        }

        mViewModel.requestExceptionEvent.observe(this) {

            mViewBinding.mSwipeRefreshLayout.isRefreshing = false

            mViewBinding.errorReLoadView.showStatusView(ErrorReLoadView.Status.NO_NETWORK)


        }


        EventBus.with(AppConstant.EventConstant.EVENT_UPDATE_CONVERSATION_READ_COUNT)
            .observe(this) {

                try {
                    val convId = it as? String ?: return@observe

                    val index =
                        mConversationAdapter.items.indexOfFirst { item -> item.convId == convId }

                    if (index != -1) {

                        val item = mConversationAdapter.getItemOrNull(index) ?: return@observe


                        IMHelper.refreshAllReadCount(item.unreadCount, false)


                        item.unreadCount = 0

                        mConversationAdapter.notifyItemChanged(index, false)


                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

        EventBus.with(AppConstant.EventConstant.EVENT_SHIELD_CONVERSATION).observe(this){

            try {
                val convId = it as? String ?: return@observe

                val index =
                    mConversationAdapter.items.indexOfFirst { item -> item.convId == convId }

                if (index != -1) {

                    val item = mConversationAdapter.getItemOrNull(index) ?: return@observe

                    IMHelper.refreshAllReadCount(item.unreadCount, false)

                    mConversationAdapter.remove(item)

                    mViewBinding.errorReLoadView.showSuccessView(mConversationAdapter.items)



                }



            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }




    private fun onMessageReceive(){


        lifecycleScope.launch {

            MessageManager.normalMessageFlow.collect {  (result,message, text) ->

                try {

                    when (message.msgType) {

                        MessageType.TEXT.type, MessageType.IMAGE_VIDEO.type -> {

                            handleCommonMessage(message)

                        }

                        MessageType.SYSTEM.type -> {

                            handleInstructionMessage(message)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }


        }

        lifecycleScope.launch {

            MessageManager.errorMessageFlow.collect { (result,data, text) ->


            }
        }

    }



    fun handleCommonMessage(messageData: MessageData) {

        try {

            val convId = messageData.convId ?: return

            val index =
                mConversationAdapter.items.indexOfFirst { item -> item.convId == convId }

            if (index != -1) {

                val item = mConversationAdapter.getItemOrNull(index) ?: return

                item.unreadCount += 1
                item.lastMsgType = messageData.msgType
                item.lastMsgContent = messageData.msgContent
                item.lastMsgId = messageData.id!!
                item.lastMsgTime = messageData.createTime!!

                mConversationAdapter.notifyItemChanged(index, false)



            }else{

                //todo 消息返回头像

//                val conversationData = ConversationData()
//
//                conversationData.unreadCount += 1
//                conversationData.lastMsgType = messageData.msgType
//                conversationData.lastMsgContent = messageData.msgContent
//                conversationData.lastMsgId = messageData.id!!
//                conversationData.lastMsgTime = messageData.createTime!!
//
//                mConversationAdapter.add(0, conversationData)
            }

            sortByDescending()

            IMHelper.refreshAllReadCount(1, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleInstructionMessage(messageData: MessageData) {

        when (messageData.msgContent) {

            InstructionType.MATCH.value, InstructionType.FAST_CONNECT.value -> {

                val convId = messageData.convId ?: return

                mViewModel.getConversationDetail(convId)


            }

        }

    }


    private fun initRecyclerView() {

        withViewBinding {

            mQuickAdapterHelper =
                mConversationAdapter.refreshLoadListener(mSwipeRefreshLayout, onRefresh = {

                    onRefresh()

                }, onLoad = {

                    onLoadMore()
                })

            recyclerView.adapter = mQuickAdapterHelper.adapter


            errorReLoadView.addEmptyView { viewGroup ->
                ViewEmptyLikeBinding.inflate(
                    LayoutInflater.from(requireContext()),
                    viewGroup,
                    true
                )
                    .apply {

                        ivImage.setImageResource(R.drawable.iv_empty_conversation)
                        tvTitle.text = "No chat conversations for now"
                        tvDesc.text = "Swipe more cards to meet new people and start conversations"
                        stvConfirm.text = "Swipe Now"


                        stvConfirm.clicks {

                        }


                    }
            }

            errorReLoadView.addNoNetView { viewGroup ->
                ViewNoNetworkBinding.inflate(LayoutInflater.from(requireContext()), viewGroup, true)
                    .apply {

                        stvConfirm.clicks {

                            onRefresh()
                        }
                    }
            }

//            mViewBinding.errorReLoadView.showStatusView(ErrorReLoadView.Status.EMPTY)


            mConversationAdapter.addOnDebouncedChildClick(R.id.ll_top) { adapter, view, position ->

                val item =
                    mConversationAdapter.getItem(position)

                mViewModel.topConversation(!item.isTop, item.convId)

                item.isTop = !item.isTop

//                mConversationAdapter.notifyItemChanged(position, false)
//
                (view.parent as? SwipeMenuLayout)?.closeMenu()

                sortByDescending()

            }
            mConversationAdapter.addOnDebouncedChildClick(R.id.cl_message) { adapter, view, position ->

                val item = mConversationAdapter.getItem(position)


                createIntent(MessageActivity::class.java)
                    .putExtra(AppConstant.Constant.CONV_ID, item.convId)
                    .putExtra(AppConstant.Constant.FRIEND_AVATAR, item.friendAvatar)
                    .putExtra(AppConstant.Constant.FRIEND_NAME, item.friendName)
                    .putExtra(AppConstant.Constant.FRIEND_USER_ID, item.friendUserId)
                    .startActivity(requireActivity())

                (view.parent as? SwipeMenuLayout)?.closeMenu()
            }

            mConversationAdapter.addOnDebouncedChildClick(R.id.ll_delete) { adapter, view, position ->

                val item = mConversationAdapter.getItem(position)

                mViewModel.deleteConversation(true, item.convId)

                mConversationAdapter.remove(item)


                (view.parent as? SwipeMenuLayout)?.closeMenu()

                IMHelper.refreshAllReadCount(item.unreadCount, false)

                errorReLoadView.showSuccessView(mConversationAdapter.items)
            }


        }
    }


    private fun initTopConversationAdapter() {

        withViewBinding {

            topRecyclerView.adapter = mTopConversationAdapter

            topRecyclerView.layoutManager = LinearLayoutManager(
                requireContext(),
                RecyclerView.HORIZONTAL, false
            )

            topRecyclerView.itemAnimator = null

            mTopConversationAdapter.setOnDebouncedItemClick { _, _, position ->

                val item = mTopConversationAdapter.getItem(position)

            }

        }
    }


    private fun requestNoticePermission() {

        if (requireContext().hasNotificationPermission()) {
            withViewBinding {
                sclNotice.visibility = View.GONE
            }

        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                ) {

                    requireContext().openNoticePermissionDetail()
                } else {

                    registerForActivityResult.launch(
                        arrayOf(
                            Manifest.permission.POST_NOTIFICATIONS,
                        )
                    )
                }

            } else {
                requireContext().openNoticePermissionDetail()
            }
        }
    }


    private fun initNoticeDialog() {

        if (requireContext().hasNotificationPermission()) return

        val cache = getCache(AppConstant.Constant.HAS_MESSAGE_NOTICE, false)

        if (cache) return

        setCache(AppConstant.Constant.HAS_MESSAGE_NOTICE, true)

        val mNoticeDialog = OpenNoticeDialog().apply {

            onConfirm = {

                requestNoticePermission()

            }

        }

        lifecycleScope.launch {

            delay(1000)

            mNoticeDialog.show(parentFragmentManager)
        }


    }


    private fun sortByDescending() {

        sortJob.launch {

            try {

               val mSortedByDescendingData =  withContext(Dispatchers.IO){

                    val mSortedByDescendingData = mConversationAdapter.items
                        .sortedWith(
                            compareByDescending<ConversationData> { it.isTop }
                                .thenByDescending { it.lastMsgTime }
                        )
                        .toMutableList()

                   mSortedByDescendingData

                }

                delay(100)

                mConversationAdapter.submitList(mSortedByDescendingData)

                mViewBinding.errorReLoadView.showSuccessView(mConversationAdapter.items)

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }



    }


    private fun onRefresh() {

        mViewModel.pageNum = 1

        mViewModel.getConversationList()

        mViewModel.getAllConversationReadCount()
    }

    private fun onLoadMore() {

        mViewModel.pageNum++

        mViewModel.getConversationList()

    }

    override fun onDestroy() {

        sortJob.cancel()

        super.onDestroy()
    }


}