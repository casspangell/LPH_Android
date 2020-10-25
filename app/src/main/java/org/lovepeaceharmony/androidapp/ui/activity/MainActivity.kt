package org.lovepeaceharmony.androidapp.ui.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.firebase.iid.FirebaseInstanceId
import org.json.JSONException
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.ui.fragment.AboutFragment
import org.lovepeaceharmony.androidapp.ui.fragment.ChantFragment
import org.lovepeaceharmony.androidapp.ui.fragment.NewsFragment
import org.lovepeaceharmony.androidapp.ui.fragment.ProfileFragment
import org.lovepeaceharmony.androidapp.utility.*
import org.lovepeaceharmony.androidapp.utility.http.LPHException
import org.lovepeaceharmony.androidapp.utility.http.LPHServiceFactory
import org.lovepeaceharmony.androidapp.utility.http.Response
import java.io.IOException
import java.lang.ref.WeakReference

/**
 * MainActivity
 * Created by Naveen Kumar M on 09/11/17.
 */
class MainActivity : AppCompatActivity(), OnTabChange {
    private var isFromProfile: Boolean = false
    private var mainLayout: View? = null
    private var chantSelectedColor: Int = 0
    private var newsSelectedColor: Int = 0
    private var aboutSelectedColor: Int = 0
    private var profileSelectedColor: Int = 0
    private var layoutDeselectedColor: Int = 0
    private var whiteColor: Int = 0
//    private var blackColor: Int = 0
    private var imageUnselectedColor: Int = 0

    /*Chant Button Properties*/
    private var layoutChant: LinearLayout? = null
    private var imageChant: ImageView? = null
    private var tvChant: TextView? = null

    /*News Button Properties*/
    private var layoutNews: LinearLayout? = null
    private var imageNews: ImageView? = null
    private var tvNews: TextView? = null

    /*About Button Properties*/
    private var layoutAbout: LinearLayout? = null
    private var imageAbout: ImageView? = null
    private var tvAbout: TextView? = null

    /*Profile Button Properties*/
    private var layoutProfile: LinearLayout? = null
    private var imageProfile: ImageView? = null
    private var tvProfile: TextView? = null

    private var profileFragment: ProfileFragment? = null
    private var chantFragment: ChantFragment? = null
    private var newsFragment: NewsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.extras != null)
            isFromProfile = intent.getBooleanExtra(Constants.BUNDLE_IS_FROM_PROFILE, false)
        setContentView(R.layout.activity_main)
        initView()

        val fcmToken = FirebaseInstanceId.getInstance().token
        val deviceToken = Helper.getStringFromPreference(this, Constants.SHARED_PREF_DEVICE_TOKEN)
        val token = Helper.getStringFromPreference(this, Constants.SHARED_PREF_TOKEN)
        if(deviceToken == null || deviceToken.isEmpty() && !token.isEmpty() && Helper.isConnected(context = this@MainActivity)){
            LPHLog.d("FCM TOKEN : " + fcmToken!!)
            val updateDeviceTokenAsync = UpdateDeviceTokenAsync(this, fcmToken!!)
            updateDeviceTokenAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
        registerReceiver(toolTipReceiver, IntentFilter(Constants.BROADCAST_MAIN_BOTTOM_LAYOUT))
        registerReceiver(clearService, IntentFilter(Constants.BROADCAST_CLEAR_THREAD))
        startService(Intent(this, ThreadClearService::class.java))
    }

    private fun initView() {

        /*Default Values*/
        chantSelectedColor = ContextCompat.getColor(this, R.color.top_bar_orange)
        newsSelectedColor = ContextCompat.getColor(this, R.color.colorPrimary)
        aboutSelectedColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        profileSelectedColor = ContextCompat.getColor(this, R.color.profile_selected_color)
        layoutDeselectedColor = ContextCompat.getColor(this, R.color.bottom_bar_bg)
        whiteColor = ContextCompat.getColor(this, android.R.color.white)
//        blackColor = ContextCompat.getColor(this, android.R.color.black)
        imageUnselectedColor = ContextCompat.getColor(this, R.color.bottom_icon_color)

        /*Chant Button Properties*/
        layoutChant = findViewById(R.id.layout_chant_now)
        imageChant = findViewById(R.id.image_chant)
        tvChant = findViewById(R.id.tv_chant)

        /*News Button Properties*/
        layoutNews = findViewById(R.id.layout_news)
        imageNews = findViewById(R.id.image_news)
        tvNews = findViewById(R.id.tv_news)
        val fragmentManager = supportFragmentManager

        /*About Button Properties*/
        layoutAbout = findViewById(R.id.layout_about)
        imageAbout = findViewById(R.id.image_about)
        tvAbout = findViewById(R.id.tv_about)

        /*Profile Button Properties*/
        layoutProfile = findViewById(R.id.layout_profile)
        imageProfile = findViewById(R.id.image_profile)
        tvProfile = findViewById(R.id.tv_profile)


        if (isFromProfile) {
            /*Default Profile Selected*/
            profileFragment = ProfileFragment.newInstance()
            profileFragment!!.setOnTabChange(this)
            fragmentManager.beginTransaction()
                    .replace(R.id.home_container, profileFragment).commit()

            layoutProfile!!.setBackgroundColor(profileSelectedColor)
            imageProfile!!.setColorFilter(whiteColor)
            tvProfile!!.setTextColor(whiteColor)
        } else {
            /*Default Chant Selected*/
            val bundle = Bundle()
            bundle.putInt(Constants.BUNDLE_TAB_INDEX, 0)
            chantFragment = ChantFragment.newInstance()
            chantFragment?.arguments = bundle
            fragmentManager.beginTransaction()
                    .replace(R.id.home_container, chantFragment).commit()

            layoutChant!!.setBackgroundColor(chantSelectedColor)
            imageChant!!.setColorFilter(whiteColor)
            tvChant!!.setTextColor(whiteColor)
        }

        layoutChant!!.setOnClickListener { view ->
            val context: Context = this
            val weakReferenceContext = WeakReference(context)
            val pendingMinutes = Helper.getFloatFromPreference(context, Constants.SHARED_PREF_PENDING_MINUTES)
            Helper.callUpdateMileStoneAsync(weakReferenceContext, pendingMinutes)

            if(chantFragment == null)
                chantFragment = ChantFragment.newInstance()
            val bundle = Bundle()
            bundle.putInt(Constants.BUNDLE_TAB_INDEX, 0)
            chantFragment?.arguments = bundle
            fragmentManager.beginTransaction()
                    .replace(R.id.home_container, chantFragment).commit()

            /*Selected*/
            view.setBackgroundColor(chantSelectedColor)
            imageChant!!.setColorFilter(whiteColor)
            tvChant!!.setTextColor(whiteColor)

            /*Deselected*/
            layoutNews!!.setBackgroundColor(layoutDeselectedColor) //News
            imageNews!!.setColorFilter(imageUnselectedColor)
            tvNews!!.setTextColor(imageUnselectedColor)

            layoutAbout!!.setBackgroundColor(layoutDeselectedColor) //About
            imageAbout!!.setColorFilter(imageUnselectedColor)
            tvAbout!!.setTextColor(imageUnselectedColor)

            layoutProfile!!.setBackgroundColor(layoutDeselectedColor) //Profile
            imageProfile!!.setColorFilter(imageUnselectedColor)
            tvProfile!!.setTextColor(imageUnselectedColor)
        }

        layoutNews!!.setOnClickListener { view ->
            newsFragment = NewsFragment.newInstance()
            fragmentManager.beginTransaction()
                    .replace(R.id.home_container, newsFragment).commit()

            /*Selected*/
            view.setBackgroundColor(newsSelectedColor)
            imageNews!!.setColorFilter(whiteColor)
            tvNews!!.setTextColor(whiteColor)

            /*Deselected*/
            layoutChant!!.setBackgroundColor(layoutDeselectedColor) //Chant
            imageChant!!.setColorFilter(imageUnselectedColor)
            tvChant!!.setTextColor(imageUnselectedColor)

            layoutAbout!!.setBackgroundColor(layoutDeselectedColor) //About
            imageAbout!!.setColorFilter(imageUnselectedColor)
            tvAbout!!.setTextColor(imageUnselectedColor)

            layoutProfile!!.setBackgroundColor(layoutDeselectedColor) //Profile
            imageProfile!!.setColorFilter(imageUnselectedColor)
            tvProfile!!.setTextColor(imageUnselectedColor)
        }

        layoutAbout!!.setOnClickListener { view ->
            val aboutFragment = AboutFragment.newInstance()
            fragmentManager.beginTransaction()
                    .replace(R.id.home_container, aboutFragment).commit()

            /*Selected*/
            view.setBackgroundColor(aboutSelectedColor)
            imageAbout!!.setColorFilter(whiteColor)
            tvAbout!!.setTextColor(whiteColor)

            /*Deselected*/
            layoutChant!!.setBackgroundColor(layoutDeselectedColor) // Chant
            imageChant!!.setColorFilter(imageUnselectedColor)
            tvChant!!.setTextColor(imageUnselectedColor)

            layoutNews!!.setBackgroundColor(layoutDeselectedColor) //News
            imageNews!!.setColorFilter(imageUnselectedColor)
            tvNews!!.setTextColor(imageUnselectedColor)

            layoutProfile!!.setBackgroundColor(layoutDeselectedColor) //Profile
            imageProfile!!.setColorFilter(imageUnselectedColor)
            tvProfile!!.setTextColor(imageUnselectedColor)
        }

        layoutProfile!!.setOnClickListener { view ->
            profileFragment = ProfileFragment.newInstance()
            profileFragment!!.setOnTabChange(this@MainActivity)
            fragmentManager.beginTransaction()
                    .replace(R.id.home_container, profileFragment).commit()

            /*Selected*/
            view.setBackgroundColor(profileSelectedColor)
            imageProfile!!.setColorFilter(whiteColor)
            tvProfile!!.setTextColor(whiteColor)

            /*Deselected*/
            layoutChant!!.setBackgroundColor(layoutDeselectedColor) // Chant
            imageChant!!.setColorFilter(imageUnselectedColor)
            tvChant!!.setTextColor(imageUnselectedColor)

            layoutNews!!.setBackgroundColor(layoutDeselectedColor) //News
            imageNews!!.setColorFilter(imageUnselectedColor)
            tvNews!!.setTextColor(imageUnselectedColor)

            layoutAbout!!.setBackgroundColor(layoutDeselectedColor) //About
            imageAbout!!.setColorFilter(imageUnselectedColor)
            tvAbout!!.setTextColor(imageUnselectedColor)
        }


    }


    private fun checkPermission() {

        if (!Helper.checkExternalStoragePermission(this)) {
            requestStoragePermission(findViewById(R.id.lay_home_activity_parent))
        }
    }

    private val toolTipReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            LPHLog.d("Tool Tip : Tool Tip Receiver MainActivity")
            val isToolTipShown = Helper.getBooleanFromPreference(this@MainActivity, Constants.SHARED_PREF_IS_TOOL_TIP_SHOWN)
            if (!isToolTipShown) {
                /*val sequence = TapTargetSequence(this@MainActivity)
                        .targets(
                                // This tap target will target the back button, we just need to pass its containing toolbar
                                TapTarget.forView(layoutNews, this@MainActivity.getString(R.string.get_news_updates_tool_tip))
                                        .dimColor(android.R.color.white)
                                        .outerCircleColor(R.color.tool_tip_color3)
                                        .targetCircleColor(android.R.color.black)
                                        .transparentTarget(true)
                                        .textColor(android.R.color.white)
                                        .id(3)

                        )
                        .listener(object : TapTargetSequence.Listener {
                            // This listener will tell us when interesting(tm) events happen in regards
                            // to the sequence
                            override fun onSequenceFinish() {
                                LPHLog.d("Tool Tip : Sequence Finished MainActivity")
                                val cache = this@MainActivity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                                val lphConstants = cache?.edit()
                                lphConstants?.putBoolean(Constants.SHARED_PREF_IS_TOOL_TIP_SHOWN, true)
                                lphConstants?.apply()
                                checkPermission()
                            }

                            override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {
                            }

                            override fun onSequenceCanceled(lastTarget: TapTarget) {

                            }
                        })

                sequence.start()*/

                TapTargetView.showFor(this@MainActivity, TapTarget.forView(layoutNews, this@MainActivity.getString(R.string.get_news_updates_tool_tip), this@MainActivity.getString(R.string.tap_here_to_continue))
                        .cancelable(false)
//                    .drawShadow(true)
                        .dimColor(android.R.color.white)
                        .outerCircleColor(R.color.tool_tip_color3)
                        .targetCircleColor(android.R.color.black)
                        .transparentTarget(true)
                        .textColor(android.R.color.white)
                        .titleTextSize(18)
                        .descriptionTextSize(14)
                        .textTypeface(ResourcesCompat.getFont(this@MainActivity, R.font.open_sans_semi_bold))
                        .descriptionTypeface(ResourcesCompat.getFont(this@MainActivity, R.font.open_sans_regular))
                        .id(1)
                        .tintTarget(false), object : TapTargetView.Listener() {
                    override fun onTargetClick(view: TapTargetView) {
                        super.onTargetClick(view)
                        // .. which evidently starts the sequence we defined earlier
                        LPHLog.d("Tool Tip : Sequence Finished MainActivity")
                        val cache = this@MainActivity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                        val lphConstants = cache?.edit()
                        lphConstants?.putBoolean(Constants.SHARED_PREF_IS_TOOL_TIP_SHOWN, true)
                        lphConstants?.apply()
                        checkPermission()
                    }

                    override fun onOuterCircleClick(view: TapTargetView?) {
                        super.onOuterCircleClick(view)
                        view?.dismiss(true)
                        LPHLog.d("Tool Tip : Sequence Finished MainActivity")
                        val cache = this@MainActivity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                        val lphConstants = cache?.edit()
                        lphConstants?.putBoolean(Constants.SHARED_PREF_IS_TOOL_TIP_SHOWN, true)
                        lphConstants?.apply()
                        checkPermission()
                    }

                    override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                        Log.d("TapTargetViewSample", "You dismissed me :(")
                    }
                })
            }
        }
    }


    private val clearService = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            LPHLog.d("Thread clear Broadcast")
//            chantFragment?.unRegisterPlayer()

        }
    }


    private fun requestStoragePermission(mainLayout: View?) {
        this.mainLayout = mainLayout
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            val snackBar = Snackbar.make(mainLayout!!, this.getString(R.string.storage_permission_needed), Snackbar.LENGTH_INDEFINITE)
            snackBar.setActionTextColor(ContextCompat.getColor(this, android.R.color.white))
            val snackBarView = snackBar.view
            snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.top_bar_orange))
            snackBar.setAction(this.getString(R.string.ok)) { ActivityCompat.requestPermissions(this@MainActivity, PERMISSIONS_STORAGE, 0) }
            snackBar.show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PermissionUtil.verifyPermissions(grantResults)) {

            val snackBar = Snackbar.make(mainLayout!!, this.getString(R.string.permission_granted), Snackbar.LENGTH_SHORT)
            snackBar.setActionTextColor(ContextCompat.getColor(this, android.R.color.white))
            val snackBarView = snackBar.view
            snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.top_bar_orange))
            snackBar.show()

        } else {
            var showRationale = false
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)
            }
            if (!showRationale) {
                val snackBar = Snackbar.make(mainLayout!!, this.getString(R.string.enable_storage_permission), Snackbar.LENGTH_SHORT)
                snackBar.setActionTextColor(ContextCompat.getColor(this, android.R.color.white))
                val snackBarView = snackBar.view
                snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.top_bar_orange))
                snackBar.show()
            } else {
                requestStoragePermission(mainLayout)
            }
        }

    }

    override fun onTabChange(index: Int) {
        if (index == 0) {
            navigateChatMileStone()
        } else if (index == 1) {
            navigateNewsFavorite()
        }
    }

    private fun navigateChatMileStone() {
        val context: Context = this
        val weakReferenceContext = WeakReference(context)
        val pendingMinutes = Helper.getFloatFromPreference(context, Constants.SHARED_PREF_PENDING_MINUTES)
        Helper.callUpdateMileStoneAsync(weakReferenceContext, pendingMinutes)

        val fragmentManager = supportFragmentManager
        val bundle = Bundle()
        bundle.putInt(Constants.BUNDLE_TAB_INDEX, 2)
        if(chantFragment == null)
            chantFragment = ChantFragment.newInstance()
        chantFragment!!.arguments = bundle
        fragmentManager.beginTransaction()
                .replace(R.id.home_container, chantFragment).commit()

        /*Selected*/
        layoutChant!!.setBackgroundColor(chantSelectedColor)
        imageChant!!.setColorFilter(whiteColor)
        tvChant!!.setTextColor(whiteColor)

        /*Deselected*/
        layoutNews!!.setBackgroundColor(layoutDeselectedColor) //News
        imageNews!!.setColorFilter(imageUnselectedColor)
        tvNews!!.setTextColor(imageUnselectedColor)

        layoutAbout!!.setBackgroundColor(layoutDeselectedColor) //About
        imageAbout!!.setColorFilter(imageUnselectedColor)
        tvAbout!!.setTextColor(imageUnselectedColor)

        layoutProfile!!.setBackgroundColor(layoutDeselectedColor) //Profile
        imageProfile!!.setColorFilter(imageUnselectedColor)
        tvProfile!!.setTextColor(imageUnselectedColor)
    }

    private fun navigateNewsFavorite() {
        val fragmentManager = supportFragmentManager
        newsFragment = NewsFragment.newInstance()
        val bundle = Bundle()
        bundle.putInt(Constants.BUNDLE_TAB_INDEX, 2)
        newsFragment!!.arguments = bundle
        fragmentManager.beginTransaction()
                .replace(R.id.home_container, newsFragment).commit()

        /*Selected*/
        layoutNews!!.setBackgroundColor(newsSelectedColor)
        imageNews!!.setColorFilter(whiteColor)
        tvNews!!.setTextColor(whiteColor)

        /*Deselected*/
        layoutChant!!.setBackgroundColor(layoutDeselectedColor) //Chant
        imageChant!!.setColorFilter(imageUnselectedColor)
        tvChant!!.setTextColor(imageUnselectedColor)

        layoutAbout!!.setBackgroundColor(layoutDeselectedColor) //About
        imageAbout!!.setColorFilter(imageUnselectedColor)
        tvAbout!!.setTextColor(imageUnselectedColor)

        layoutProfile!!.setBackgroundColor(layoutDeselectedColor) //Profile
        imageProfile!!.setColorFilter(imageUnselectedColor)
        tvProfile!!.setTextColor(imageUnselectedColor)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(toolTipReceiver)
        LPHLog.d("Main Activity On Destroy MainActivity")
        chantFragment?.unRegisterPlayer()
        unregisterReceiver(clearService)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LPHLog.d("MainActivity OnActivityResult requestCode=$requestCode, resultCode=$resultCode")
        val lphApplication = applicationContext as LPHApplication
        if (requestCode == GoogleSingIn.RC_SIGN_IN) {
            val googleSingIn = GoogleSingIn(this@MainActivity)
            LPHLog.d("Coming Google SignIn on MainActivity")
            if(data != null)
                googleSingIn.activityResult(requestCode, data)
        } else if (lphApplication.isFromProfileFbLogin && profileFragment != null) {
            lphApplication.isFromProfileFbLogin = false
            profileFragment!!.onActivityResult(requestCode, resultCode, data)
        } else if (lphApplication.isFromMileStoneFbLogin && chantFragment != null) {
            lphApplication.isFromMileStoneFbLogin = false
            chantFragment!!.onActivityResult(requestCode, resultCode, data)
        } else if (lphApplication.isFromFavoriteFbLogin && newsFragment != null) {
            lphApplication.isFromFavoriteFbLogin = false
            newsFragment!!.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE)
    }

    private class UpdateDeviceTokenAsync internal constructor(context: MainActivity, val deviceToken: String) : AsyncTask<Void, Void, Response<Any>>() {

        private val context: WeakReference<MainActivity> = WeakReference(context)

        override fun doInBackground(vararg p0: Void?): Response<Any> {
            var response = Response<Any>()
            try {
                val lphService = LPHServiceFactory.getCALFService(context.get()!!)
                val params = HashMap<String, String>()
                params[Constants.API_DEVICE_TOKEN] = deviceToken
                params[Constants.API_DEVICE_INFO] =  "android"
                response = lphService.updateDeviceToken(params)
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
                val cache = context.get()!!.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                val lphConstants = cache.edit()
                lphConstants.putString(Constants.SHARED_PREF_DEVICE_TOKEN, deviceToken)
                lphConstants.apply()
            }
        }
    }
}
