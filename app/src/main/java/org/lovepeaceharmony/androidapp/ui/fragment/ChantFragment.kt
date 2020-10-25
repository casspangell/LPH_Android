package org.lovepeaceharmony.androidapp.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.adapters.PageAdapter
import org.lovepeaceharmony.androidapp.utility.Constants
import org.lovepeaceharmony.androidapp.utility.Helper
import org.lovepeaceharmony.androidapp.utility.LPHLog
import java.util.*




/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the [ChantFragment.newInstance] factory method to
 * create an instance of this fragment.
 * Created by Naveen Kumar M on 09/11/17.
 */
class ChantFragment : Fragment() {

    private var tabLayout: TabLayout? = null
    private var currentTabState = 0
    private var milestonesFragment: MilestonesFragment? = null
    private var chantNowFragment: ChantNowFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        if (arguments != null) {
            currentTabState = arguments!!.getInt(Constants.BUNDLE_TAB_INDEX, 0)
            LPHLog.d("Coming to Bundle: " + currentTabState)
        }
        /*if (savedInstanceState != null) {
            currentTabState = savedInstanceState.getInt(TAB_STATE, 0)
        }*/
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chant, container, false)
        LPHLog.d("ChantFragment : InitView callled  + " + currentTabState)
        initView(view)
        return view
    }


    /*override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(TAB_STATE, tabLayout!!.selectedTabPosition)
    }*/

    private fun initView(view: View) {
        tabLayout = view.findViewById(R.id.tab_layout)
        tabLayout!!.addTab(tabLayout!!.newTab().setText(context!!.resources.getText(R.string.chant_now)))
        tabLayout!!.addTab(tabLayout!!.newTab().setText(context!!.resources.getText(R.string.reminders)))
        tabLayout!!.addTab(tabLayout!!.newTab().setText(context!!.resources.getText(R.string.milestones)))

        if(chantNowFragment == null)
            chantNowFragment = ChantNowFragment.newInstance()
        else
            LPHLog.d("Chant fragment is not null")

        val remindersFragment = RemindersFragment.newInstance()
        milestonesFragment = MilestonesFragment.newInstance()

        val tabList = ArrayList<Fragment>()
        tabList.add(chantNowFragment!!)
        tabList.add(remindersFragment)
        tabList.add(milestonesFragment!!)

        val viewPager = view.findViewById<ViewPager>(R.id.view_pager)
        val pageAdapter = PageAdapter(childFragmentManager, tabList)
        viewPager.offscreenPageLimit = 3
        viewPager.adapter = pageAdapter


        LPHLog.d("currentTabState InitView ChantNowFragment : " + currentTabState)


//        viewPager.currentItem = currentTabState

        if(currentTabState > 0) {
            viewPager.postDelayed({
                val tab = tabLayout!!.getTabAt(currentTabState)
                tab?.select()
                viewPager.currentItem = currentTabState
            }, 50)

        } else {
            viewPager.postDelayed({
                val tab = tabLayout!!.getTabAt(currentTabState)
                tab?.select()
                viewPager.currentItem = currentTabState
            }, 50)
        }


        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }

        })

        val isToolTipShown = Helper.getBooleanFromPreference(context!!, Constants.SHARED_PREF_IS_TOOL_TIP_SHOWN)
        LPHLog.d("isToolTipShown : " +isToolTipShown)
        if(!isToolTipShown){
            /*val sequence = TapTargetSequence(activity!!)
                    .targets(
                            // This tap target will target the back button, we just need to pass its containing toolbar
                            TapTarget.forView(tabLayout, this.getString(R.string.chant_any_time_tool_tip)).tintTarget(true).cancelable(false)
                                    .dimColor(android.R.color.white)
                                    .outerCircleColor(R.color.tool_tip_color1)
                                    .targetCircleColor(android.R.color.black)
                                    .transparentTarget(true)
                                    .textColor(android.R.color.white)
                                    .id(1)

                    )
                    .listener(object : TapTargetSequence.Listener {
                        // This listener will tell us when interesting(tm) events happen in regards
                        // to the sequence
                        override fun onSequenceFinish() {
                            LPHLog.d("Tool Tip : Sequence Finished ChantFragment")
                            val intent1 = Intent(Constants.BROADCAST_CHANT_NOW_ADAPTER)
                            context?.sendBroadcast(intent1)
                        }

                        override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {
                            Log.d("TapTargetView", "Clicked on " + lastTarget.id())
                        }

                        override fun onSequenceCanceled(lastTarget: TapTarget) {
                        }


                    })

            sequence.start()*/


            TapTargetView.showFor(activity!!, TapTarget.forView(tabLayout, this.getString(R.string.chant_any_time_tool_tip), this.getString(R.string.tap_here_to_continue))
                    .cancelable(false)
//                    .drawShadow(true)
                    .dimColor(android.R.color.white)
                    .outerCircleColor(R.color.tool_tip_color1)
                    .targetCircleColor(android.R.color.black)
                    .transparentTarget(true)
                    .textColor(android.R.color.white)
                    .titleTextSize(18)
                    .descriptionTextSize(14)
                    .textTypeface(ResourcesCompat.getFont(activity!!, R.font.open_sans_semi_bold))
                    .descriptionTypeface(ResourcesCompat.getFont(activity!!, R.font.open_sans_regular))
                    .id(1)
                    .tintTarget(false), object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView) {
                    super.onTargetClick(view)
                    // .. which evidently starts the sequence we defined earlier
                    LPHLog.d("Tool Tip : Sequence Finished ChantFragment")
                    val intent1 = Intent(Constants.BROADCAST_CHANT_NOW_ADAPTER)
                    context?.sendBroadcast(intent1)
                }

                override fun onOuterCircleClick(view: TapTargetView?) {
                    super.onOuterCircleClick(view)
                    view?.dismiss(true)
                    LPHLog.d("Tool Tip : Sequence Finished ChantFragment")
                    val intent1 = Intent(Constants.BROADCAST_CHANT_NOW_ADAPTER)
                    context?.sendBroadcast(intent1)
                }

                override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                    Log.d("TapTargetViewSample", "You dismissed me :(")
                }
            })
        }
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
