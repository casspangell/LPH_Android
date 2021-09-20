package org.lovepeaceharmony.android.app.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.lovepeaceharmony.android.app.R
import org.lovepeaceharmony.android.app.databinding.FragmentMilestonesBinding
import org.lovepeaceharmony.android.app.utility.DataState
import org.lovepeaceharmony.android.app.utility.ConfirmationAlertCallback
import org.lovepeaceharmony.android.app.utility.Helper
import org.lovepeaceharmony.android.app.viewmodel.MainViewModel
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

    private var _binding: FragmentMilestonesBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentMilestonesBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnEraseMilestones.setOnClickListener {
            Helper.showResetConfirmationAlert(
                it.context,
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

        viewModel.firebaseDbCleared.observe(viewLifecycleOwner) {
            if (it is DataState.Success) observeFirebase()
        }
    }

    override fun onResume() {
        super.onResume()
        observeFirebase()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeFirebase() {
        viewModel.chantData.observe(viewLifecycleOwner) { chantDataState ->
            if (chantDataState is DataState.Success) {
                binding.tvMinutes.text =
                    chantDataState.data.totalSecondsChanted.toDisplayTimeString()
                binding.tvCurrentStreakCount.text =
                    chantDataState.data.streakMilestone.current_streak.toString()
                binding.tvLongestStreakCount.text =
                    chantDataState.data.streakMilestone.longest_streak.toString()
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
            viewModel.clearMileStones()
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
