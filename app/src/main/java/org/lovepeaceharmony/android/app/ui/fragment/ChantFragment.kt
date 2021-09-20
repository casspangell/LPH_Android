package org.lovepeaceharmony.android.app.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.getkeepsafe.taptargetview.TapTarget
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.lovepeaceharmony.android.app.R
import org.lovepeaceharmony.android.app.adapters.PageAdapter
import org.lovepeaceharmony.android.app.databinding.FragmentChantBinding
import org.lovepeaceharmony.android.app.utility.Constants
import org.lovepeaceharmony.android.app.utility.init
import org.lovepeaceharmony.android.app.viewmodel.MainViewModel


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the [ChantFragment.newInstance] factory method to
 * create an instance of this fragment.
 * Created by Naveen Kumar M on 09/11/17.
 */
@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ChantFragment : Fragment(R.layout.fragment_chant) {

    private var _binding: FragmentChantBinding? = null
    private val binding get() = _binding!!
    private val chantNowFragment by lazy { ChantNowFragment.newInstance() }
    private val milestonesFragment by lazy { MilestonesFragment.newInstance() }
    private val remindersFragment by lazy { RemindersFragment.newInstance() }
    private val viewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentChantBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.isToolTipShown.collect { shown ->
                    if (!shown) TapTarget.forView(
                        binding.tabLayout,
                        resources.getString(R.string.chant_any_time),
                        resources.getString(R.string.tap_anywhere_to_continue)
                    ).init(requireActivity(), R.color.tool_tip_color1) {
                        context?.sendBroadcast(Intent(Constants.BROADCAST_CHANT_NOW_ADAPTER))
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initView() = with(binding) {

        viewPager.offscreenPageLimit = 3
        viewPager.adapter = PageAdapter(
            this@ChantFragment,
            arrayOf(chantNowFragment, remindersFragment, milestonesFragment)
        )

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> resources.getText(R.string.chant_now)
                1 -> resources.getText(R.string.reminders)
                2 -> resources.getText(R.string.milestones)
                else -> throw IllegalArgumentException("Invalid Page")
            }
        }.attach()
    }

    fun unRegisterPlayer() {
        chantNowFragment.unRegisterThread()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        milestonesFragment.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private val TAB_STATE = "tabState"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment ChantFragment.
         */
        fun newInstance(): ChantFragment {
            return ChantFragment()
        }
    }
}// Required empty public constructor
