package org.lovepeaceharmony.androidapp.ui.activity

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.utility.ConfirmationAlertCallback
import org.lovepeaceharmony.androidapp.utility.Constants
import org.lovepeaceharmony.androidapp.utility.Helper
import java.lang.ref.WeakReference

class CreateAccountActivity : AppCompatActivity() {
    private var context: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        setContentView(R.layout.activity_create_account)
        initView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initView() {
        val etName = findViewById<EditText>(R.id.editText_Name)
        val etEmail = findViewById<EditText>(R.id.editText_Email)
        val etPassword = findViewById<EditText>(R.id.editText_Password)
        val etConfirmPwd = findViewById<EditText>(R.id.editText_Confirm_Password)
        val tvCreateAccount = findViewById<TextView>(R.id.tv_create_account)
        val tvBack = findViewById<TextView>(R.id.tv_back)
        val bannerImage = findViewById<ImageView>(R.id.banner_image)

        val width = Helper.getDisplayWidth(this)
        bannerImage.layoutParams.height = (width / 2)
        bannerImage.layoutParams.width = width
        bannerImage.requestLayout()

        tvBack.setOnClickListener { onBackPressed() }

        etConfirmPwd.setOnEditorActionListener(TextView.OnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (Helper.isConnected(context!!)) {
                    createAccount(etName, etEmail, etPassword, etConfirmPwd)
                } else {

                    Helper.showConfirmationAlertTwoButton(context!!, context!!.getString(R.string.internet_warning), object : ConfirmationAlertCallback {
                        override fun onPositiveButtonClick() {
                            textView.onEditorAction(EditorInfo.IME_ACTION_DONE)
                        }

                        override fun onNegativeButtonClick() {

                        }

                        override fun onNeutralButtonClick() {

                        }
                    })

                }
                Helper.hideSoftKeyboard(context, etEmail)
                return@OnEditorActionListener true
            }
            false
        })

        tvCreateAccount.setOnClickListener { view ->
            if (Helper.isConnected(context!!)) {
                createAccount(etName, etEmail, etPassword, etConfirmPwd)
            } else {

                Helper.showConfirmationAlertTwoButton(context!!, context!!.getString(R.string.internet_warning), object : ConfirmationAlertCallback {
                    override fun onPositiveButtonClick() {
                        view.performClick()
                    }

                    override fun onNegativeButtonClick() {

                    }

                    override fun onNeutralButtonClick() {

                    }
                })

            }
            Helper.hideSoftKeyboard(context, etEmail)
        }

    }

    private fun createAccount(etName: EditText, etEmail: EditText, etPassword: EditText, etConfirmPwd: EditText) {
        val name = etName.text.toString().trim { it <= ' ' }
        val email = etEmail.text.toString().trim { it <= ' ' }
        val password = etPassword.text.toString().trim { it <= ' ' }
        val confirmPwd = etConfirmPwd.text.toString().trim { it <= ' ' }

        when {
            name.isEmpty() -> {
                Helper.showAlert(context!!, context!!.getString(R.string.please_enter_name))
                etName.requestFocus()
            }
            email.isEmpty() -> {
                Helper.showAlert(context!!, context!!.getString(R.string.please_enter_email))
                etEmail.requestFocus()
            }
            Helper.isNotValidEmail(email) -> {
                Helper.showAlert(context!!, context!!.getString(R.string.email_error))
                etEmail.requestFocus()
            }
            password.isEmpty() -> {
                Helper.showAlert(context!!, context!!.getString(R.string.please_enter_password))
                etPassword.requestFocus()
            }
            confirmPwd.isEmpty() -> {
                Helper.showAlert(context!!, context!!.getString(R.string.please_enter_confirm_password))
                etConfirmPwd.requestFocus()
            }
            password != confirmPwd -> {
                Helper.showAlert(context!!, context!!.getString(R.string.password_match_error))
                etConfirmPwd.requestFocus()
            }
            else -> {
                val weakReferenceContext: WeakReference<Context> = WeakReference(context!!)
                val registerAsync = Helper.RegisterAsync(weakReferenceContext, email, password, "", name, false, Constants.LoginType.Email)
                registerAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
        }
    }
}
