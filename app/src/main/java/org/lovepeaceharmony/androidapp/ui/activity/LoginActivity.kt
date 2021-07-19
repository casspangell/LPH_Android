package org.lovepeaceharmony.androidapp.ui.activity

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import org.json.JSONObject
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.utility.Constants
import org.lovepeaceharmony.androidapp.utility.GoogleSingIn
import org.lovepeaceharmony.androidapp.utility.Helper
import org.lovepeaceharmony.androidapp.utility.LPHLog
import java.lang.ref.WeakReference


/**
 * LoginActivity
 * Created by Naveen Kumar M on 06/12/17.
 */
class LoginActivity : AppCompatActivity() {
    private var context: Context? = null
    private var googleSingIn: GoogleSingIn? = null
    private var callbackManager: CallbackManager? = null
    private var isFromFb: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        setContentView(R.layout.activity_login)

        /*FbCode*/
        callbackManager = CallbackManager.Factory.create()
        initView()
    }

    private fun initView() {
        val tvSignIn = findViewById<TextView>(R.id.tv_sign_in)
        val tvWithoutSignIn = findViewById<TextView>(R.id.tv_without_sign_in)
        val googleSignInBtn = findViewById<View>(R.id.google_sign_in)
        val facebookSignInBtn = findViewById<View>(R.id.facebook_sign_in_button)
        val bannerImage = findViewById<ImageView>(R.id.banner_image)

        val width = Helper.getDisplayWidth(this)
        bannerImage.layoutParams.height = (width / 2)
        bannerImage.layoutParams.width = width
        bannerImage.requestLayout()

        tvSignIn.setOnClickListener {
            val intent = Intent(context, LoginDetailActivity::class.java)
            startActivity(intent)
        }

        tvWithoutSignIn.setOnClickListener {
            val cache = context!!.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
            val lphConstants = cache.edit()
            lphConstants.putBoolean(Constants.SHARED_PREF_IS_LOGIN, true)
            lphConstants.putString(Constants.SHARED_PREF_LOGIN_TYPE, Constants.LoginType.WithoutEmail.name)
            lphConstants.apply()
            Helper.setPlayList(context!!, "songs", ".mp3")
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        googleSignInBtn.setOnClickListener {
            if (Helper.isConnected(this@LoginActivity)) {
                isFromFb = false
                googleSingIn = GoogleSingIn(context!!)
                googleSingIn!!.checkUser()
            } else {
                Helper.showAlert(this@LoginActivity, this@LoginActivity.resources.getString(R.string.no_connection))
            }
        }

        facebookSignInBtn.setOnClickListener {
            val loginButton = findViewById<LoginButton>(R.id.fb_login_button)
            callbackManager = CallbackManager.Factory.create()

            if (loginButton != null) {
                loginButton.setReadPermissions("public_profile", "email", "user_friends")

                if (Helper.isConnected(this@LoginActivity)) {
                    isFromFb = true
                    loginButton.performClick()

                    loginButton.isPressed = true

                    loginButton.invalidate()

                    loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                        override fun onSuccess(loginResult: LoginResult) {

                            LPHLog.d("Facebook Login: ", "Coming to success")
                            val request = GraphRequest.newMeRequest(
                                    loginResult.accessToken
                            ) { `object`, response ->
                                // Application code
                                LPHLog.d("Access Token : " + loginResult.accessToken.token)
                                LPHLog.d("response JsonObject: ",  response.jsonObject.toString())
                                LPHLog.d("response: ", response.toString() + "")
                                LPHLog.d("response object: ", `object`.toString() + "")

                                val resultObj: JSONObject = response.jsonObject
                                val name: String = resultObj.optString("name")
                                val password: String = resultObj.optString("id")
                                val email: String = resultObj.optString("email")
                                val profilePicUrl = resultObj.optJSONObject("picture").optJSONObject("data").optString("url")

                                val weakReferenceContext: WeakReference<Context> = WeakReference(context!!)
                                val registerAsync = Helper.RegisterAsync(weakReferenceContext, email, password, profilePicUrl, name, false, Constants.LoginType.Facebook)
                                registerAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                            }
                            val parameters = Bundle()
                            parameters.putString("fields", "id,name,email,picture.type(large)")
                            request.parameters = parameters
                            request.executeAsync()
                        }

                        override fun onCancel() {
                            LPHLog.d("Facebook Login: ", "Coming to onCancel")
                        }

                        override fun onError(error: FacebookException) {
                            LPHLog.d("Facebook Login: ", "Coming to error")
                            LPHLog.d("Facebook Login Error: ", error.message!!)
                        }
                    })

                    loginButton.isPressed = false

                    loginButton.invalidate()
                } else {
                    Helper.showAlert(this@LoginActivity, this@LoginActivity.resources.getString(R.string.no_connection))
                }
            }
        }

    }

    override fun onPause() {
        super.onPause()
        LPHLog.d("LoginActivity onPause")
        if (googleSingIn != null)
            googleSingIn!!.pause()
    }

    override fun onStop() {
        super.onStop()
        LPHLog.d("LoginActivity onStop")
        if (googleSingIn != null)
            googleSingIn!!.pause()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (googleSingIn != null && requestCode == GoogleSingIn.RC_SIGN_IN) {
            googleSingIn!!.activityResult(requestCode, data)
        } else if (isFromFb) {
            callbackManager!!.onActivityResult(requestCode, resultCode, data)
        }
    }
}
