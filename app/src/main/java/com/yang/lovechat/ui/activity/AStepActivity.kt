package com.yang.lovechat.ui.activity

import androidx.core.widget.doAfterTextChanged
import com.yang.lovechat.R
import com.yang.lovechat.base.activity.BaseActivity
import com.yang.lovechat.data.UpdateUserInfoData
import com.yang.lovechat.databinding.ActAStepBinding
import com.yang.lovechat.helper.UserInfoHold
import com.yang.lovechat.util.clicks
import com.yang.lovechat.util.createIntent
import com.yang.lovechat.util.dateFormat
import com.yang.lovechat.util.edgeToEdgeAll
import com.yang.lovechat.util.showShort
import com.yang.lovechat.util.startActivity
import com.yang.lovechat.util.toZodiac
import com.yang.lovechat.viewmodel.MainViewModel
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.jvm.java


class AStepActivity : BaseActivity<ActAStepBinding, MainViewModel>(ActAStepBinding::inflate) {


    private var currentSex = 0

    private var mCurrentDate: Date? = null

    private val mCalendar = Calendar.getInstance().apply {

        add(Calendar.YEAR, -26)
        add(Calendar.DAY_OF_WEEK, -1)
    }

    private var currentAge = 26

    override fun initView() {


        mViewBinding.root.edgeToEdgeAll()

        withViewBinding {


            sllMan.setOnClickListener {

                currentSex = 0

                sllMan.shapeDrawableBuilder.setSolidGradientColors(getColor(R.color.startColor),getColor(R.color.endColor)).intoBackground()
                sllWomen.shapeDrawableBuilder.setSolidGradientColors(getColor(R.color.color_222222),getColor(R.color.color_222222)).intoBackground()
                sllOther.shapeDrawableBuilder.setSolidGradientColors(getColor(R.color.color_222222),getColor(R.color.color_222222)).intoBackground()

            }
            sllWomen.setOnClickListener {

                currentSex = 1

                sllMan.shapeDrawableBuilder.setSolidGradientColors(getColor(R.color.color_222222),getColor(R.color.color_222222)).intoBackground()
                sllWomen.shapeDrawableBuilder.setSolidGradientColors(getColor(R.color.startColor),getColor(R.color.endColor)).intoBackground()
                sllOther.shapeDrawableBuilder.setSolidGradientColors(getColor(R.color.color_222222),getColor(R.color.color_222222)).intoBackground()
            }
            sllOther.setOnClickListener {

                currentSex = 2

                sllMan.shapeDrawableBuilder.setSolidGradientColors(getColor(R.color.color_222222),getColor(R.color.color_222222)).intoBackground()
                sllWomen.shapeDrawableBuilder.setSolidGradientColors(getColor(R.color.color_222222),getColor(R.color.color_222222)).intoBackground()
                sllOther.shapeDrawableBuilder.setSolidGradientColors(getColor(R.color.startColor),getColor(R.color.endColor)).intoBackground()
            }



            setName.doAfterTextChanged {

                stvNext.isEnabled = setName.text.toString().isNotBlank()
            }

            if (null != UserInfoHold.userInfo){

                setName.setHint(UserInfoHold.userInfo?.userName)

            }


            mCurrentDate = mCalendar.time

            mTimePickView.setCurrentTime(mCalendar.time.dateFormat("MM/dd/yyyy", Locale.US))

            mTimePickView.onTimeChange = { date, age ->

                mCurrentDate = date

                currentAge = age

            }


            stvNext.clicks {

                val mCurrentDate = mCurrentDate?:return@clicks


                val birth = mCurrentDate.time

                if (currentAge < 18){

                    showShort("Not allowed for use under 18 years old")

                    return@clicks
                }

                val name = setName.text.toString()

                val updateUserInfoData = UpdateUserInfoData()

                updateUserInfoData.userName = name

                updateUserInfoData.gender = currentSex

                updateUserInfoData.age = currentAge

                updateUserInfoData.birth = birth

                updateUserInfoData.constellation = mCurrentDate.toZodiac(this@AStepActivity)

                mViewModel.updateUserInfo(updateUserInfoData)

                showLoading()


            }
        }

    }

    override fun initViewModel() {

        mViewModel.mUpdateUserInfoStatus.observe(this) {

            dismissLoading()

            createIntent(BStepActivity::class.java).startActivity(this@AStepActivity)
        }

        mViewModel.requestFailEvent.observe(this) {


            dismissLoading()
        }
    }



    override fun initData() {


    }






}