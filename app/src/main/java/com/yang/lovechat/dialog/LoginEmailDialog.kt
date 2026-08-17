package com.yang.lovechat.dialog

import android.content.DialogInterface
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.SpanUtils
import com.yang.lovechat.R
import com.yang.lovechat.base.adapter.BaseRecyclerAdapter
import com.yang.lovechat.base.dialog.BaseDialog
import com.yang.lovechat.constant.AppConstant
import com.yang.lovechat.databinding.DialogLoginEmailBinding
import com.yang.lovechat.databinding.ItemEmailMenuBinding
import com.yang.lovechat.ui.activity.AStepActivity
import com.yang.lovechat.ui.activity.MainActivity
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.copyContent
import com.yang.lovechat.util.createIntent
import com.yang.lovechat.util.edgeToEdgeTop
import com.yang.lovechat.util.getCache
import com.yang.lovechat.util.hideSoftInput
import com.yang.lovechat.util.isVpnConnected
import com.yang.lovechat.util.startActivity
import com.yang.lovechat.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginEmailDialog : BaseDialog<DialogLoginEmailBinding>(DialogLoginEmailBinding::inflate) {

    private val mViewModel by sharedViewModels<MainViewModel>()

    private var isPasswordVisible = false


    private lateinit var mAdapter: BaseRecyclerAdapter<String, ItemEmailMenuBinding>

    private var emailMenu = arrayOf("@gmail.com", "@yahoo.com", "@outlook.com", "@hotmail.com")

    private var localPart = ""



    override fun initView() {

       withViewBinding {

            root.edgeToEdgeTop()

            initRecyclerView()

            lifecycleScope.launch {

                repeatOnLifecycle(Lifecycle.State.STARTED) {

                    etAccount.requestFocus()

                    delay(500)

                    etAccount.showSoftInputOnFocus = true

                    KeyboardUtils.showSoftInput()
                }


            }

           ivClose.setOnClickListener {

               dismissAllowingStateLoss()
           }


            etAccount.doAfterTextChanged {

                tvEmailNotice.visibility =
                    if (it.toString().contains("@")) View.INVISIBLE else View.VISIBLE

                ivEmailClear.visibility =
                    if (it.toString().isNotEmpty()) View.VISIBLE else View.GONE


                createEmailMenuData(it.toString().trim())

                canLogin()
            }
            etPassword.doAfterTextChanged {

                ivPasswordEyes.visibility =
                    if (it.toString().isNotEmpty()) View.VISIBLE else View.GONE
                ivPasswordClear.visibility =
                    if (it.toString().isNotEmpty()) View.VISIBLE else View.GONE

                canLogin()
            }

            ivEmailClear.setOnClickListener {

                etAccount.setText("")
            }
            ivPasswordClear.setOnClickListener {

                etPassword.setText("")
            }


            ivPasswordEyes.setOnClickListener {

                isPasswordVisible = !isPasswordVisible

                if (isPasswordVisible) {
                    etPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    ivPasswordEyes.setImageResource(R.drawable.iv_eyes)
                } else {
                    etPassword.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    ivPasswordEyes.setImageResource(R.drawable.iv_no_eyes)
                }

                etPassword.setSelection(etPassword.text?.length ?: 0)

            }


            stvLogin.clicks {


                val email = etAccount.text.toString().trim()

                val password = etPassword.text.toString().trim()

                if (email.isBlank() || password.isBlank()) {

                    return@clicks
                }


                if (getCache(AppConstant.Constant.IS_VPN,"False") == "True" && isVpnConnected(requireContext())){

                    initNoticeDialog()

                    return@clicks
                }

                mDialogBinding.etPassword.hideSoftInput(requireContext())

                mViewModel.login(AppConstant.Constant.EMAIL, email, password)
            }



        }


    }

    override fun initData() {
    }


    override fun initViewModel() {


        mViewModel.mLoginInfoData.observe(this) {



            if (it.firstLogin) {

                createIntent(AStepActivity::class.java).startActivity(requireActivity(), true)

            } else {

                createIntent(MainActivity::class.java).startActivity(requireActivity(), true)
            }

        }
        mViewModel.requestFailEvent.observe(this) {


            if (it is Boolean){
                initNoticeDialog()
            }

        }

    }


    private fun canLogin() {

      withViewBinding {
          stvLogin.isEnabled =
              etAccount.text.toString().isNotBlank() && etPassword.text.toString().isNotBlank()
      }

    }


    private fun initRecyclerView() {

        withViewBinding {

            mAdapter = object :
                BaseRecyclerAdapter<String, ItemEmailMenuBinding>(ItemEmailMenuBinding::inflate) {
                override fun convert(
                    holder: BaseRecyclerViewHolder<ItemEmailMenuBinding>,
                    itemView: ItemEmailMenuBinding,
                    item: String,
                    position: Int
                ) {

                    itemView.tvEmail.text = item

                }

            }
            recyclerView.adapter = mAdapter

            mAdapter.setOnItemClickListener { _, _, position ->

                val item = mAdapter.getItem(position) ?: return@setOnItemClickListener


                etAccount.setText(item)

                etAccount.setSelection(etAccount.text.toString().length)

                createEmailMenuData("")
            }


        }

    }


    private fun createEmailMenuData(text: String) {

        try {
            
            withViewBinding { 
                
            


            if (text.isBlank()) {
                mAdapter.submitList(null)
                recyclerView.visibility = View.GONE
                localPart = ""
                return
            }

            val isExactMatch = emailMenu.any { text.endsWith(it) }
            if (isExactMatch) {
                mAdapter.submitList(null)
                recyclerView.visibility = View.GONE
                return
            }

            val atIndex = text.indexOf("@")

            if (atIndex == -1) {

                localPart = text
                val suggestions = emailMenu.map { localPart + it }
                mAdapter.submitList(suggestions)
                recyclerView.visibility = View.VISIBLE
            } else {

                localPart = text.substring(0, atIndex)
                val domainPart = text.substring(atIndex).lowercase()


                val matchingDomains = emailMenu.filter { domain ->
                    domain.lowercase().startsWith(domainPart)
                }


                val suggestions = matchingDomains.map { localPart + it }

                if (suggestions.isNotEmpty()) {
                    mAdapter.submitList(suggestions)
                    recyclerView.visibility = View.VISIBLE
                } else {

                    mAdapter.submitList(null)
                    recyclerView.visibility = View.GONE
                }
            }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun initNoticeDialog() {


        NoticeDialog().apply {

            initView = { dialog, mViewBinding ->

                mViewBinding.tvTitle.visibility = View.GONE
                createNoticeText(mViewBinding.tvContent)
                mViewBinding.tvCancel.visibility = View.GONE
                mViewBinding.tvCommit.text = "OK"

            }

        }.show(parentFragmentManager)

    }

    private fun createNoticeText(text:TextView) {

        SpanUtils.with(text).append("Your account did not pass our security verification.If you believe this is a mistake, please contact us at:\n")
            .append("service@twomorrow-chat.com")
            .setClickSpan(getColor(R.color.color_C76BFF), true) {
                requireContext().copyContent("service@twomorrow-chat.com")

            }
            .create()
    }

    override fun setDialogHeight(): Int {
        return WindowManager.LayoutParams.MATCH_PARENT
    }

    override fun setDialogGravity(): Int {
        return Gravity.BOTTOM
    }

    override fun onDismiss(dialog: DialogInterface) {

//        mDialogBinding.root.hideSoftInput(requireContext())

        super.onDismiss(dialog)
    }

}