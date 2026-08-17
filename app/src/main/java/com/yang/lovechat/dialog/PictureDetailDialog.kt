package com.yang.lovechat.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.yang.lovechat.R
import com.yang.lovechat.adapter.PictureDetailAdapter
import com.yang.lovechat.base.dialog.BaseDialog
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.data.MediaEnumType
import com.yang.lovechat.data.PictureData
import com.yang.lovechat.databinding.DialogPirtureDetailBinding
import com.yang.lovechat.manager.VideoPlayerManager
import com.yang.lovechat.util.edgeToEdgeBottom
import com.yang.lovechat.util.edgeToEdgeTop
import com.yang.lovechat.util.getParcelableArrayListData
import com.yang.lovechat.widget.VideoPlayerView


class PictureDetailDialog() :
    BaseDialog<DialogPirtureDetailBinding>(DialogPirtureDetailBinding::inflate) {

    private val mPictureDetailAdapter by lazy { PictureDetailAdapter() }

    companion object {

        fun newInstance(data: List<PictureData>, currentPosition: Int): PictureDetailDialog {

            return PictureDetailDialog().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(
                        AppConstant.Constant.DATA,
                        data as ArrayList<PictureData>
                    )
                    putInt(AppConstant.Constant.POSITION, currentPosition)
                }
            }
        }
    }

    private var isPageChanged = false

    private var list: MutableList<PictureData> = mutableListOf()

    private var currentPageIndex = 0

    private var isFirst = true

    override fun onStart() {
        super.onStart()

        dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

    }


    override fun initView() {

        withViewBinding {

            clToolbar.edgeToEdgeTop()

            root.edgeToEdgeBottom()

            ivBack.setOnClickListener {
                dismissAllowingStateLoss()
            }

            initViewPager()

        }


    }

    override fun initData() {

        arguments?.apply {

            list = getParcelableArrayListData<PictureData>(AppConstant.Constant.DATA)
                ?: mutableListOf()

            mPictureDetailAdapter.submitList(list)

            currentPageIndex = getInt(AppConstant.Constant.POSITION, currentPageIndex)


            withViewBinding {

                tvCount.text = "${currentPageIndex}/${list.size}"


                viewPager.setCurrentItem(currentPageIndex, false)




            }
        }

    }


    private fun initViewPager() {

        withViewBinding {

            viewPager.offscreenPageLimit = 5


            viewPager[0].overScrollMode = View.OVER_SCROLL_NEVER


            val mRecyclerView = viewPager.getChildAt(0) as RecyclerView

            viewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(mPosition: Int) {
                    super.onPageSelected(mPosition)

                    isPageChanged = true

                    currentPageIndex = mPosition

                    tvCount.text = "${mPosition + 1}/${list.size}"

                    if(isFirst){

                        autoPlayVideo(mRecyclerView,currentPageIndex)

                        isFirst = false
                    }

                }

                override fun onPageScrollStateChanged(state: Int) {
                    if (state == ViewPager2.SCROLL_STATE_IDLE && isPageChanged) {

                        isPageChanged = false

                        Log.d("ViewPager", "页面完全翻页完成: $currentPageIndex")

                        autoPlayVideo(mRecyclerView,currentPageIndex)


                    }
                }

            })


            viewPager.adapter = mPictureDetailAdapter

        }
    }

    private fun autoPlayVideo(
        mRecyclerView: RecyclerView,
        currentPageIndex: Int
    ) {

        VideoPlayerManager.pause()

        val item = mPictureDetailAdapter.getItemOrNull(currentPageIndex) ?: return

        when (item.mediaEnumType) {
            MediaEnumType.LOCAL_VIDEO -> {
                togglePlayPause(mRecyclerView,currentPageIndex)
            }

            MediaEnumType.VIDEO -> {
                togglePlayPause(mRecyclerView,currentPageIndex)
            }

            else -> return
        }

    }

    private fun togglePlayPause(
        recyclerView: RecyclerView,
        currentPageIndex: Int
    ) {

        recyclerView.post {

            val findViewByPosition =
                recyclerView.layoutManager?.findViewByPosition(currentPageIndex) ?: return@post

            val mPlayerView =
                findViewByPosition.findViewById<VideoPlayerView>(R.id.pv_video) ?: return@post


            mPlayerView.togglePlayPause()
        }



    }


    override fun setDialogHeight(): Int {
        return WindowManager.LayoutParams.MATCH_PARENT
    }

    override fun onDismiss(dialog: DialogInterface) {
        VideoPlayerManager.release()
        super.onDismiss(dialog)
    }

}