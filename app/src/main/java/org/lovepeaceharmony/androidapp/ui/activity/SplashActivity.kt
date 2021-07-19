package org.lovepeaceharmony.androidapp.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import org.json.JSONException
import org.json.JSONObject
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.utility.*
import org.lovepeaceharmony.androidapp.utility.http.LPHException
import org.lovepeaceharmony.androidapp.utility.http.LPHServiceFactory
import org.lovepeaceharmony.androidapp.utility.http.Response
import java.io.IOException
import java.lang.ref.WeakReference

/**
 * SplashActivity
 * Created by Naveen Kumar M on 06/12/17.
 */
class SplashActivity : AppCompatActivity() {

    private var context: Context? = null
    var inviteCoupon: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        setContentView(R.layout.activity_splash)
        val bannerImage = findViewById<ImageView>(R.id.banner_image)

        val width = Helper.getDisplayWidth(this)
        bannerImage.layoutParams.height = (width / 2)
        bannerImage.layoutParams.width = width
        bannerImage.requestLayout()


        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener(this) { pendingDynamicLinkData ->
                    // Get deep link from result (may be null if no link is found)
                    var deepLink: Uri? = null
                    if (pendingDynamicLinkData != null) {
                        deepLink = pendingDynamicLinkData.link
                    }
                    //
                    // If the user isn't signed in and the pending Dynamic Link is
                    // an invitation, sign in the user anonymously, and record the
                    // referrer's UID.
                    //
                    if (deepLink != null && deepLink.getBooleanQueryParameter("invitedby", false)) {
                        inviteCoupon = deepLink.getQueryParameter("invitedby")
                        val cache = this@SplashActivity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                        val lphConstants = cache?.edit()
                        lphConstants?.putString(Constants.SHARED_PREF_INVITED_BY, inviteCoupon)
                        lphConstants?.apply()
                    }
                }



        Handler().postDelayed(/*
         * Showing splash screen with a timer. This will be useful when you
         * want to show case your app logo / company
         */
        {
            startApp()
        }, 1000)

    }



    private fun startApp() {
        val settings = getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE) // 0 - for private mode
        val isLoggedIn = settings.getBoolean(Constants.SHARED_PREF_IS_LOGIN, false)
        val loginType = Helper.getStringFromPreference(this, Constants.SHARED_PREF_LOGIN_TYPE)

        if (isLoggedIn && loginType != Constants.LoginType.WithoutEmail.name) {
            val email = Helper.getStringFromPreference(this, Constants.SHARED_PREF_EMAIL)
            val password = Helper.getStringFromPreference(this, Constants.SHARED_PREF_PASSWORD)
            val source = Helper.getStringFromPreference(this, Constants.SHARED_PREF_SOURCE)
            val weakReferenceContext = WeakReference(this.context!!)
            if(Helper.isConnected(this)) {

                val loginAsync = LoginAsync(this, email, password, source.toLowerCase(), weakReferenceContext)
                loginAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            } else {
                Helper.showConfirmationAlertTwoButton(this, this.getString(R.string.internet_warning), object : ConfirmationAlertCallback {
                    override fun onPositiveButtonClick() {
                        val loginAsync = LoginAsync(this@SplashActivity, email, password, source.toLowerCase(), weakReferenceContext)
                        loginAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                    }

                    override fun onNegativeButtonClick() {

                    }

                    override fun onNeutralButtonClick() {

                    }
                })
            }
        } else if(isLoggedIn && loginType == Constants.LoginType.WithoutEmail.name) {
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            intent.putExtra(Constants.BUNDLE_IS_FROM_PROFILE, false)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            val i = Intent(this@SplashActivity, LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
            finish()
        }

    }

    private class LoginAsync internal constructor(context: SplashActivity, private val email: String, private val password: String, private val source: String, private val weakReferenceContext: WeakReference<Context>) : AsyncTask<Void, Void, Response<Any>>() {
        private val context: WeakReference<SplashActivity> = WeakReference(context)


        override fun doInBackground(vararg voids: Void): Response<Any> {
            var response = Response<Any>()
            try {
                val lphService = LPHServiceFactory.getCALFService(context.get()!!)
                response = lphService.login(email, password, source = source)
            } catch (e: LPHException) {
                e.printStackTrace()
                response.setThrowable(e)
            } catch (e: JSONException) {
                e.printStackTrace()
                response.setThrowable(e)
            } catch (e: IOException) {
                e.printStackTrace()
                response.setThrowable(e)
            }

            return response
        }

        override fun onPostExecute(response: Response<Any>) {
            super.onPostExecute(response)
            if (response.isSuccess()) {
                val res: String = response.getResult() as String
                LPHLog.d("Response onPost 1: " + res)

                val jsonObj  = JSONObject(res)
                val dataObj = jsonObj.getJSONObject(Constants.PARSE_DATA)
                val userObj = dataObj.getJSONObject(Constants.PARSE_USER)
                val name = userObj.optString(Constants.API_NAME)
                val email = userObj.optString(Constants.API_EMAIL)
                val source = userObj.optString(Constants.API_SOURCE)
                val profilePic = userObj.optString(Constants.API_PROFILE_PIC_URL)
                val id = userObj.optInt(Constants.PARSE_ID)
                val inviteCoupon = userObj.optString(Constants.PARSE_INVITE_COUPON)
                val token = dataObj.optString(Constants.API_TOKEN)

                val cache = context.get()!!.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                val lphConstants = cache.edit()
                lphConstants.putBoolean(Constants.SHARED_PREF_IS_LOGIN, true)
                lphConstants.putString(Constants.SHARED_PREF_LOGIN_TYPE, Constants.LoginType.Email.name)
                lphConstants.putString(Constants.SHARED_PREF_TOKEN, token)
                lphConstants.putString(Constants.SHARED_PREF_USER_NAME, name)
                lphConstants.putString(Constants.SHARED_PREF_PASSWORD, password)
                lphConstants.putString(Constants.SHARED_PREF_EMAIL, email)
                lphConstants.putString(Constants.SHARED_PREF_SOURCE, source)
                lphConstants.putString(Constants.SHARED_PREF_PROFILE_PIC_URL, profilePic)
                lphConstants.putString(Constants.SHARED_PREF_INVITE_COUPON, inviteCoupon)
                lphConstants.putInt(Constants.SHARED_PREF_ID, id)
                lphConstants.apply()

//                Helper.clearAllDbValues(context = context.get()!!)
                val pendingMinutes = Helper.getFloatFromPreference(context.get()!!, Constants.SHARED_PREF_PENDING_MINUTES)
                LPHLog.d("Pending minutes in Splash Activity : " + pendingMinutes)
                Helper.callUpdateMileStoneAsync(weakReferenceContext, pendingMinutes)

                val intent = Intent(context.get()!!, MainActivity::class.java)
                intent.putExtra(Constants.BUNDLE_IS_FROM_PROFILE, false)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.get()!!.startActivity(intent)
                context.get()!!.finish()
            } else {
                MessageBox(context.get()!!, context.get()!!.resources.getString(R.string.alert), response.getServerMessage()!!).showMessage()
            }
        }
    }
}
