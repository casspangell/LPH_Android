package org.lovepeaceharmony.android.app.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.lovepeaceharmony.android.app.R
import org.lovepeaceharmony.android.app.databinding.FragmentMovementBinding
import org.lovepeaceharmony.android.app.utility.Helper

/**
 * A simple [Fragment] subclass.
 * Created by Naveen Kumar M on 07/12/17.
 */
class MasterShaFragment : Fragment() {

    private var _binding: FragmentMovementBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentMovementBinding.inflate(
        inflater, container, false
    ).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivImage.setImageResource(R.drawable.master_sha_img)
        binding.tvContent.text = Helper.fromHtml(getString(R.string.about_master_sha_content))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        fun newInstance(): MasterShaFragment {
            return MasterShaFragment()

        }
    }

}
