package org.lovepeaceharmony.androidapp.ui.activity

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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
 * LoginDetailActivity
 * Created by Naveen Kumar M on 06/12/17.
 */
class LoginDetailActivity : AppCompatActivity() {
    private var context: Context? = null
    private var isFromProfile: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        if (intent.extras != null)
            isFromProfile = intent.getBooleanExtra(Constants.BUNDLE_IS_FROM_PROFILE, false)
        setContentView(R.layout.activity_login_detail)
        initView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initView() {
        val tvBack = findViewById<TextView>(R.id.tv_back)
        val tvCreateAccount = findViewById<TextView>(R.id.tv_create_account)
        val email = findViewById<EditText>(R.id.editText_Email)
        val password = findViewById<EditText>(R.id.editText_Password)
        val tvSignIn = findViewById<TextView>(R.id.tv_sign_in)
        val bannerImage = findViewById<ImageView>(R.id.banner_image)

        val width = Helper.getDisplayWidth(this)
        bannerImage.layoutParams.height = (width / 2)
        bannerImage.layoutParams.width = width
        bannerImage.requestLayout()

        tvBack.setOnClickListener { onBackPressed() }

        tvCreateAccount.setOnClickListener {
            val intent = Intent(context, CreateAccountActivity::class.java)
            startActivity(intent)
        }

        password.setOnEditorActionListener(TextView.OnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                //do here your stuff f
                login(textView, email, password)
                return@OnEditorActionListener true
            }
            false
        })

        tvSignIn.setOnClickListener { view -> login(view, email, password) }
    }

    private fun login(view: View, email: EditText, password: EditText) {
        val emailText = email.text.toString().trim { it <= ' ' }
        val passwordText = password.text.toString().trim { it <= ' ' }
        when {
            emailText.isEmpty() -> {
                Helper.showAlert(context!!, context!!.getString(R.string.please_enter_email))
                email.requestFocus()
            }
            passwordText.isEmpty() -> {
                Helper.showAlert(context!!, context!!.getString(R.string.please_enter_password))
                password.requestFocus()
            }
            else -> {

                if (Helper.isConnected(context!!)) {
                    val loginAsync = LoginAsync(this@LoginDetailActivity, emailText, passwordText)
                    loginAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                } else {

                    Helper.showConfirmationAlertTwoButton(context!!, context!!.getString(R.string.internet_warning), object : ConfirmationAlertCallback {
                        override fun onPositiveButtonClick() {
                            view.performClick()
                        }

                        override fun onNegativeButtonClick() {

                        }

                        override fun onNeutralButtonClick() {

                        }
                    })

                }
                Helper.hideSoftKeyboard(context, email)

            }
        }
    }

    private class LoginAsync internal constructor(context: LoginDetailActivity, private val email: String, private val password: String) : AsyncTask<Void, Void, Response<Any>>() {
        private var progress: MessageBox? = null
        private val context: WeakReference<LoginDetailActivity>

        init {
            this.context = WeakReference(context)
        }
        override fun onPreExecute() {
            progress = MessageBox(context.get()!!, context.get()!!.resources.getString(R.string.processing_request), context.get()!!.resources.getString(R.string.please_wait))
            progress!!.showProgress()
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): Response<Any> {
            var response = Response<Any>()
            try {
                val lphService = LPHServiceFactory.getCALFService(context.get()!!)
                val source = Helper.getSource(Constants.LoginType.Email)
                response = lphService.login(email, password, source)
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
            if (progress != null) {
                progress!!.hideProgress()
                progress = null
            }
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
                lphConstants.putString(Constants.SHARED_PREF_EMAIL, email)
                lphConstants.putString(Constants.SHARED_PREF_PASSWORD, password)
                lphConstants.putString(Constants.SHARED_PREF_SOURCE, source)
                lphConstants.putString(Constants.SHARED_PREF_PROFILE_PIC_URL, profilePic)
                lphConstants.putString(Constants.SHARED_PREF_INVITE_COUPON, inviteCoupon)
                lphConstants.putInt(Constants.SHARED_PREF_ID, id)
                lphConstants.apply()

//                Helper.clearAllDbValues(context = context.get()!!)
                Helper.setPlayList(context.get()!!, "songs", ".mp3")

                val intent = Intent(context.get()!!, MainActivity::class.java)
                intent.putExtra(Constants.BUNDLE_IS_FROM_PROFILE, context.get()!!.isFromProfile)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.get()!!.startActivity(intent)
                context.get()!!.finish()
            } else {
                MessageBox(context.get()!!, context.get()!!.resources.getString(R.string.alert), response.getServerMessage()!!).showMessage()
            }
        }
    }



}
