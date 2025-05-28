package org.lovepeaceharmony.androidapp.ui.activity

import android.app.Activity
import android.app.AlertDialog
import android.widget.Toast
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.core.view.isVisible
import androidx.appcompat.app.AppCompatActivity
// import com.facebook.CallbackManager
// import com.facebook.FacebookCallback
// import com.facebook.FacebookException
// import com.facebook.login.LoginResult
// import com.facebook.login.widget.LoginButton
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions
//import com.google.android.gms.common.api.ApiException
// import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.databinding.ActivityLoginBinding
import org.lovepeaceharmony.androidapp.ui.base.BaseActivity
import org.lovepeaceharmony.androidapp.utility.BetterActivityResult
import org.lovepeaceharmony.androidapp.utility.Helper
import org.lovepeaceharmony.androidapp.utility.LPHLog

/**
 * LoginActivity
 * Created by Naveen Kumar M on 06/12/17.
 */
@AndroidEntryPoint
class LoginActivity : BaseActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    // private val callbackManager by lazy { CallbackManager.Factory.create() }
//    private var isFromFb: Boolean = false
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    override fun onStart() {
        super.onStart()
        updateUI(firebaseAuth.currentUser)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // if (isFromFb) {
        //     callbackManager.onActivityResult(requestCode, resultCode, data)
        // }
    }

    private fun initView() = with(binding) {
        header.bannerImage.isVisible = true

        // btnFacebook.setOnClickListener { signInWithFacebook() }  // Temporarily disabled
//        btnGoogle.setOnClickListener { signInWithGoogle() }

        btnLogin.setOnClickListener {
            val email = emailContainer.editText?.text?.toString()
            val password = passwordContainer.editText?.text?.toString()
            if (!email.isNullOrBlank() && !password.isNullOrBlank())
                signInWithEmailAndPassword(email, password)
        }

        tvResetPassword.setOnClickListener {
            val email = emailContainer.editText?.text?.toString()?.trim {it <= ' '}
            if (email?.isEmpty() == true) {
                Toast.makeText(this@LoginActivity, getString(R.string.please_provide_valid_email), Toast.LENGTH_SHORT).show()
            }
            else {
                if (email != null) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    getString(R.string.email_sent),
                                    Toast.LENGTH_LONG
                                ).show()
                                finish()
                            }
                            else {
                                Toast.makeText(this@LoginActivity,task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
        }

        tvSignUp.setOnClickListener {
            this@LoginActivity.run {
                startActivity(Intent(this, RegisterActivity::class.java))
            }
        }
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(
            email, password
        ).addOnCompleteListener(this@LoginActivity) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                LPHLog.d("signInWithEmail:success")
                val user = firebaseAuth.currentUser
                updateUI(user)
            } else {
                // If sign in fails, display a message to the user.
                LPHLog.d("signInWithEmail:failure ${task.exception}")
                updateUI(null)
            }
        }
    }

    /*
    private fun signInWithFacebook() {
        with(binding.fbLoginButton) {
            setPermissions("public_profile", "email", "user_friends")
            if (Helper.isConnected(this@LoginActivity)) login()
            else Helper.showAlert(
                this@LoginActivity,
                this@LoginActivity.resources.getString(R.string.please_check_your_internet_connection)
            )
        }
    }

    fun firebaseAuthWithFacebook(loginResult: LoginResult) {
        LPHLog.d("Access Token : " + loginResult.accessToken.token)
        val credential = FacebookAuthProvider.getCredential(loginResult.accessToken.token)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                LPHLog.d("signInWithCredential:success")
                val user = firebaseAuth.currentUser
                updateUI(user)
            } else {
                // If sign in fails, display a message to the user.
                LPHLog.d("signInWithCredential:failure ${task.exception}")
                updateUI(null)
            }
        }
    }

    private fun LoginButton.login() {
        isFromFb = true

        performClick()

        isPressed = true

        invalidate()

        val fbCallback = object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                result?.let { firebaseAuthWithFacebook(it) }
            }

            override fun onCancel() {
                LPHLog.d("Facebook Login: ", "Coming to onCancel")
            }

            override fun onError(error: FacebookException?) {
                LPHLog.d("Facebook Login: ", "Coming to error")
                LPHLog.d("Facebook Login Error: ", error?.message ?: "Something went wrong.")
            }
        }

        registerCallback(callbackManager, fbCallback)

        isPressed = false

        invalidate()
    }
    */

//    private fun signInWithGoogle() {
//        if (Helper.isConnected(this@LoginActivity)) {
//            isFromFb = false
//            // Configure Google Sign In
//            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build()
//
//            val result = object : BetterActivityResult.OnActivityResult<ActivityResult> {
//                override fun onActivityResult(result: ActivityResult) {
//                    if (result.resultCode == Activity.RESULT_OK) {
//                        // There are no request codes
//                        val data = result.data
//                        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//                        try {
//                            // Google Sign In was successful, authenticate with Firebase
//                            val account = task.getResult(ApiException::class.java)
//                            LPHLog.d("firebaseAuthWithGoogle: ${account.id}")
//                            account.idToken?.let { firebaseAuthWithGoogle(it) }
//                        } catch (e: ApiException) {
//                            // Google Sign In failed, update UI appropriately
//                            LPHLog.d("Google sign in failed $e")
//                        }
//                    }
//                }
//            }
//            activityLauncher.launch(
//                GoogleSignIn.getClient(this@LoginActivity, gso).signInIntent, result
//            )
//        } else Helper.showAlert(
//            this@LoginActivity,
//            this@LoginActivity.resources.getString(R.string.please_check_your_internet_connection)
//        )
//    }

//    private fun firebaseAuthWithGoogle(idToken: String) {
//        val credential = GoogleAuthProvider.getCredential(idToken, null)
//        firebaseAuth.signInWithCredential(credential)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    // Sign in success, update UI with the signed-in user's information
//                    LPHLog.d("signInWithCredential:success")
//                    val user = firebaseAuth.currentUser
//                    updateUI(user)
//                } else {
//                    // If sign in fails, display a message to the user.
//                    LPHLog.d("signInWithCredential:failure ${task.exception}")
//                    updateUI(null)
//                }
//            }
//    }

    private fun updateUI(user: FirebaseUser?) {
        user?.let {
            Helper.setPlayList(this, "songs", ".mp3")
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } ?: Log.d("Firebase", "Failed")
    }

//    private fun showEmailSentDialog() {
//        AlertDialog.Builder(this)
//            .setTitle(getString(R.string.reset_password))
//            .setMessage(getString(R.string.an_email_to_reset_your_password_has_been_sent))
//            .setPositiveButton("OK") { dialog, _ ->
//                dialog.dismiss()
//            }
//            .show()
//    }
}
