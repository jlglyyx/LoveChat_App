package com.yang.lovechat.helper

import com.yang.lovechat.ui.activity.MessageActivity
import java.lang.ref.WeakReference

object ChatHelper {

    val mChatList = mutableListOf<WeakReference<MessageActivity>>()

    fun add(mMessageActivity:MessageActivity){


        try {

        }catch (e: Exception){
            e.printStackTrace()
        }

        val weakReference = WeakReference(mMessageActivity)

        mChatList.add(weakReference)
    }

    fun remove(mMessageActivity:MessageActivity){

        try {
            val weakReference = WeakReference(mMessageActivity)

            if (mChatList.isNotEmpty()){

                mChatList.remove(weakReference)

            }
        }catch (e: Exception){
            e.printStackTrace()
        }



    }

    fun removeLast(){

        try {
            if (mChatList.isNotEmpty()){

                mChatList.forEach {

                    it.get()?.finish()
                }

                mChatList.clear()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }


    }
}