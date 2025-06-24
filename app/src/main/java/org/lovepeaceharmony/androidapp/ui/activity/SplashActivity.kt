package org.lovepeaceharmony.androidapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.databinding.ActivitySplashBinding
import org.lovepeaceharmony.androidapp.ui.base.BaseActivity
import org.lovepeaceharmony.androidapp.viewmodel.AuthState
import org.lovepeaceharmony.androidapp.viewmodel.SplashViewModel

@AndroidEntryPoint
class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
    }

    private fun setupObservers() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> showLoading()
                is AuthState.LoggedIn -> navigateToMain()
                is AuthState.NotLoggedIn -> navigateToLogin()
                is AuthState.Error -> {
                    hideLoading()
                    showError(state.message)
                    navigateToLogin()
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
} 