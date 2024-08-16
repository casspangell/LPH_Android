package org.lovepeaceharmony.androidapp.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.app.AlertDialog
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.FirebaseDatabase
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
            btnDelete.setOnClickListener { showDeleteConfirmationDialog() }
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

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_account))
            .setMessage(getString(R.string.are_you_sure_warning))
            .setPositiveButton(getString(R.string.yes)) { dialog, which ->
                deleteUserAccount()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun deleteUserAccount() {
        val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val database = FirebaseDatabase.getInstance()

        user?.let {
            val userId = it.uid

            // Try to delete user data from Realtime Database
            database.getReference("users").child(userId)
                .removeValue()
                .addOnCompleteListener { databaseTask ->
                    // Attempt to delete user account regardless of database operation result
                    it.delete().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.success),
                                Toast.LENGTH_SHORT
                            ).show()

                            // Redirect to LoginActivity after successful deletion
                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                requireContext(), "Fail 2",
                                //getString(R.string.failed_to_delete_account) + task.exception?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }.addOnFailureListener { databaseTask ->
                    // If user data does not exist or there's an issue, still proceed with account deletion
                    it.delete().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.success),
                                Toast.LENGTH_SHORT
                            ).show()

                            // Redirect to LoginActivity after successful deletion
                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                requireContext(), "Fail1",
                                //getString(R.string.failed_to_delete_user_data) + task.exception?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
        }
    }
}

