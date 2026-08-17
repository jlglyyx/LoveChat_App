package com.yang.lovechat.ui.activity


import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.trailing.TrailingLoadStateAdapter.OnTrailingListener
import com.yang.lovechat.adapter.SystemMessageAdapter
import com.yang.lovechat.base.activity.BaseActivity
import com.yang.lovechat.base.adapter.BottomLoadAdapter
import com.yang.lovechat.databinding.ActSystemMessageBinding
import com.yang.lovechat.viewmodel.MessageViewModel


class SystemMessageActivity : BaseActivity<ActSystemMessageBinding, MessageViewModel>(ActSystemMessageBinding::inflate) {

    private val mSystemMessageAdapter: SystemMessageAdapter by lazy { SystemMessageAdapter() }

    private lateinit var mQuickAdapterHelper: QuickAdapterHelper



    override fun initView() {


        mViewBinding.apply {


        }


    }

    override fun initViewModel() {



    }



    override fun initData() {


        initRecyclerView()

        getHistoryMessage()


    }


    private fun getHistoryMessage() {




    }

    private fun initRecyclerView() {



        mQuickAdapterHelper = QuickAdapterHelper.Builder(mSystemMessageAdapter)
            .setTrailingLoadStateAdapter(BottomLoadAdapter().setOnLoadMoreListener(object :
                OnTrailingListener {
                override fun onFailRetry() {

                    getHistoryMessage()

                }

                override fun onLoad() {

                    getHistoryMessage()
                }


            })).build()


        mViewBinding.recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = false
        }

        mViewBinding.recyclerView.adapter = mQuickAdapterHelper.adapter


    }

    override fun finish() {

        mViewModel.readConversation("")

        super.finish()
    }


}