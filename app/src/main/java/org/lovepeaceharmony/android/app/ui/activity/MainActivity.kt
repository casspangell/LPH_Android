package org.lovepeaceharmony.android.app.ui.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.getkeepsafe.taptargetview.TapTarget
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.lovepeaceharmony.android.app.R
import org.lovepeaceharmony.android.app.databinding.ActivityMainBinding
import org.lovepeaceharmony.android.app.ui.fragment.ChantFragment
import org.lovepeaceharmony.android.app.utility.Constants
import org.lovepeaceharmony.android.app.utility.Helper
import org.lovepeaceharmony.android.app.utility.LPHLog
import org.lovepeaceharmony.android.app.utility.init
import org.lovepeaceharmony.android.app.utility.setupWithNavController
import org.lovepeaceharmony.android.app.viewmodel.MainViewModel

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
    private var mainLayout: View? = null

    private var chantFragment: ChantFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (savedInstanceState == null) setupBottomNavigationBar()

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.isToolTipShown.collect { shown ->
                    if (!shown) {
                        toolTipReceiver = object : BroadcastReceiver() {
                            override fun onReceive(context: Context, intent: Intent) {
                                LPHLog.d("Tool Tip : Tool Tip Receiver MainActivity")
                                TapTarget.forView(
                                    binding.bottomNavigationMenu,
                                    this@MainActivity.getString(R.string.get_news_updates_tool_tip),
                                    this@MainActivity.getString(R.string.tap_anywhere_to_continue)
                                ).init(this@MainActivity, R.color.tool_tip_color3) {
                                    viewModel.setToolTipShown(true)
                                    checkPermission()
                                }
                            }
                        }
                        registerReceiver(
                            toolTipReceiver, IntentFilter(Constants.BROADCAST_MAIN_BOTTOM_LAYOUT)
                        )
                    }
                }
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Now that BottomNavigationBar has restored its instance state
        // and its selectedItemId, we can proceed with setting up the
        // BottomNavigationBar with Navigation
        setupBottomNavigationBar()
    }

    private fun checkPermission() {

        if (!Helper.checkExternalStoragePermission(this)) {
            requestStoragePermission(findViewById(R.id.lay_home_activity_parent))
        }
    }

    private var toolTipReceiver: BroadcastReceiver? = null

    private fun requestStoragePermission(mainLayout: View?) {
        this.mainLayout = mainLayout
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val snackBar = Snackbar.make(
                mainLayout!!,
                this.getString(R.string.enable_storage_permission),
                Snackbar.LENGTH_INDEFINITE
            )
            snackBar.setActionTextColor(ContextCompat.getColor(this, android.R.color.white))
            val snackBarView = snackBar.view
            snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.top_bar_orange))
            snackBar.setAction(this.getString(R.string.ok)) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    PERMISSIONS_STORAGE,
                    0
                )
            }
            snackBar.show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val verified = if (grantResults.isEmpty()) false
        else grantResults.none { it != PackageManager.PERMISSION_GRANTED }
        if (verified) {

            val snackBar = Snackbar.make(
                mainLayout!!,
                this.getString(R.string.permission_granted),
                Snackbar.LENGTH_SHORT
            )
            snackBar.setActionTextColor(ContextCompat.getColor(this, android.R.color.white))
            val snackBarView = snackBar.view
            snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.top_bar_orange))
            snackBar.show()

        } else {
            var showRationale = false
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
            if (!showRationale) {
                val snackBar = Snackbar.make(
                    mainLayout!!,
                    this.getString(R.string.enable_storage_permission),
                    Snackbar.LENGTH_SHORT
                )
                snackBar.setActionTextColor(ContextCompat.getColor(this, android.R.color.white))
                val snackBarView = snackBar.view
                snackBarView.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.top_bar_orange
                    )
                )
                snackBar.show()
            } else {
                requestStoragePermission(mainLayout)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        toolTipReceiver?.let { unregisterReceiver(it) }
        LPHLog.d("Main Activity On Destroy MainActivity")
        chantFragment?.unRegisterPlayer()
    }

    companion object {
        private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
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
                R.navigation.nav_news,
                R.navigation.nav_logout
            ),
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_fragment,
            intent = intent
        )
    }
}
