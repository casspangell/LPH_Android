package org.lovepeaceharmony.androidapp.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.androidquery.AQuery
import org.json.JSONException
import org.json.JSONObject
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.utility.*
import org.lovepeaceharmony.androidapp.utility.http.LPHException
import org.lovepeaceharmony.androidapp.utility.http.LPHServiceFactory
import org.lovepeaceharmony.androidapp.utility.http.Response
import java.io.IOException
import java.lang.ref.WeakReference


/**
 * ProfilePicUploadActivity
 * Created by Naveen Kumar M on 21/12/17.
 */

class ProfilePicUploadActivity : AppCompatActivity(), ImageUtils.ImageAttachmentListener {
    private var imageUtils: ImageUtils? = null
    private var context: Context? = null
    private var bitmap: Bitmap? = null
    private var fileName: String? = null
    private var ivImage: ImageView? = null
    private var ivDefault: RoundedImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        setContentView(R.layout.activity_profile_pic_upload)
        imageUtils = ImageUtils(this)
        initView()
    }

    private fun initView() {
        val aQuery = AQuery(context)
        ivImage = findViewById(R.id.iv_image)
        ivDefault = findViewById(R.id.iv_default_image)
        val tvBack = findViewById<TextView>(R.id.tv_back)
        val takeAPhoto = findViewById<View>(R.id.layout_take_photo)
        val chooseAPhoto = findViewById<View>(R.id.layout_choose_photo)

        val profilePicUrl = Helper.getStringFromPreference(context!!, Constants.SHARED_PREF_PROFILE_PIC_URL)
        if(profilePicUrl != null && !profilePicUrl.isEmpty()) {
            aQuery.id(ivImage)?.image(profilePicUrl, true, true, 144, 0)
            ivDefault?.visibility = View.GONE
        }

        tvBack.setOnClickListener { onBackPressed() }

        takeAPhoto.setOnClickListener {
            if (imageUtils!!.isDeviceSupportCamera) {
                imageUtils!!.launchCamera(1)
            } else {
                Toast.makeText(context, context!!.getString(R.string.camera_not_supported), Toast.LENGTH_SHORT).show()
            }
        }

        chooseAPhoto.setOnClickListener {
            imageUtils!!.launchGallery(1)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LPHLog.d("Activyt onActivityResult : " + requestCode + "resultCode: "+ resultCode)
        if(data != null)
            imageUtils!!.onActivityResult(requestCode, resultCode, data)
        else
            LPHLog.d("data is null")

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        imageUtils!!.requestPermissionResult(requestCode, permissions, grantResults)
    }

    override fun imageAttachment(from: Int, filename: String, file: Bitmap?, uri: Uri, encodedString: String) {
        this.bitmap = file
        this.fileName = filename
        ivDefault?.visibility = View.GONE
        ivImage?.setImageBitmap(file)
        LPHLog.d("File Name : " + filename)

        /*val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        if(file != null) {
            val base64String = ImageBase_64.encodeTobase64(file)
            if(base64String != null)s
                startAsync(base64String)
        }*/

//
//        val byteArrayOutputStream = ByteArrayOutputStream()
//        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
//        val byteArray = byteArrayOutputStream.toByteArray()
//
//        val encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP)
        LPHLog.d("Encoded: " + encodedString)
        startAsync(encodedString)
    }

    private fun startAsync(base64String: String) {
        if(Helper.isConnected(context!!)) {
            val profilePicUploadAsync = ProfilePicUploadAsync(this, base64String)
            profilePicUploadAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        } else {
            Helper.showConfirmationAlertTwoButton(context!!, context!!.getString(R.string.internet_warning), object : ConfirmationAlertCallback {
                override fun onPositiveButtonClick() {
                    startAsync(base64String)
                }

                override fun onNegativeButtonClick() {

                }

                override fun onNeutralButtonClick() {

                }
            })
        }
    }

    private class ProfilePicUploadAsync internal constructor(context: ProfilePicUploadActivity, private val base64String: String) : AsyncTask<Void, Void, Response<Any>>() {
        private var progress: MessageBox? = null
        private val context: WeakReference<ProfilePicUploadActivity> = WeakReference(context)

        override fun onPreExecute() {
            progress = MessageBox(context.get()!!, context.get()!!.resources.getString(R.string.processing_request), context.get()!!.resources.getString(R.string.please_wait))
            progress!!.showProgress()
            super.onPreExecute()
        }

        override fun doInBackground(vararg voids: Void): Response<Any> {
            var response = Response<Any>()
            try {
                val lphService = LPHServiceFactory.getCALFService(context.get()!!)
                val params = HashMap<String, String>()
                params[Constants.API_IMAGE] = base64String
                response = lphService.profilePicUpload(params)
            } catch (e: LPHException) {
                e.printStackTrace()
                response.setThrowable(e)
            } catch (e: JSONException) {
                e.printStackTrace()
                response.setThrowable(e)
            } catch (e: IOException) {
                e.printStackTrace()
                response.setThrowable(e)
            }

            return response
        }

        override fun onPostExecute(response: Response<Any>) {
            super.onPostExecute(response)
            if (progress != null) {
                progress!!.hideProgress()
                progress = null
            }
            if (response.isSuccess()) {

                val res: String = response.getResult() as String
                LPHLog.d("Response onPost 1: " + res)
                val jsonObj  = JSONObject(res)
                val dataObj = jsonObj.getJSONObject(Constants.PARSE_DATA)
                val userObj = dataObj.getJSONObject(Constants.PARSE_USER)
                val profilePic = userObj.optString(Constants.API_PROFILE_PIC_URL)
                Toast.makeText(context.get()!!, context.get()?.getString(R.string.uploaded_successfully), Toast.LENGTH_SHORT).show()
                val cache = context.get()!!.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                val lphConstants = cache.edit()
                lphConstants.putString(Constants.SHARED_PREF_PROFILE_PIC_URL, profilePic)
                lphConstants.apply()
                context.get()!!.ivImage!!.setImageBitmap(context.get()!!.bitmap)

            } else {
                MessageBox(context.get()!!, context.get()!!.resources.getString(R.string.alert), response.getServerMessage()!!).showMessage()
            }
        }
    }

}
