package org.lovepeaceharmony.androidapp.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.adapters.PageAdapter
import org.lovepeaceharmony.androidapp.databinding.FragmentChantBinding
import org.lovepeaceharmony.androidapp.repo.DataState
import org.lovepeaceharmony.androidapp.utility.Constants
import org.lovepeaceharmony.androidapp.utility.LPHLog
import org.lovepeaceharmony.androidapp.utility.init
import org.lovepeaceharmony.androidapp.viewmodel.MainViewModel


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

    private lateinit var binding: FragmentChantBinding
    private var currentTabState = 0
    private var milestonesFragment: MilestonesFragment? = null
    private var chantNowFragment: ChantNowFragment? = null
    private val viewModel by activityViewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        if (arguments != null) {
            currentTabState = requireArguments().getInt(Constants.BUNDLE_TAB_INDEX, 0)
            LPHLog.d("Coming to Bundle: $currentTabState")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChantBinding.bind(view)
        initView()
        
        // Check if this is first launch
        val sharedPrefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val hasSeenIntro = sharedPrefs.getBoolean("has_seen_intro", false)
        
        viewModel.userPreferences.observe(viewLifecycleOwner) { state ->
            if (state is DataState.Success && state.data.toolTipShown.not() && !hasSeenIntro) {
                TapTarget.forView(
                    binding.tabLayout,
                    resources.getString(R.string.chant_any_time),
                    resources.getString(R.string.tap_anywhere_to_continue)
                ).init(requireActivity(), R.color.tool_tip_color1) {
                    // Save that user has seen the intro
                    sharedPrefs.edit().putBoolean("has_seen_intro", true).apply()
                    context?.sendBroadcast(Intent(Constants.BROADCAST_CHANT_NOW_ADAPTER))
                }
            }
        }
        LPHLog.d("ChantFragment : InitView called  $currentTabState")
    }

    private fun initView() = with(binding) {
        tabLayout.addTab(tabLayout.newTab().setText(resources.getText(R.string.chant_now)))
        tabLayout.addTab(tabLayout.newTab().setText(resources.getText(R.string.reminders)))
        tabLayout.addTab(tabLayout.newTab().setText(resources.getText(R.string.milestones)))

        if (chantNowFragment == null)
            chantNowFragment = ChantNowFragment.newInstance()
        else
            LPHLog.d("Chant fragment is not null")

        val remindersFragment = RemindersFragment.newInstance()
        milestonesFragment = MilestonesFragment.newInstance()

        val tabList = ArrayList<Fragment>()
        tabList.add(chantNowFragment!!)
        tabList.add(remindersFragment)
        tabList.add(milestonesFragment!!)

        viewPager.offscreenPageLimit = 3
        viewPager.adapter = PageAdapter(childFragmentManager, tabList)

        LPHLog.d("currentTabState InitView ChantNowFragment : $currentTabState")

        if (currentTabState > 0) {
            viewPager.postDelayed({
                val tab = tabLayout.getTabAt(currentTabState)
                tab?.select()
                viewPager.currentItem = currentTabState
            }, 50)

        } else {
            viewPager.postDelayed({
                val tab = tabLayout.getTabAt(currentTabState)
                tab?.select()
                viewPager.currentItem = currentTabState
            }, 50)
        }


        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }

        })
    }

    fun unRegisterPlayer() {
        chantNowFragment?.unRegisterThread()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (milestonesFragment != null)
            milestonesFragment!!.onActivityResult(requestCode, resultCode, data)
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
