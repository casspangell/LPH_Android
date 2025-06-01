package org.lovepeaceharmony.androidapp.ui.fragment


import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.utility.Helper

/**
 * A simple [Fragment] subclass.
 * Created by Naveen Kumar M on 07/12/17.
 */
class MasterShaFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_movement, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        val tvContent = view.findViewById<TextView>(R.id.tv_content)
        val ivImage = view.findViewById<ImageView>(R.id.iv_image)
        tvContent.text = Helper.fromHtml(context!!.getString(R.string.master_sha_content))
        ivImage.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.master_sha_img))
    }

    companion object {

        fun newInstance(): MasterShaFragment {
            return MasterShaFragment()

        }
    }

}// Required empty public constructor
