package org.lovepeaceharmony.androidapp.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.adapters.PageAdapter
import org.lovepeaceharmony.androidapp.utility.Constants
import org.lovepeaceharmony.androidapp.utility.OnTabChange
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the [NewsFragment.newInstance] factory method to
 * create an instance of this fragment.
 * Created by Naveen Kumar M on 10/11/17.
 */

class NewsFragment : Fragment(), OnTabChange {

    private var tabLayout: TabLayout? = null
    private var currentTabState = 0
    private var viewPager: ViewPager? = null
    private var favoriteNewsFragment: FavoriteNewsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        if (arguments != null) {
            currentTabState = arguments!!.getInt(Constants.BUNDLE_TAB_INDEX, 0)
        }
        if (savedInstanceState != null) {
            currentTabState = savedInstanceState.getInt(TAB_STATE, 0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_news, container, false)
        initView(view)
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(TAB_STATE, tabLayout!!.selectedTabPosition)
    }

    private fun initView(view: View) {
        tabLayout = view.findViewById(R.id.tab_layout)
        tabLayout!!.addTab(tabLayout!!.newTab().setText(context!!.resources.getText(R.string.recent)))
        tabLayout!!.addTab(tabLayout!!.newTab().setText(context!!.resources.getText(R.string.categories)))
        tabLayout!!.addTab(tabLayout!!.newTab().setText(context!!.resources.getText(R.string.favorites)))

        val recentNewsFragment = RecentNewsFragment.newInstance()
        val categoriesFragment = CategoriesFragment.newInstance()
        favoriteNewsFragment = FavoriteNewsFragment.newInstance()
        favoriteNewsFragment!!.setOnTabChange(this)

        val tabList = ArrayList<Fragment>()
        tabList.add(recentNewsFragment)
        tabList.add(categoriesFragment)
        tabList.add(favoriteNewsFragment!!)

        viewPager = view.findViewById(R.id.view_pager)
        val pageAdapter = PageAdapter(childFragmentManager, tabList)
        viewPager!!.offscreenPageLimit = 3
        viewPager!!.adapter = pageAdapter

        val tab = tabLayout!!.getTabAt(currentTabState)
        tab?.select()

        viewPager!!.currentItem = currentTabState

        viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager!!.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }

        })
    }

    override fun onTabChange(index: Int) {
        viewPager!!.setCurrentItem(index, true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (favoriteNewsFragment != null)
            favoriteNewsFragment!!.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private val TAB_STATE = "tabState"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment NewsFragment.
         */
        fun newInstance(): NewsFragment {
            return NewsFragment()
        }
    }
}// Required empty public constructor
