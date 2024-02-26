package org.lovepeaceharmony.android.app.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.lovepeaceharmony.android.app.R
import org.lovepeaceharmony.android.app.databinding.ActivitySplashBinding
import org.lovepeaceharmony.android.app.utility.Constants
import org.lovepeaceharmony.android.app.utility.Helper


/**
 * SplashActivity
 * Created by Naveen Kumar M on 06/12/17.
 */
@ExperimentalCoroutinesApi
class SplashActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySplashBinding.inflate(layoutInflater) }

    private var context: Context? = null
    var inviteCoupon: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        setContentView(binding.root)

        val rotate = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        rotate.duration = 1000
        rotate.repeatCount = Animation.INFINITE

        binding.ivHealingBall.startAnimation(rotate)

        val width = Helper.getDisplayWidth(this)
        with(binding.header.bannerImage) {
            layoutParams.height = width / 2
            layoutParams.width = width
            requestLayout()
        }

        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                if (pendingDynamicLinkData == null) return@addOnSuccessListener
                // Get deep link from result (may be null if no link is found)
                val deepLink = pendingDynamicLinkData.link
                //
                // If the user isn't signed in and the pending Dynamic Link is
                // an invitation, sign in the user anonymously, and record the
                // referrer's UID.
                //
                if (deepLink != null && deepLink.getBooleanQueryParameter("invitedby", false)) {
                    inviteCoupon = deepLink.getQueryParameter("invitedby")
                    val cache = this@SplashActivity.getSharedPreferences(
                        Constants.SHARED_PREF_NAME,
                        Context.MODE_PRIVATE
                    )
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
            }, 1000
        )

    }


    private fun startApp() {
        val settings = getSharedPreferences(
            Constants.SHARED_PREF_NAME,
            Context.MODE_PRIVATE
        ) // 0 - for private mode
        val isLoggedIn = settings.getBoolean(Constants.SHARED_PREF_IS_LOGIN, false)
        val loginType = Helper.getStringFromPreference(this, Constants.SHARED_PREF_LOGIN_TYPE)

        if (isLoggedIn && loginType == Constants.LoginType.WithoutEmail.name) {
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            intent.putExtra(Constants.BUNDLE_IS_FROM_PROFILE, false)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim);
        } else {
            val i = Intent(this@SplashActivity, LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
            finish()
            overridePendingTransition(R.anim.nav_default_enter_anim, R.anim.nav_default_exit_anim);
        }

    }
}
