package org.lovepeaceharmony.androidapp.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 * @author by Naveen Kumar on 10/11/17.
 */
class PageAdapter(fm: FragmentManager, private val tabsList: List<Fragment>) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return tabsList[position]
    }

    override fun getCount(): Int {
        return tabsList.size
    }
}
