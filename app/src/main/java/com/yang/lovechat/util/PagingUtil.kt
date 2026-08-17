package com.yang.lovechat.util

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.LoadState
import com.chad.library.adapter4.loadState.leading.LeadingLoadStateAdapter.OnLeadingListener
import com.chad.library.adapter4.loadState.trailing.TrailingLoadStateAdapter
import com.chad.library.adapter4.loadState.trailing.TrailingLoadStateAdapter.OnTrailingListener
import com.yang.lovechat.base.adapter.BottomLoadAdapter
import com.yang.lovechat.base.adapter.TopLoadAdapter
import com.yang.lovechat.base.paging.BasePagingSource
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.http.MResult
import com.yang.lovechat.widget.ErrorReLoadView
import kotlinx.coroutines.flow.Flow


/**
 * SwipeRefreshLayout 网络请求
 */
fun <T : Any> BaseQuickAdapter<T, *>.refreshLoadDataListener(
    refresh: SwipeRefreshLayout,
    loading: QuickAdapterHelper,
    errorReLoadView: ErrorReLoadView?,
    mData: MutableList<T>?
) {

    val data = if (mData.isNullOrEmpty()) mutableListOf() else mData

    if (refresh.isRefreshing) {

        refresh.isRefreshing = false

        if (data.isEmpty()) {
            //this.isStateViewEnable = true

        } else {
            //this.isStateViewEnable = false


            if (data.size < AppConstant.Constant.PAGE_SIZE_COUNT) {
                loading.trailingLoadState = LoadState.NotLoading(true)
            } else {
                loading.trailingLoadState = LoadState.NotLoading(false)
            }
        }

        this.submitList(data)

    } else if (loading.trailingLoadState == LoadState.Loading) {

        if (data.size < AppConstant.Constant.PAGE_SIZE_COUNT) {
            if (data.isNotEmpty()) {
                this.addAll(data)
            }
            loading.trailingLoadState = LoadState.NotLoading(true)
        } else {
            this.addAll(data)
            loading.trailingLoadState = LoadState.NotLoading(false)
        }
    } else {
        if (data.isEmpty()) {
//            this.isStateViewEnable = true
        } else {
//            this.isStateViewEnable = false

            if (data.size < AppConstant.Constant.PAGE_SIZE_COUNT) {
                loading.trailingLoadState = LoadState.NotLoading(true)
            } else {
                loading.trailingLoadState = LoadState.NotLoading(false)
            }
        }

        this.submitList(data)
    }

    errorReLoadView?.showSuccessView(this.items)

}


/**
 * SwipeRefreshLayout上拉加载 下拉刷新监听
 */
fun <T : Any> BaseQuickAdapter<T, *>.refreshLoadListener(
    refresh: SwipeRefreshLayout? = null,
    onRefresh: (() -> Unit)? = null,
    onLoad: (() -> Unit)? = null
): QuickAdapterHelper {

    refresh?.setOnRefreshListener {
        onRefresh?.invoke()

    }

    val mQuickAdapterHelper = QuickAdapterHelper.Builder(this)
        .setTrailingLoadStateAdapter(BottomLoadAdapter().setOnLoadMoreListener(object :
            TrailingLoadStateAdapter.OnTrailingListener {
            override fun onLoad() {
                onLoad?.invoke()
            }

            override fun onFailRetry() {
            }

            override fun isAllowLoading(): Boolean {

                return if (null == refresh){
                    true
                }else{
                    !refresh.isRefreshing
                }

            }

        })).build()


    return mQuickAdapterHelper

}


/**
 * SwipeRefreshLayout上拉加载 下拉刷新监听
 */
fun <T : Any> BaseQuickAdapter<T, *>.topLoadListener(
    onLoad: () -> Unit
): QuickAdapterHelper {

    val mQuickAdapterHelper = QuickAdapterHelper.Builder(this)
        .setLeadingLoadStateAdapter(TopLoadAdapter().setOnLeadingListener(object :
            OnLeadingListener {
            override fun onLoad() {
                onLoad()
            }

            override fun isAllowLoading(): Boolean {
                return true
            }

        })).build()


    return mQuickAdapterHelper

}

fun <T : Any> BaseQuickAdapter<T, *>.bottomLoadListener(
    onLoad: () -> Unit
): QuickAdapterHelper {

    val mQuickAdapterHelper = QuickAdapterHelper.Builder(this)
        .setTrailingLoadStateAdapter(BottomLoadAdapter().setOnLoadMoreListener(object :
            OnTrailingListener {
            override fun onLoad() {
                onLoad()
            }

            override fun onFailRetry() {
                onLoad()
            }


        })).build()

    return mQuickAdapterHelper

}








fun <T:PagingDataAdapter<*, *>> T.addLoadListener(
    swipeRefreshLayout: SwipeRefreshLayout? = null,
    errorReLoadView: ErrorReLoadView? = null,
    otherRefresh:(()-> Unit)? = null
) {

    errorReLoadView?.onClick = {

        refresh()

        otherRefresh?.invoke()

        errorReLoadView.showStatusView(ErrorReLoadView.Status.LOADING)
    }

    addLoadStateListener { loadState ->

        val isRefresh = loadState.source.refresh

        val isListEmpty = itemCount <= 0

        when {
            isRefresh is androidx.paging.LoadState.Loading -> {

                if (isListEmpty) {
                    errorReLoadView?.showStatusView(ErrorReLoadView.Status.LOADING)
                }
            }

            isRefresh is androidx.paging.LoadState.Error -> {

                if (isListEmpty) {
                    errorReLoadView?.showStatusView(ErrorReLoadView.Status.ERROR)
                }

            }

            isRefresh is androidx.paging.LoadState.NotLoading && isListEmpty -> {
                errorReLoadView?.showStatusView(ErrorReLoadView.Status.EMPTY)
            }

            else -> {
                errorReLoadView?.showStatusView(ErrorReLoadView.Status.NORMAL)
            }
        }

        swipeRefreshLayout?.isRefreshing = false
    }

    swipeRefreshLayout?.setOnRefreshListener {
        refresh()
        otherRefresh?.invoke()
    }

}





fun <T : Any> buildPager(
    pageSize: Int = AppConstant.Constant.PAGE_SIZE_COUNT,
    loadPage: suspend (Int) -> MResult<MutableList<T>>
): Flow<PagingData<T>> {
    return Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = 200
        ),
        pagingSourceFactory = { BasePagingSource(pageSize, loadPage) }
    ).flow
}
