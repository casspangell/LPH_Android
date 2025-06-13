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
class MovementFragment : Fragment() {



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_movement, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        val tvContent = view.findViewById<TextView>(R.id.tv_content)
        val ivImage = view.findViewById<ImageView>(R.id.iv_image)
        tvContent.text = Helper.fromHtml(context!!.getString(R.string.movement_content))
        ivImage.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.the_movement_img))
    }

    companion object {


        fun newInstance(): MovementFragment {
            return MovementFragment()
        }
    }

}// Required empty public constructor
