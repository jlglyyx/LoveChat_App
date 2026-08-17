package com.yang.lovechat.base.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yang.lovechat.http.IHttpException
import com.yang.lovechat.http.MResult

class BasePagingSource<T : Any>(
    private val pageSize: Int,
    private val loadPage: suspend (Int) -> MResult<MutableList<T>>
) : PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        return try {
            val page = params.key ?: 1

            val loadPage = loadPage(page)

           if (!loadPage.success){
              throw IHttpException.HttpErrorException(loadPage.message, loadPage.code)
           }

            val data = loadPage.data
            LoadResult.Page(
                data = data,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (data.size < pageSize) null else page + 1
//                nextKey = if (data.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? = null
}
