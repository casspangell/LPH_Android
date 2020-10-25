package org.lovepeaceharmony.androidapp.utility

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Base64
import android.util.Log
import android.widget.Toast
import org.lovepeaceharmony.androidapp.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@SuppressLint("SdCardPath")
class ImageUtils {


    internal var context: Context
    private var currentActivity: Activity? = null
    private var currentFragment: Fragment? = null

    private var imageAttachmentCallBack: ImageAttachmentListener? = null

    private var selectedPath: String? = ""
    private var imageUri: Uri? = null
    private var path: File? = null

    private var from = 0
    private var isFragment = false


    /**
     * Check Camera Availability
     *
     * @return
     */

    // this device has a camera
    // no camera on this device
    val isDeviceSupportCamera: Boolean
        get() = this.context.packageManager.hasSystemFeature(
                PackageManager.FEATURE_CAMERA)

    constructor(act: Activity) {

        this.context = act
        this.currentActivity = act
        imageAttachmentCallBack = context as ImageAttachmentListener
    }

    constructor(act: Activity, fragment: Fragment, isFragment: Boolean) {

        this.context = act
        this.currentActivity = act
        imageAttachmentCallBack = fragment as ImageAttachmentListener
        if (isFragment) {
            this.isFragment = true
            currentFragment = fragment
        }

    }

    /**
     * Get file name from path
     *
     * @param path
     * @return
     */

    fun getFileNameFromPath(path: String): String {
        return path.substring(path.lastIndexOf('/') + 1, path.length)

    }

    /**
     * Get Image URI from Bitmap
     *
     * @param context
     * @param photo
     * @return
     */

    fun getImageUri(context: Context, photo: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        photo.compress(Bitmap.CompressFormat.PNG, 80, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, photo, "Title", null)
        return Uri.parse(path)
    }

    /**
     * Get Path from Image URI
     *
     * @param uri
     * @return
     */

    private fun getPath(uri: Uri?): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = this.context.contentResolver.query(uri!!, projection, null, null, null)
        var columnIndex = 0
        return if (cursor != null) {
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            val path = cursor.getString(columnIndex)
            cursor.close()
            path
        } else
            uri.path ?: ""
    }

    /**
     * Bitmap from String
     *
     * @param encodedString
     * @return
     */
    fun stringToBitMap(encodedString: String): Bitmap? {
        return try {
            val encodeByte = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        } catch (e: Exception) {
            e.message
            null
        }

    }


    /**
     * Get String from Bitmap
     *
     * @param bitmap
     * @return
     */

    fun bitMapToString(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }


    /**
     * Compress Imgae
     *
     * @param imageUri
     * @param height
     * @param width
     * @return
     */


    private fun compressImage(imageUri: String, height: Float, width: Float): Bitmap? {

        val filePath = getRealPathFromURI(imageUri)
        var scaledBitmap: Bitmap? = null

        val options = BitmapFactory.Options()

        // by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
        // you try the use the bitmap here, you will get null.

        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(filePath, options)

        var actualHeight = options.outHeight
        var actualWidth = options.outWidth

        // max Height and width values of the compressed image is taken as 816x612

        var imgRatio = (actualWidth / actualHeight).toFloat()
        val maxRatio = width / height

        // width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > height || actualWidth > width) {
            when {
                imgRatio < maxRatio -> {
                    imgRatio = height / actualHeight
                    actualWidth = (imgRatio * actualWidth).toInt()
                    actualHeight = height.toInt()
                }
                imgRatio > maxRatio -> {
                    imgRatio = width / actualWidth
                    actualHeight = (imgRatio * actualHeight).toInt()
                    actualWidth = width.toInt()
                }
                else -> {
                    actualHeight = height.toInt()
                    actualWidth = width.toInt()

                }
            }
        }

        //  setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)

        //  inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false

        // this options allow android to claim the bitmap memory if it runs low on memory

        options.inPurgeable = true
        options.inInputShareable = true
        options.inTempStorage = ByteArray(16 * 1024)

        try {
            //  load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()

        }

        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }

        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f

        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)

        val canvas = Canvas(scaledBitmap!!)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(bmp, middleX - bmp.width / 2, middleY - bmp.height / 2, Paint(Paint.FILTER_BITMAP_FLAG))

        // check the rotation of the image and display it properly

        val exif: ExifInterface
        try {
            exif = ExifInterface(filePath)

            val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0)
            Log.d("EXIF", "Exif: " + orientation)
            val matrix = Matrix()
            when (orientation) {
                6 -> {
                    matrix.postRotate(90f)
                    Log.d("EXIF", "Exif: " + orientation)
                }
                3 -> {
                    matrix.postRotate(180f)
                    Log.d("EXIF", "Exif: " + orientation)
                }
                8 -> {
                    matrix.postRotate(270f)
                    Log.d("EXIF", "Exif: " + orientation)
                }
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix,
                    true)

            return scaledBitmap
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * Get RealPath from Content URI
     *
     * @param contentURI
     * @return
     */
    private fun getRealPathFromURI(contentURI: String): String {
        val contentUri = Uri.parse(contentURI)
        val cursor = context.contentResolver.query(contentUri, null, null, null, null)
        try {
            if (cursor == null) {
                return contentUri.path ?: ""
            } else {
                cursor.moveToFirst()
                val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
    }


    /**
     * ImageSize Calculation
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }

        return inSampleSize
    }

    /**
     * Launch Camera
     *
     * @param from
     */

    fun launchCamera(from: Int) {
        this.from = from

        if (Build.VERSION.SDK_INT >= 23) {
            if (isFragment)
                permissionCheckFragment(1)
            else
                permissionCheck(1)
        } else {
            cameraCall()
        }
    }

    /**
     * Launch Gallery
     *
     * @param from
     */

    fun launchGallery(from: Int) {

        this.from = from

        if (Build.VERSION.SDK_INT >= 23) {
            if (isFragment)
                permissionCheckFragment(2)
            else
                permissionCheck(2)
        } else {
            galleyCall()
        }
    }

    /**
     * Show AlertDialog with the following options
     *
     * Camera
     * Gallery
     *
     *
     */

    /*fun imagepicker(from: Int) {
        this.from = from

        val items: Array<CharSequence>

        if (isDeviceSupportCamera) {
            items = arrayOfNulls(2)
            items[0] = "Camera"
            items[1] = "Gallery"
        } else {
            items = arrayOfNulls(1)
            items[0] = "Gallery"
        }

        val alertdialog = android.app.AlertDialog.Builder(currentActivity)
        alertdialog.setTitle("Add Image")
        alertdialog.setItems(items) { dialog, item ->
            if (items[item] == "Camera") {
                launchCamera(from)
            } else if (items[item] == "Gallery") {
                launchGallery(from)
            }
        }
        alertdialog.show()
    }*/

    /**
     * Check permission
     *
     * @param code
     */

    private fun permissionCheck(code: Int) {
        val hasWriteContactsPermission = ContextCompat.checkSelfPermission(currentActivity!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)

        val cameraPermission = ContextCompat.checkSelfPermission(currentActivity!!,
                Manifest.permission.CAMERA)

        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED || cameraPermission != PackageManager.PERMISSION_GRANTED) {
            val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(currentActivity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(currentActivity!!, Manifest.permission.CAMERA)

            if (!showRationale) {

                showMessageOKCancel("For adding images , You need to provide permission to access your files",
                        DialogInterface.OnClickListener { dialog, which ->
                            ActivityCompat.requestPermissions(currentActivity!!,
                                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                                    code)
                        })
                return
            }
//            if (!ActivityCompat.shouldShowRequestPermissionRationale(currentActivity!!,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//
//                showMessageOKCancel("For adding images , You need to provide permission to access your files",
//                        DialogInterface.OnClickListener { dialog, which ->
//                            ActivityCompat.requestPermissions(currentActivity!!,
//                                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                                    code)
//                        })
//                return
//            }

            ActivityCompat.requestPermissions(currentActivity!!,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                    code)
            return
        }

        if (code == 1)
            cameraCall()
        else if (code == 2)
            galleyCall()
    }


    /**
     * Check permission
     *
     * @param code
     */

    private fun permissionCheckFragment(code: Int) {
        Log.d(TAG, "permissionCheckFragment: " + code)
        val hasWriteContactsPermission = ContextCompat.checkSelfPermission(currentActivity!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(currentActivity!!,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                showMessageOKCancel("For adding images , You need to provide permission to access your files",
                        DialogInterface.OnClickListener { dialog, which ->
                            currentFragment!!.requestPermissions(
                                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                    code)
                        })
                return
            }

            currentFragment!!.requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    code)
            return
        }

        if (code == 1)
            cameraCall()
        else if (code == 2)
            galleyCall()
    }


    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(currentActivity!!)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show()
    }


    /**
     * Capture image from camera
     */

    private fun cameraCall() {
        val values = ContentValues()
        imageUri = currentActivity!!.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent1 = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent1.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (isFragment)
            currentFragment!!.startActivityForResult(intent, 0)
        else {
            LPHLog.d("Coming to startActivity")
            currentActivity!!.startActivityForResult(intent, 0)
        }
    }

    /**
     * pick image from Gallery
     *
     */

    private fun galleyCall() {
        Log.d(TAG, "galleyCall: ")

        val intent2 = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent2.type = "image/*"

        if (isFragment)
            currentFragment!!.startActivityForResult(intent2, 1)
        else
            currentActivity!!.startActivityForResult(intent2, 1)

    }


    /**
     * Activity PermissionResult
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    fun requestPermissionResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraCall()
            } else {
                Toast.makeText(currentActivity, "Permission denied", Toast.LENGTH_LONG).show()
            }

            2 ->
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    galleyCall()
                } else {

                    Toast.makeText(currentActivity, "Permission denied", Toast.LENGTH_LONG).show()
                }
        }
    }


    /**
     * Intent ActivityResult
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        val fileName: String
        val bitmap: Bitmap?
        LPHLog.d("resultCode: " + resultCode)

        when (requestCode) {
            0 ->

                if (resultCode == Activity.RESULT_OK) {

                    Log.i("Camera Selected", "Photo")

                    try {
                        selectedPath = null
                        selectedPath = getPath(imageUri)
                        // Log.i("selected","path"+selectedPath);
                        fileName = selectedPath!!.substring(selectedPath!!.lastIndexOf("/") + 1)
                        // Log.i("file","name"+file_name);

                        val uploadedBitmap: Bitmap? = data.extras?.get("data") as? Bitmap

//                        val uploadedBitmap = BitmapFactory.decodeFile(selectedPath)
                        val byteArrayOutputStream = ByteArrayOutputStream()
                        uploadedBitmap?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                        val byteArray = byteArrayOutputStream.toByteArray()
                        val encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP)

//                        bitmap = compressImage(imageUri!!.toString(), 144f, 144f)
                        imageAttachmentCallBack!!.imageAttachment(from, fileName, uploadedBitmap, imageUri!!, encoded)
                    } catch (exception: OutOfMemoryError) {
                        exception.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                }
            1 -> if (resultCode == Activity.RESULT_OK) {
                Log.i("Gallery", "Photo")
                val selectedImage = data.data

                try {
                    selectedPath = null
                    selectedPath = getPath(selectedImage)
                    fileName = selectedPath!!.substring(selectedPath!!.lastIndexOf("/") + 1)

                    val uploadedBitmap = BitmapFactory.decodeFile(selectedPath)
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    uploadedBitmap?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                    val byteArray = byteArrayOutputStream.toByteArray()
                    val encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP)

//                    bitmap = compressImage(selectedImage!!.toString(), 144f, 144f)
                    LPHLog.d("selectedPath : " + selectedPath!!)
                    imageAttachmentCallBack!!.imageAttachment(from, fileName, uploadedBitmap, selectedImage!!, encoded)
                } catch (exception: OutOfMemoryError) {
                    exception.printStackTrace()
                    Toast.makeText(context, context.getString(R.string.upload_out_of_memory), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }


            }
        }


    }

    /**
     * Get image from Uri
     *
     * @param uri
     * @param height
     * @param width
     * @return
     */
    fun getImageFromUri(uri: Uri, height: Float, width: Float): Bitmap? {
        var bitmap: Bitmap? = null

        try {
            bitmap = compressImage(uri.toString(), height, width)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return bitmap
    }

    /**
     * Get filename from URI
     * @param uri
     * @return
     */
    fun getFileNameFromUri(uri: Uri): String? {
        var path: String? = null
        var fileName: String? = null

        try {

            path = getRealPathFromURI(uri.path ?: "")
            fileName = path.substring(path.lastIndexOf("/") + 1)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        return fileName

    }


    /**
     * Check Image Exist (or) Not
     *
     * @param file_name
     * @param file_path
     * @return
     */

    fun checkimage(file_name: String, file_path: String): Boolean {
        val flag: Boolean
        path = File(file_path)

        val file = File(path, file_name)
        flag = if (file.exists()) {
            Log.i("file", "exists")
            true
        } else {
            Log.i("file", "not exist")
            false
        }

        return flag
    }


    /**
     * Get Image from the given path
     *
     * @param file_name
     * @param file_path
     * @return
     */

    fun getImage(file_name: String, file_path: String): Bitmap? {

        path = File(file_path)
        val file = File(path, file_name)

        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        options.inSampleSize = 2
        options.inTempStorage = ByteArray(16 * 1024)

        return BitmapFactory.decodeFile(file.absolutePath, options)
    }

    /**
     * Create an image
     *
     * @param bitmap
     * @param file_name
     * @param filepath
     * @param file_replace
     */


    fun createImage(bitmap: Bitmap, file_name: String, filepath: String, file_replace: Boolean) {

        path = File(filepath)

        if (!path!!.exists()) {
            path!!.mkdirs()
        }

        var file = File(path, file_name)

        if (file.exists()) {
            if (file_replace) {
                file.delete()
                file = File(path, file_name)
                storeImage(file, bitmap)
                Log.i("file", "replaced")
            }
        } else {
            storeImage(file, bitmap)
        }

    }


    /**
     *
     *
     * @param file
     * @param bmp
     */
    private fun storeImage(file: File, bmp: Bitmap) {
        try {
            val out = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    // Image Attachment Callback

    interface ImageAttachmentListener {
        fun imageAttachment(from: Int, filename: String, file: Bitmap?, uri: Uri, encodedString: String)
    }


}
