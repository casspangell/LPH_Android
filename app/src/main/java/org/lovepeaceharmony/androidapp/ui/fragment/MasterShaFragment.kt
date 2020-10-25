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
        val masterShaText = "<B>Dr. and Master Zhi Gang Sha</B> is a world-renowned healer, Tao Grandmaster, philanthropist, humanitarian, and creator of Tao Calligraphy. He is the founder of Soul Mind Body Medicine™ and an eleven-time <i>New York Times</i> bestselling author of 24 books. An M.D. in China and a doctor of traditional Chinese medicine in China and Canada, Master Sha is the founder of Tao Academy™ and Love Peace Harmony Foundation™, which is dedicated to helping families worldwide create happier and healthier lives. <br><br>" +
                "A grandmaster of many ancient disciplines, including tai chi, qigong, kung fu, feng shui, and the I <i>Ching</i>, Master Sha was named Qigong Master of the Year at the Fifth World Congress on Qigong. In 2006, he was honored with the prestigious Martin Luther King, Jr. Commemorative Commission Award for his humanitarian efforts, and in 2016 Master Sha received rare and prestigious appointments as Shu Fa Jia (National Chinese Calligrapher Master) and Yan Jiu Yuan (Honorable Researcher Professor), the highest titles a Chinese calligrapher can receive, by the State Ethnic of Academy of Painting in China. <br><br>" +
                "Master Sha was named Honorary Member of the Club of Budapest Foundation, an organization dedicated to resolving the social, political, economic, and ecological challenges of the twenty-first century. Officially launching in 2015, Master Sha has been invited to become one of the Founding Signatories of the Fuji Declaration, whose mission is to create lasting peace on Earth. Master Sha is a member of Rotary E-Club of World Peace and is a founding signatory of the Conscious Business Declaration, which aims to define a new standard for business in the twenty-first century that will increase economic prosperity while healing the environment and improving humanity’s well-being. In 2016, Master Sha received a Commendation from Los Angeles County for his humanitarian efforts both locally and worldwide, and a Commendation from the Hawaii Senate as a selfless humanitarian who celebrates the power of the human spirit as he travels the world tirelessly to create a Love Peace Harmony World Family. In 2017, he received the International Tara Award from Ven. Mae Chee Sansanee Sthirasuta of Sathira-Dhammasathan, for being a person who acts like a Boddhisattva, doing good for society. He continues to deliver free webcasts, teleconferences, and events that attract viewers and listeners from around the world.<br>"

        tvContent.text = Helper.fromHtml(masterShaText)

        ivImage.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.master_sha_img))
    }

    companion object {

        fun newInstance(): MasterShaFragment {
            return MasterShaFragment()

        }
    }

}// Required empty public constructor
