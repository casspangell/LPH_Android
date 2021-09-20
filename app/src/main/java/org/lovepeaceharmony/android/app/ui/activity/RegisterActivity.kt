package org.lovepeaceharmony.android.app.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.lovepeaceharmony.android.app.R
import org.lovepeaceharmony.android.app.databinding.ActivityRegisterBinding
import org.lovepeaceharmony.android.app.ui.base.BaseActivity
import org.lovepeaceharmony.android.app.utility.Constants
import org.lovepeaceharmony.android.app.utility.Helper
import org.lovepeaceharmony.android.app.utility.LPHLog


/**
 * LoginActivity
 * Created by Naveen Kumar M on 06/12/17.
 */
class RegisterActivity : BaseActivity() {

    private val binding by lazy { ActivityRegisterBinding.inflate(layoutInflater) }
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

    private fun initView() = with(binding) {
        header.bannerImage.isVisible = true
        with(header) {
            toolbar.apply {
                isVisible = true
                setNavigationOnClickListener { onBackPressed() }
                title = resources.getString(R.string.create_an_account)
            }
        }

        btnLogin.setOnClickListener {
            val email = emailContainer.editText?.text?.toString()
            val password = passwordContainer.editText?.text?.toString()
            if (!email.isNullOrBlank() && !password.isNullOrBlank())
                registerWithEmailAndPassword(email, password)
        }

    }

    private fun registerWithEmailAndPassword(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(
            email, password
        ).addOnCompleteListener(this@RegisterActivity) { task ->
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

    private fun updateUI(user: FirebaseUser?) {
        user?.let {
            Helper.setPlayList(this, "songs", ".mp3")
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(Constants.BUNDLE_IS_FROM_PROFILE, false)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } ?: Log.d("Firebase", "Failed")
    }
}
