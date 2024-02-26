package org.lovepeaceharmony.android.app.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.lovepeaceharmony.android.app.R
import org.lovepeaceharmony.android.app.databinding.FragmentProfileBinding
import org.lovepeaceharmony.android.app.ui.activity.LoginActivity
import org.lovepeaceharmony.android.app.ui.base.BaseFragment
import org.lovepeaceharmony.android.app.utility.ConfirmationAlertCallback
import org.lovepeaceharmony.android.app.utility.Constants
import org.lovepeaceharmony.android.app.utility.Helper
import org.lovepeaceharmony.android.app.utility.alert

/**
 * A simple [Fragment] subclass.
 * Created by Naveen Kumar M on 10/11/17.
 */
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ProfileFragment : BaseFragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentProfileBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLogout.setOnClickListener { startLogout() }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun startLogout() {
        if (networkConnected) handleLogout()
        else alert(R.string.please_check_your_internet_connection) {
            positiveButton(R.string.ok) { startLogout() }
        }.show()
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
