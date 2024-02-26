package org.lovepeaceharmony.android.app.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.lovepeaceharmony.android.app.R
import org.lovepeaceharmony.android.app.adapters.PageAdapter
import org.lovepeaceharmony.android.app.databinding.FragmentAboutBinding

/**
 * A simple [Fragment] subclass.
 * Created by Naveen Kumar M on 10/11/17.
 */
@ExperimentalCoroutinesApi
class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentAboutBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            btnViewWebsite.setOnClickListener {
                startActivity(getActionView("https://lovepeaceharmony.org/"))
            }
            btnDonateNow.setOnClickListener {
                startActivity(getActionView("https://lovepeaceharmony.org/donate/"))
            }

            with(viewPager) {
                offscreenPageLimit = 3
                adapter = PageAdapter(
                    this@AboutFragment, arrayOf(
                        TheSongFragment.newInstance(),
                        MovementFragment.newInstance(),
                        MasterShaFragment.newInstance()
                    )
                )
            }

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = "OBJECT ${(position + 1)}"
                tab.text = when (position) {
                    0 -> resources.getText(R.string.the_song)
                    1 -> resources.getText(R.string.the_movement)
                    2 -> resources.getText(R.string.master_sha)
                    else -> throw IllegalArgumentException("Invalid Page")
                }
            }.attach()

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getActionView(uriString: String) = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(uriString)
    )

}
