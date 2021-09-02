package org.lovepeaceharmony.androidapp.ui.fragment


import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.databinding.FragmentMilestonesBinding
import org.lovepeaceharmony.androidapp.repo.DataState
import org.lovepeaceharmony.androidapp.utility.ConfirmationAlertCallback
import org.lovepeaceharmony.androidapp.utility.Helper
import org.lovepeaceharmony.androidapp.utility.reset
import org.lovepeaceharmony.androidapp.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [MilestonesFragment.newInstance] factory method to
 * create an instance of this fragment.
 * Created by Naveen Kumar M on 10/11/17.
 */
@ExperimentalCoroutinesApi
class MilestonesFragment : Fragment(R.layout.fragment_milestones) {

    private lateinit var binding: FragmentMilestonesBinding
    private val viewModel by activityViewModels<MainViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMilestonesBinding.bind(view).apply {
            btnEraseMilestones.setOnClickListener {
                Helper.showResetConfirmationAlert(
                    root.context,
                    resources.getString(R.string.this_cannot_be_undone),
                    object : ConfirmationAlertCallback {
                        override fun onPositiveButtonClick() {
                            resetMilestones()
                        }

                        override fun onNegativeButtonClick() {

                        }

                        override fun onNeutralButtonClick() {

                        }
                    })
            }
        }



        viewModel.chantingStreak.observe(viewLifecycleOwner) {
            if (it is DataState.Success) {
                binding.tvLongestStreakCount.text = it.data?.longest_streak?.toString() ?: "0"
                binding.tvCurrentStreakCount.text = it.data?.current_streak?.toString() ?: "0"
            }
        }


        viewModel.chantingMilestone.observe(viewLifecycleOwner) { state ->
            if (state is DataState.Success) {
                val secs = state.data.sumOf { it.seconds }
                binding.tvMinutes.text = secs.toDisplayTimeString()
            }
        }
    }

    private fun Long.toDisplayTimeString(): String {
        val tz: TimeZone = TimeZone.getTimeZone("UTC")
        val df = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        df.timeZone = tz
        return df.format(Date(this * 1000)) ?: "0"
    }


    private fun resetMilestones() {
        if (Helper.isConnected(requireContext())) {
            Firebase.reset()
        } else {
            Helper.showConfirmationAlertTwoButton(
                requireContext(),
                resources.getString(R.string.please_check_your_internet_connection),
                object : ConfirmationAlertCallback {
                    override fun onPositiveButtonClick() {
                        resetMilestones()
                    }

                    override fun onNegativeButtonClick() {

                    }

                    override fun onNeutralButtonClick() {

                    }
                })
        }
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment MilestonesFragment.
         */
        fun newInstance(): MilestonesFragment {
            return MilestonesFragment()
        }
    }
}
