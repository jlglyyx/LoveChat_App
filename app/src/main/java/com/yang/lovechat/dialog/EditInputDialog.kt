package com.yang.lovechat.dialog

import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.yang.lovechat.base.dialog.BaseDialog
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.databinding.DialogEditInputBinding
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.hideSoftInput

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.apply
import kotlin.let
import kotlin.text.isBlank
import kotlin.text.trim
import kotlin.toString

class EditInputDialog : BaseDialog<DialogEditInputBinding>(DialogEditInputBinding::inflate) {

    var onConfirm: ((String) -> Unit)? = null

    private var startText = ""


    companion object {
        fun newInstance(data: String,type: Int): EditInputDialog {
            return EditInputDialog().apply {
                arguments = Bundle().apply {
                    putString(AppConstant.Constant.DATA, data)
                    putInt(AppConstant.Constant.TYPE, type)
                }
            }
        }
    }


    override fun initView() {


        withViewBinding {

            arguments?.let {

                val name = it.getString(AppConstant.Constant.DATA, "")

                val type = it.getInt(AppConstant.Constant.TYPE, 0)

                if (type == 0) {
                    tvTitle.text = "Edit Name"
                    setText.setHint("Enter your name")
                    setText.filters = arrayOf(InputFilter.LengthFilter(20))
                    setText.minLines = 1
                } else {
                    tvTitle.text = "Edit Bio"
                    setText.setHint("Enter your bio")
                    setText.filters = arrayOf(InputFilter.LengthFilter(200))
                    setText.minLines = 4
                }

                startText = name

                setText.setText(name)

                setText.setSelection(setText.text.toString().length)

            }


            setText.doAfterTextChanged {

                tvSave.isEnabled = it.toString() != startText

            }


            ViewCompat.setOnApplyWindowInsetsListener(sclContainer) { v, insets ->
                val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                val navHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom


                val offset = if (imeHeight > 0) imeHeight else navHeight

                if (v.translationY != -offset.toFloat()) {
                    v.translationY = -offset.toFloat()
                }

                val screenOpen = offset > navHeight

                if (screenOpen){
                }

                Log.i(TAG, "initView: $offset")

                insets
            }

            llContainer.clicks {

                dismissAllowingStateLoss()
            }

            ivClose.clicks {

                dismissAllowingStateLoss()

            }

            tvSave.clicks {

                val toString = setText.text.toString()

                if (toString.isBlank()) return@clicks

                onConfirm?.invoke(toString.trim())

                dismissAllowingStateLoss()
            }


            lifecycleScope.launch {

                delay(200)

                setText.hideSoftInput(requireContext(),true)
            }

        }

    }

    override fun initData() {
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

    }

    override fun setDialogHeight(): Int {

        return WindowManager.LayoutParams.MATCH_PARENT

    }

    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }


}