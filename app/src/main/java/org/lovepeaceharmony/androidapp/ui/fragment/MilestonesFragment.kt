package org.lovepeaceharmony.androidapp.ui.fragment


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import org.json.JSONException
import org.json.JSONObject
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.ui.activity.LoginDetailActivity
import org.lovepeaceharmony.androidapp.utility.*
import org.lovepeaceharmony.androidapp.utility.http.LPHException
import org.lovepeaceharmony.androidapp.utility.http.LPHServiceFactory
import org.lovepeaceharmony.androidapp.utility.http.Response
import java.io.IOException
import java.lang.ref.WeakReference

/**
 * A simple [Fragment] subclass.
 * Use the [MilestonesFragment.newInstance] factory method to
 * create an instance of this fragment.
 * Created by Naveen Kumar M on 10/11/17.
 */
class MilestonesFragment : Fragment() {
    private var rlProgressContainer: View? = null
    private var googleSingIn: GoogleSingIn? = null
    private var callbackManager: CallbackManager? = null
    private var isFromFb: Boolean = false
    private var tvDays : TextView? = null
    private var tvMinutes : TextView? = null
    private var tvInviteCount : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*FbCode*/
        callbackManager = CallbackManager.Factory.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_milestones, container, false)
        initView(view)
        return view
    }

    override fun onResume() {
        super.onResume()
        if(!Helper.isConnected(context!!)) {
            Toast.makeText(context!!, context?.getString(R.string.no_connection), Toast.LENGTH_SHORT).show()
        }
    }

    private fun initView(view: View) {
        val loginContainer = view.findViewById<View>(R.id.milestone_login_container)
        val milestoneContainer = view.findViewById<View>(R.id.mile_stone_container)
        val googleSignInBtn = view.findViewById<View>(R.id.google_sign_in)
        val faceBookSignInBtn = view.findViewById<View>(R.id.facebook_sign_in_button)
        val tvSignIn = view.findViewById<TextView>(R.id.tv_sign_in)
        val shareThisApp = view.findViewById<View>(R.id.tv_share_this_app)
        val eraseMileStone = view.findViewById<View>(R.id.tv_erase_milestone)
        rlProgressContainer = view.findViewById(R.id.rl_progressContainer)
        tvDays = view.findViewById(R.id.tv_days)
        tvMinutes = view.findViewById(R.id.tv_minutes)
        tvInviteCount = view.findViewById(R.id.tv_people_count)

        val loginType = Helper.getStringFromPreference(context!!, Constants.SHARED_PREF_LOGIN_TYPE)

        if (loginType.equals(Constants.LoginType.WithoutEmail.name, ignoreCase = true)) {
            loginContainer.visibility = View.VISIBLE
            milestoneContainer.visibility = View.GONE
        } else {
            updateMileStoneValues()
            if(Helper.isConnected(context!!)) {
                val mileStoneAsync = MileStoneAsync(this)
                mileStoneAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }

            loginContainer.visibility = View.GONE
            milestoneContainer.visibility = View.VISIBLE
        }

        shareThisApp.setOnClickListener {
            try {
                Helper.onInviteClicked(context!!)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }

        tvSignIn.setOnClickListener {
            val intent = Intent(context, LoginDetailActivity::class.java)
            intent.putExtra(Constants.BUNDLE_IS_FROM_PROFILE, true)
            startActivity(intent)
        }

        googleSignInBtn.setOnClickListener {
            googleSingIn = GoogleSingIn(context!!)
            googleSingIn!!.checkUser()
        }

        eraseMileStone.setOnClickListener{
            Helper.showResetConfirmationAlert(context!!, context?.getString(R.string.this_cannot_be_undone)!!, object : ConfirmationAlertCallback {
                override fun onPositiveButtonClick() {
                    startResetAsync()
                }

                override fun onNegativeButtonClick() {

                }

                override fun onNeutralButtonClick() {

                }
            })
        }

        val loginButton = view.findViewById<LoginButton>(R.id.fb_login_button)
        faceBookSignInBtn.setOnClickListener {
            callbackManager = CallbackManager.Factory.create()

            if (loginButton != null) {
                loginButton.setReadPermissions("public_profile", "email", "user_friends")

                if (Helper.isConnected(context!!)) {
                    val lphApplication = activity!!.applicationContext as LPHApplication
                    lphApplication.isFromMileStoneFbLogin = true
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
        if (isFromFb) {
            callbackManager!!.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun startResetAsync() {
        if(Helper.isConnected(context!!)) {
            val resetMileStoneAsync = ResetMileStoneAsync(this@MilestonesFragment)
            resetMileStoneAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        } else {
            Helper.showConfirmationAlertTwoButton(context!!, context!!.getString(R.string.internet_warning), object : ConfirmationAlertCallback {
                override fun onPositiveButtonClick() {
                    startResetAsync()
                }

                override fun onNegativeButtonClick() {

                }

                override fun onNeutralButtonClick() {

                }
            })
        }
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment MilestonesFragment.
         */
        fun newInstance(): MilestonesFragment {
            return MilestonesFragment()
        }
    }

    private class MileStoneAsync internal constructor(context: MilestonesFragment) : AsyncTask<Void, Void, Response<Any>>() {

        private val context: WeakReference<MilestonesFragment> = WeakReference(context)


        override fun onPreExecute() {
            super.onPreExecute()
            context.get()?.rlProgressContainer!!.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: Void?): Response<Any> {
            var response = Response<Any>()
            try {
                val lphService = LPHServiceFactory.getCALFService(context.get()?.context!!)
                response = lphService.getMileStone()
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
            context.get()?.rlProgressContainer!!.visibility = View.GONE
            if (response.isSuccess()) {
                context.get()?.updateMileStoneValues()
            }
        }
    }

    private fun updateMileStoneValues() {
        if(context != null) {
            val days = Helper.getIntFromPreference(context!!, Constants.SHARED_PREF_MILESTONE_DAYS)
            val minutes = Helper.getIntFromPreference(context!!, Constants.SHARED_PREF_MILESTONE_MINUTES)
            val inviteCount = Helper.getIntFromPreference(context!!, Constants.SHARED_PREF_MILESTONE_INVITE_COUNT)
            val convertedMin = Helper.formatNumberToText(minutes.toLong())
            val convertedDays = Helper.formatNumberToText(days.toLong())
            val convertedInviteCount = Helper.formatNumberToText(inviteCount.toLong())
            tvMinutes?.text = convertedMin
            tvDays?.text = convertedDays
            tvInviteCount?.text = convertedInviteCount
        }
    }

    private class ResetMileStoneAsync internal constructor(context: MilestonesFragment) : AsyncTask<Void, Void, Response<Any>>() {
        private var progress: MessageBox? = null
        private val context: WeakReference<MilestonesFragment> = WeakReference(context)

        override fun onPreExecute() {
            progress = MessageBox(context.get()?.context!!, context.get()!!.resources.getString(R.string.processing_request), context.get()!!.resources.getString(R.string.please_wait))
            progress!!.showProgress()
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): Response<Any> {
            var response = Response<Any>()
            try {
                val lphService = LPHServiceFactory.getCALFService(context.get()?.context!!)
                response = lphService.restMileStone()
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
                context.get()?.updateMileStoneValues()
            } else {
                MessageBox(context.get()?.context!!, context.get()!!.resources.getString(R.string.alert), response.getServerMessage()!!).showMessage()
            }
        }
    }


    private val mileStoneBroadCast = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            LPHLog.d("Update MileStone BroadCast ")
            updateMileStoneValues()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity != null)
            activity!!.registerReceiver(mileStoneBroadCast, IntentFilter(Constants.BROADCAST_MILESTONES))
    }

    override fun onDetach() {
        super.onDetach()
        if (activity != null)
            activity!!.unregisterReceiver(mileStoneBroadCast)
    }
}
