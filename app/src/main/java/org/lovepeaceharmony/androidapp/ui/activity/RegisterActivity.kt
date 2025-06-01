package org.lovepeaceharmony.androidapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.auth.AuthPrefs
import org.lovepeaceharmony.androidapp.databinding.ActivityRegisterBinding
import org.lovepeaceharmony.androidapp.ui.base.BaseActivity
import org.lovepeaceharmony.androidapp.utility.Constants
import org.lovepeaceharmony.androidapp.utility.Helper
import org.lovepeaceharmony.androidapp.utility.LPHLog


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
            val confirmPassword = confirmPasswordContainer.editText?.text?.toString()

            // Clear any previous error messages
            errorMessage.visibility = View.GONE
            errorMessage.text = ""

            when {
                email.isNullOrBlank() -> {
                    showError(getString(R.string.error_email_required))
                }
                !isValidEmail(email) -> {
                    showError(getString(R.string.error_invalid_email))
                }
                password.isNullOrBlank() -> {
                    showError(getString(R.string.error_password_required))
                }
                confirmPassword.isNullOrBlank() -> {
                    showError(getString(R.string.error_confirm_password_required))
                }
                password != confirmPassword -> {
                    showError(getString(R.string.error_passwords_dont_match))
                }
                else -> {
                    registerWithEmailAndPassword(email, password)
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showError(message: String) {
        binding.errorMessage.apply {
            text = message
            visibility = View.VISIBLE
        }
    }

    private fun registerWithEmailAndPassword(email: String, password: String) {
        binding.progressBar.isVisible = true
        firebaseAuth.createUserWithEmailAndPassword(
            email, password
        ).addOnCompleteListener(this@RegisterActivity) { task ->
            binding.progressBar.isVisible = false
            if (task.isSuccessful) {
                LPHLog.d("signInWithEmail:success")
                val user = firebaseAuth.currentUser
                updateUI(user)
            } else {
                LPHLog.d("signInWithEmail:failure ${task.exception}")
                val errorMessage = when {
                    task.exception?.message?.contains("email address is already in use") == true -> {
                        getString(R.string.error_email_already_in_use)
                    }
                    task.exception?.message?.contains("badly formatted") == true -> {
                        getString(R.string.error_invalid_email)
                    }
                    task.exception?.message?.contains("password is invalid") == true -> {
                        getString(R.string.error_invalid_password)
                    }
                    else -> {
                        getString(R.string.error_registration_failed)
                    }
                }
                showError(errorMessage)
                updateUI(null)
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        user?.let {
            // Save auth data for auto-login
            user.getIdToken(true).addOnSuccessListener { tokenResult ->
                AuthPrefs.saveAuthData(
                    context = this,
                    token = tokenResult.token ?: "",
                    userId = user.uid
                )
                
                // Initialize app data
                Helper.setPlayList(this, "songs", ".mp3")
                
                // Navigate to main activity
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(Constants.BUNDLE_IS_FROM_PROFILE, false)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}
