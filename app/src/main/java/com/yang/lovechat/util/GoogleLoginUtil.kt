package com.yang.lovechat.util

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.yang.lovechat.constant.AppConstant
import kotlin.jvm.java
import kotlin.text.isNullOrEmpty
import kotlin.toString


class GoogleLoginUtil {

    private val WEB_CLIENT_ID =
        "933853479195-s6e3lgdvmmuf4be0424okgkkrpu4ak8e.apps.googleusercontent.com"

    private val TAG = "GoogleLoginUtil"

    private var mGoogleSignInClient: GoogleSignInClient? = null

    private fun initCredentialManager(context: Context) {

        if (!AppConstant.ClientInfo.OPEN_GOOGLE) return

        if (null == mGoogleSignInClient) {
            val mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build()

            mGoogleSignInClient = GoogleSignIn.getClient(context, mGoogleSignInOptions)
        }
    }


    fun startLogin(context: Context): Intent? {

        if (!AppConstant.ClientInfo.OPEN_GOOGLE) return null

        initCredentialManager(context.applicationContext)

        return mGoogleSignInClient?.signInIntent


    }


    fun handleLoginResult(
        data: Intent?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {

            if (null == data){

                return
            }

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            val account = task.getResult(ApiException::class.java)

            val idToken = account.idToken
            if (idToken.isNullOrEmpty()){
                onError("idToken is empty")
            }else{
                onSuccess(idToken)
            }
        } catch (e: Exception) {
            onError(e.message.toString())
        }

    }

    fun remove(){
        mGoogleSignInClient = null
    }

    fun googleLogOut(context: Context) {

        initCredentialManager(context)

        mGoogleSignInClient?.signOut()

        mGoogleSignInClient = null
    }
}