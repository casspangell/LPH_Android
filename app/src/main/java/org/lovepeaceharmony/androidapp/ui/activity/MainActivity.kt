package org.lovepeaceharmony.androidapp.ui.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.getkeepsafe.taptargetview.TapTarget
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.json.JSONException
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.databinding.ActivityMainBinding
import org.lovepeaceharmony.androidapp.repo.DataState
import org.lovepeaceharmony.androidapp.ui.fragment.ChantFragment
import org.lovepeaceharmony.androidapp.utility.*
import org.lovepeaceharmony.androidapp.utility.http.LPHException
import org.lovepeaceharmony.androidapp.utility.http.LPHServiceFactory
import org.lovepeaceharmony.androidapp.utility.http.Response
import org.lovepeaceharmony.androidapp.viewmodel.MainViewModel
import java.io.IOException
import java.lang.ref.WeakReference

/**
 * MainActivity
 * Created by Naveen Kumar M on 09/11/17.
 */
@AndroidEntryPoint
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<MainViewModel>()
    private var currentNavController: LiveData<NavController>? = null
    private var isFromProfile: Boolean = false
    private var mainLayout: View? = null

    private var chantFragment: ChantFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.extras != null)
            isFromProfile = intent.getBooleanExtra(Constants.BUNDLE_IS_FROM_PROFILE, false)
        setContentView(binding.root)
        initView()
        if (savedInstanceState == null) setupBottomNavigationBar()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
            if (tokenTask.isSuccessful.not()) return@addOnCompleteListener
            val fcmToken = tokenTask.result
            val deviceToken =
                Helper.getStringFromPreference(this, Constants.SHARED_PREF_DEVICE_TOKEN)
            val token = Helper.getStringFromPreference(this, Constants.SHARED_PREF_TOKEN)
            if (deviceToken.isEmpty() && token.isNotEmpty() && Helper.isConnected(context = this@MainActivity)) {
                LPHLog.d("FCM TOKEN : $fcmToken")
                val updateDeviceTokenAsync = UpdateDeviceTokenAsync(this, fcmToken)
                updateDeviceTokenAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
        }

        viewModel.userPreferences.observe(this) { state ->
            if (state is DataState.Success) registerReceiver(
                toolTipReceiver, 
                IntentFilter(Constants.BROADCAST_MAIN_BOTTOM_LAYOUT),
                Context.RECEIVER_NOT_EXPORTED
            )
        }
        registerReceiver(
            clearService, 
            IntentFilter(Constants.BROADCAST_CLEAR_THREAD),
            Context.RECEIVER_NOT_EXPORTED
        )
        startService(Intent(this, ThreadClearService::class.java))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Now that BottomNavigationBar has restored its instance state
        // and its selectedItemId, we can proceed with setting up the
        // BottomNavigationBar with Navigation
        setupBottomNavigationBar()
    }

    private fun initView() = with(binding) {
        binding.bottomNavigationMenu.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chant -> {
                    true
                }
                R.id.nav_about -> {
                    true
                }
                R.id.nav_logout -> {
                    true
                }
                else -> false
            }
        }
    }

    private val toolTipReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            LPHLog.d("Tool Tip : Tool Tip Receiver MainActivity")

            if (!viewModel.isToolTipShown) TapTarget.forView(
                binding.bottomNavigationMenu,
                this@MainActivity.getString(R.string.get_news_updates_tool_tip),
                this@MainActivity.getString(R.string.tap_anywhere_to_continue)
            ).init(this@MainActivity, R.color.tool_tip_color3) {
                viewModel.isToolTipShown = true
            }
        }
    }

    private val clearService = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            LPHLog.d("Thread clear Broadcast")
//            chantFragment?.unRegisterPlayer()

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(toolTipReceiver)
        LPHLog.d("Main Activity On Destroy MainActivity")
        chantFragment?.unRegisterPlayer()
        unregisterReceiver(clearService)
    }

    companion object {
        private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private class UpdateDeviceTokenAsync(
        context: MainActivity,
        val deviceToken: String
    ) : AsyncTask<Void, Void, Response<Any>>() {

        private val context: WeakReference<MainActivity> = WeakReference(context)

        override fun doInBackground(vararg p0: Void?): Response<Any> {
            var response = Response<Any>()
            try {
                val lphService = LPHServiceFactory.getCALFService(context.get()!!)
                val params = HashMap<String, String>()
                params[Constants.API_DEVICE_TOKEN] = deviceToken
                params[Constants.API_DEVICE_INFO] = "android"
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
                val cache = context.get()!!
                    .getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                val lphConstants = cache.edit()
                lphConstants.putString(Constants.SHARED_PREF_DEVICE_TOKEN, deviceToken)
                lphConstants.apply()
            }
        }
    }

    /**
     * Called on first creation and when restoring state.
     */
    private fun setupBottomNavigationBar() {
        // Setup the bottom navigation view with a list of navigation graphs
        currentNavController = binding.bottomNavigationMenu.setupWithNavController(
            navGraphIds = listOf(
                R.navigation.nav_chant,
                R.navigation.nav_about,
                R.navigation.nav_logout
            ),
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_fragment,
            intent = intent
        )
    }
}
