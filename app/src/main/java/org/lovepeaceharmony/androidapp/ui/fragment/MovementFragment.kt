package org.lovepeaceharmony.androidapp.ui.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
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
        val movementText = "<B>Love Peace Harmony Foundation</B> was founded in 2006 and formally registered in the United States as a nonprofit in 2008. The mission of Love Peace Harmony Foundation is to offer assistance and support to various local and global humanitarian efforts. We strive to initiate and support projects that create happy, healthy, and peaceful families and communities. On a philosophical level, the Foundation’s mission is to serve all humanity and to make others happier and healthier. The goal is to raise the consciousness of humanity by chanting the Divine Soul Song <i>Love, Peace and Harmony</i> to uplift the frequency and vibration of Mother Earth and to improve humanity’s health and happiness by teaching of self-healing techniques.<br><br>" +
                "Some of our numerous initiatives have included working with the Tarayana Foundation in Bhutan, a remote kingdom in the Himalayas. We are helping to support access to education, health care, and proper nutrition and creating a pilot project there that we hope will be replicated in many other areas of the world.<br><br>" +
                "In Toronto, Canada, we have supported low-income families so they could send their children to the Harbourfront Centre summer camp to experience the many benefits of enjoying a variety of activities with their peers and engaging actively with the environment.<br><br>" +
                "We have also contributed to the nonprofit WandAid to help families in earthquake damaged zones in Nepal. We are very honored to help support communities impacted by natural disasters, political crises, and other disadvantages.<br><br>" +
                "Love Peace Harmony Foundation has numerous additional ongoing projects and is actively expanding efforts worldwide to help people live happier and healthier lives.<br><br>" +
                "The Divine Soul Song <i>Love, Peace and Harmony</i> has been credited with many deeply transformative experiences by the people who have experienced it, lowering crime rates, incidences of physical abuse, and improving the health for women in low income homes in Mumbai, as well as increasing children’s intelligence and school performance. Chanting <i>Love, Peace and Harmony</i> for children in the slums of Mumbai (Dharavi, Asia’s largest slum) also resulted in greater emotional balance, happiness and other emotional benefits.<br>"
        tvContent.text = Helper.fromHtml(movementText)

        ivImage.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.the_movement_img))
    }

    companion object {


        fun newInstance(): MovementFragment {
            return MovementFragment()
        }
    }

}// Required empty public constructor
