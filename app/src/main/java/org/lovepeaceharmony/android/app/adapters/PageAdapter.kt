package org.lovepeaceharmony.android.app.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * @author by Naveen Kumar on 10/11/17.
 */
@ExperimentalCoroutinesApi
class PageAdapter(
    fragment: Fragment, private val tabList: Array<Fragment>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = tabList.size

    override fun createFragment(
        position: Int
    ) = tabList.getOrNull(position) ?: throw IllegalArgumentException("Invalid position")
}
