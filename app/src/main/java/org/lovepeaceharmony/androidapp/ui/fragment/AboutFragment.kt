package org.lovepeaceharmony.androidapp.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.adapters.PageAdapter
import org.lovepeaceharmony.androidapp.ui.activity.WebViewActivity
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the [AboutFragment.newInstance] factory method to
 * create an instance of this fragment.
 * Created by Naveen Kumar M on 10/11/17.
 */
class AboutFragment : Fragment() {

    private var currentTabState = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        if (savedInstanceState != null) {
            currentTabState = savedInstanceState.getInt(TAB_STATE, 0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_about, container, false)
        initView(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up Donate button
        view.findViewById<TextView>(R.id.tv_donate_now).setOnClickListener {
            launchWebView("https://www.lovepeaceharmony.org/donate")
        }

        // Set up Website button
        view.findViewById<TextView>(R.id.tv_view_website).setOnClickListener {
            launchWebView("https://www.lovepeaceharmony.org")
        }
    }

    private fun initView(view: View) {
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        tabLayout.addTab(tabLayout.newTab().setText(context!!.resources.getText(R.string.the_song)))
        tabLayout.addTab(tabLayout.newTab().setText(context!!.resources.getText(R.string.the_movement)))
        tabLayout.addTab(tabLayout.newTab().setText(context!!.resources.getText(R.string.master_sha)))

        val theSongFragment = TheSongFragment.newInstance()
        val movementFragment = MovementFragment.newInstance()
        val masterShaFragment = MasterShaFragment.newInstance()

        val tabList = ArrayList<Fragment>()
        tabList.add(theSongFragment)
        tabList.add(movementFragment)
        tabList.add(masterShaFragment)

        val viewPager = view.findViewById<ViewPager>(R.id.view_pager)
        val pageAdapter = PageAdapter(childFragmentManager, tabList)
        viewPager.offscreenPageLimit = 3
        viewPager.adapter = pageAdapter

        val tab = tabLayout.getTabAt(currentTabState)
        tab?.select()

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

    private fun launchWebView(url: String) {
        val intent = Intent(requireContext(), WebViewActivity::class.java).apply {
            putExtra("url", url)
        }
        startActivity(intent)
    }

    companion object {
        private val TAB_STATE = "tabState"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment AboutFragment.
         */
        fun newInstance(): AboutFragment {
            return AboutFragment()
        }
    }

}// Required empty public constructor
