package org.lovepeaceharmony.androidapp.ui.fragment

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.androidquery.AQuery
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONException
import org.json.JSONObject
import org.lovepeaceharmony.androidapp.BuildConfig
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.ui.activity.LoginActivity
import org.lovepeaceharmony.androidapp.ui.activity.LoginDetailActivity
import org.lovepeaceharmony.androidapp.ui.activity.ProfilePicUploadActivity
import org.lovepeaceharmony.androidapp.utility.*
import org.lovepeaceharmony.androidapp.utility.http.LPHException
import org.lovepeaceharmony.androidapp.utility.http.LPHServiceFactory
import org.lovepeaceharmony.androidapp.utility.http.Response
import java.io.IOException
import java.lang.ref.WeakReference

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 * Created by Naveen Kumar M on 10/11/17.
 */
class ProfileFragment : Fragment() {

    private var onTabChange: OnTabChange? = null
    private var googleSingIn: GoogleSingIn? = null
    private var callbackManager: CallbackManager? = null
    private var isFromFb: Boolean = false
    private var aQuery: AQuery? = null
    private var ivProfile: RoundedImageView? = null

    fun setOnTabChange(onTabChange: OnTabChange) {
        this.onTabChange = onTabChange
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*FbCode*/
        callbackManager = CallbackManager.Factory.create()
        aQuery = AQuery(context!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        val tvSignOut = view.findViewById<TextView>(R.id.tv_sign_out)
        val tvManageFavorite = view.findViewById<TextView>(R.id.tv_manage_favorites)
        val tvViewMilestone = view.findViewById<TextView>(R.id.tv_view_milestones)
        val tvShareThisApp = view.findViewById<TextView>(R.id.tv_share_app)
        val googleSignInBtn = view.findViewById<View>(R.id.google_sign_in)
        val faceBookSignInBtn = view.findViewById<View>(R.id.facebook_sign_in_button)
        val tvVersion = view.findViewById<TextView>(R.id.tvVersion)

        try {
            context?.let {
                tvVersion.text = "version ${it.packageManager.getPackageInfo(it.packageName, 0)}"
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }


        tvSignOut.setOnClickListener {
            startLogout()
        }

        tvManageFavorite.setOnClickListener {
            if (onTabChange != null) {
                Handler().postDelayed({ onTabChange!!.onTabChange(1) }, 300)
            }
        }

        tvViewMilestone.setOnClickListener {
            if (onTabChange != null) {
                Handler().postDelayed({ onTabChange!!.onTabChange(0) }, 300)
            }
        }

        googleSignInBtn.setOnClickListener {
            googleSingIn = GoogleSingIn(context!!)
            googleSingIn!!.checkUser()
        }

        val loginButton = view.findViewById<LoginButton>(R.id.fb_login_button)
        faceBookSignInBtn.setOnClickListener {
            callbackManager = CallbackManager.Factory.create()

            if (loginButton != null) {
                loginButton.setReadPermissions("public_profile", "email", "user_friends")

                if (Helper.isConnected(context!!)) {
                    val lphApplication = activity!!.applicationContext as LPHApplication
                    lphApplication.isFromProfileFbLogin = true
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
                            parameters.putString("fields", "id,name,email,gender, birthday,picture.type(large)")
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
                    Helper.showAlert(context!!, context!!.resources.getString(R.string.no_connection))
                }
            }
        }

        tvShareThisApp.setOnClickListener {
            try {
                Helper.onInviteClicked(context!!)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }


        updateView(view)
    }

    private fun updateView(view: View) {
        val loginLayout = view.findViewById<View>(R.id.login_layout)
        val profileLayout = view.findViewById<View>(R.id.profile_layout)
        val editProfilePic = view.findViewById<View>(R.id.edit_profile)
        val tvSignIn = view.findViewById<TextView>(R.id.tv_sign_in)
        val tvProfileName = view.findViewById<TextView>(R.id.tv_profile_name)
        ivProfile = view.findViewById(R.id.iv_profile)
        val profileBackground = view.findViewById<View>(R.id.profile_background)

        val loginType = Helper.getStringFromPreference(context!!, Constants.SHARED_PREF_LOGIN_TYPE)

        if (loginType.equals(Constants.LoginType.WithoutEmail.name, ignoreCase = true)) {
            loginLayout.visibility = View.VISIBLE
            profileLayout.visibility = View.GONE
            profileBackground.setBackgroundColor(ContextCompat.getColor(context!!, R.color.profile_grey))
            editProfilePic.visibility = View.GONE
        } else {
            loginLayout.visibility = View.GONE
            profileLayout.visibility = View.VISIBLE
            editProfilePic.visibility = View.VISIBLE
            profileBackground.setBackgroundColor(ContextCompat.getColor(context!!, R.color.profile_green_color))
            val userName = Helper.getStringFromPreference(context!!, Constants.SHARED_PREF_USER_NAME)
            val profilePicUrl = Helper.getStringFromPreference(context!!, Constants.SHARED_PREF_PROFILE_PIC_URL)
            if(profilePicUrl != null && !profilePicUrl.isEmpty())
                aQuery?.id(ivProfile)?.image(profilePicUrl, true, true, 82, 0)
            tvProfileName.text = userName

            editProfilePic.setOnClickListener {
                val intent = Intent(context, ProfilePicUploadActivity::class.java)
                startActivityForResult(intent, Constants.REQUEST_CODE_PROFILE_UPLOAD)
            }
        }

        tvSignIn.setOnClickListener {
            val intent = Intent(context, LoginDetailActivity::class.java)
            intent.putExtra(Constants.BUNDLE_IS_FROM_PROFILE, true)
            startActivity(intent)
        }
    }


    override fun onPause() {
        super.onPause()
        if (googleSingIn != null)
            googleSingIn!!.pause()
    }

    override fun onStop() {
        super.onStop()
        if (googleSingIn != null)
            googleSingIn!!.pause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LPHLog.d("Profile OnActivityResult")
        if (googleSingIn != null && requestCode == GoogleSingIn.RC_SIGN_IN) {
            googleSingIn!!.activityResult(requestCode, data!!)
        } else if(requestCode == Constants.REQUEST_CODE_PROFILE_UPLOAD) {
            val profilePicUrl = Helper.getStringFromPreference(context!!, Constants.SHARED_PREF_PROFILE_PIC_URL)
            aQuery?.id(ivProfile)?.image(profilePicUrl, true, true, 82, 0)

        } else if (isFromFb) {
            callbackManager!!.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment ProfileFragment.
         */
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }

    private fun startLogout(){
        if(Helper.isConnected(context!!)) {
            val logoutAsync = LogoutAsync(this@ProfileFragment)
            logoutAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        } else {
            Helper.showConfirmationAlertTwoButton(context!!, context!!.getString(R.string.internet_warning), object : ConfirmationAlertCallback {
                override fun onPositiveButtonClick() {
                    startLogout()
                }

                override fun onNegativeButtonClick() {

                }

                override fun onNeutralButtonClick() {

                }
            })
        }
    }

    private class LogoutAsync internal constructor(context: ProfileFragment) : AsyncTask<Void, Void, Response<Any>>() {
        private var progress: MessageBox? = null
        private val context: WeakReference<ProfileFragment> = WeakReference(context)

        override fun onPreExecute() {
            progress = MessageBox(context.get()?.context!!, context.get()!!.resources.getString(R.string.processing_request), context.get()!!.resources.getString(R.string.please_wait))
            progress!!.showProgress()
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): Response<Any> {
            var response = Response<Any>()
            try {
                val lphService = LPHServiceFactory.getCALFService(context.get()?.context!!)
                val deviceToken = Helper.getStringFromPreference(context = context.get()?.context!!, key = Constants.SHARED_PREF_DEVICE_TOKEN)
                val params = HashMap<String, String>()
                params[Constants.API_DEVICE_TOKEN] =  deviceToken
                response = lphService.logOut(params)
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
                val loginType = Helper.getStringFromPreference(context.get()?.context!!, Constants.SHARED_PREF_LOGIN_TYPE)

                Helper.clearAllDbValues(context = context.get()?.context!!)
                val cache = context.get()?.context!!.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                cache?.edit()?.clear()?.apply()

                if (loginType.equals(Constants.LoginType.Google.name, ignoreCase = true)) {
                    FirebaseAuth.getInstance().signOut()
                } else if (loginType.equals(Constants.LoginType.Facebook.name, ignoreCase = true)) {
                    LoginManager.getInstance().logOut()
                }

                val i = Intent(context.get()?.context!!, LoginActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.get()?.context!!.startActivity(i)
            } else {
                MessageBox(context.get()?.context!!, context.get()!!.resources.getString(R.string.alert), response.getServerMessage()!!).showMessage()
            }
        }
    }
}
