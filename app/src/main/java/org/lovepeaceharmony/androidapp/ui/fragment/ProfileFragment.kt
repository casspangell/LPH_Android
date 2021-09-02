package org.lovepeaceharmony.androidapp.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.databinding.FragmentProfileBinding
import org.lovepeaceharmony.androidapp.repo.DataState
import org.lovepeaceharmony.androidapp.ui.activity.LoginActivity
import org.lovepeaceharmony.androidapp.viewmodel.MainViewModel
import org.lovepeaceharmony.androidapp.utility.*

/**
 * A simple [Fragment] subclass.
 * Created by Naveen Kumar M on 10/11/17.
 */
@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view).apply {
            btnLogout.setOnClickListener { startLogout() }
        }
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment ProfileFragment.
         */
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }

    private fun startLogout() {
        if (Helper.isConnected(requireContext())) handleLogout()
        else Helper.showConfirmationAlertTwoButton(
            requireContext(),
            resources.getString(R.string.please_check_your_internet_connection),
            object : ConfirmationAlertCallback {
                override fun onPositiveButtonClick() {
                    startLogout()
                }

                override fun onNegativeButtonClick() {}

                override fun onNeutralButtonClick() {}
            }
        )
    }

    private fun handleLogout() {
        Helper.clearAllDbValues(requireContext())
        requireContext().getSharedPreferences(
            Constants.SHARED_PREF_NAME,
            Context.MODE_PRIVATE
        ).edit().clear().apply()

        Firebase.auth.signOut()

        val i = Intent(requireContext(), LoginActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        requireContext().startActivity(i)
    }
}
