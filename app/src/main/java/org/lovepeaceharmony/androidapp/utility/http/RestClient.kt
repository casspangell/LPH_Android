package org.lovepeaceharmony.androidapp.utility.http

import android.content.Context
import android.net.ParseException
import org.lovepeaceharmony.androidapp.R
import org.lovepeaceharmony.androidapp.utility.ConfirmationAlertCallback
import org.lovepeaceharmony.androidapp.utility.Helper
import org.lovepeaceharmony.androidapp.utility.LPHLog
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*
import javax.net.ssl.HttpsURLConnection

/**
 * RestClient
 * Created by Naveen Kumar M on 02/01/18.
 */
class RestClient {

    enum class HTTPMethod {
        GET, POST, DELETE
    }

    companion object {
        private val CONNECTION_TIMEOUT = 1000 * 25

        private val CALF_ENCODING_FORMAT = "UTF-8"

        @Throws(IOException::class)
        fun httpRequest(context: Context, isPagination: Boolean, strUrl: String, postDataParams: HashMap<String, String>?, httpMethod: HTTPMethod, authToken: String): String {
            val response = StringBuilder()
            var strUrl1 = strUrl
            if (Helper.isConnected(context)) {
                LPHLog.d("Http request initiated: " + strUrl)
                if (null != postDataParams) {
                    LPHLog.d("Http request params :" + getPostDataString(postDataParams))
                }

                if (httpMethod == HTTPMethod.GET && null != postDataParams) {
                    strUrl1 = strUrl + "?" + getPostDataString(postDataParams)
                    LPHLog.d("Http Full URL : " + strUrl1)
                }
                val url = URL(strUrl1)
                val conn = url.openConnection() as HttpURLConnection
                conn.readTimeout = CONNECTION_TIMEOUT
                conn.connectTimeout = CONNECTION_TIMEOUT
                if (httpMethod == HTTPMethod.GET) {
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.setRequestProperty("charset", "utf-8")
                } else if (httpMethod == HTTPMethod.POST) {
                    conn.requestMethod = "POST"
                } else if (httpMethod == HTTPMethod.DELETE){
                    conn.requestMethod = "DELETE"
                }
                if(!authToken.isEmpty())
                    conn.setRequestProperty("Authorization", "Bearer " + authToken)
//                conn.doInput = true
//                conn.doOutput = true
                if (httpMethod == HTTPMethod.POST) {
                    conn.doInput = true
                    conn.doOutput = true
                    val os = conn.outputStream
                    val writer = BufferedWriter(
                            OutputStreamWriter(os, CALF_ENCODING_FORMAT))
                    if (postDataParams != null)
                        writer.write(getPostDataString(postDataParams))

                    writer.flush()
                    writer.close()
                    os.close()
                }

                val responseCode = conn.responseCode
                LPHLog.d("ResponseCode: " + responseCode)

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    var line: String? = null
                    val br = BufferedReader(InputStreamReader(conn.inputStream))
                    while ({ line = br.readLine(); line }() != null) {
                        response.append(line)
                    }
                } else if (conn.responseCode == HttpsURLConnection.HTTP_MOVED_PERM) {
                    response.append(conn.getHeaderField("Location"))
                }
                LPHLog.d("Response: " + response.toString())
                return response.toString()
            } else if (!isPagination) {
                Helper.showConfirmationAlertTwoButton(context, context.getString(R.string.internet_warning), object : ConfirmationAlertCallback {
                    override fun onPositiveButtonClick() {
                        try {
                            httpRequest(context, isPagination, strUrl, postDataParams, httpMethod, authToken)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onNegativeButtonClick() {

                    }

                    override fun onNeutralButtonClick() {

                    }
                })

            }

            return response.toString()
        }


        @Throws(UnsupportedEncodingException::class)
        private fun getPostDataString(params: HashMap<String, String>?): String {
            val result = StringBuilder()
            if (params == null) {
                return ""
            }
            var first = true
            var value: String?
            for ((key, value1) in params) {
                if (first)
                    first = false
                else
                    result.append("&")

                result.append(URLEncoder.encode(key, CALF_ENCODING_FORMAT))
                result.append("=")
                value = value1
                if (value == null) {
                    value = ""
                }
                result.append(URLEncoder.encode(value, CALF_ENCODING_FORMAT))
            }
            return result.toString()
        }

        @Throws(ParseException::class, IOException::class)
        fun httpFileUpload(urlTo: String, params: HashMap<String, String>, filepath: String): String {
            val connection: HttpURLConnection
            val outputStream: DataOutputStream
            val inputStream: InputStream

            val twoHyphens = "--"
            val boundary = "*****" + java.lang.Long.toString(System.currentTimeMillis()) + "*****"
            val lineEnd = "\r\n"

            val result: String

            var bytesRead: Int
            var bytesAvailable: Int
            var bufferSize: Int
            val buffer: ByteArray
            val maxBufferSize = 1024 * 1024


            val url = URL(urlTo)
            connection = url.openConnection() as HttpURLConnection

            connection.doInput = true
            connection.doOutput = true
            connection.useCaches = false

            connection.requestMethod = "POST"
            connection.setRequestProperty("Connection", "Keep-Alive")
            connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0")
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary)


            for ((key, value) in params) {
                LPHLog.d(key + ":" + value)
                connection.setRequestProperty(key, value)
            }


            outputStream = DataOutputStream(connection.outputStream)

            //        for (String filepath : filepathList) {

            val q = filepath.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val idx = q.size - 1
            outputStream.writeBytes(twoHyphens + boundary + lineEnd)
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + filepath + "\"; filename=\"" + q[idx] + "\"" + lineEnd)
            outputStream.writeBytes("Content-Type: image/jpeg" + lineEnd)
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd)
            outputStream.writeBytes(lineEnd)

            val file = File(filepath)
            val fileInputStream = FileInputStream(file)
            bytesAvailable = fileInputStream.available()
            bufferSize = Math.min(bytesAvailable, maxBufferSize)
            buffer = ByteArray(bufferSize)
            bytesRead = fileInputStream.read(buffer, 0, bufferSize)
            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize)
                bytesAvailable = fileInputStream.available()
                bufferSize = Math.min(bytesAvailable, maxBufferSize)
                bytesRead = fileInputStream.read(buffer, 0, bufferSize)
            }

            outputStream.writeBytes(lineEnd)
            fileInputStream.close()

            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)

            inputStream = connection.inputStream
            result = convertStreamToString(inputStream)

            inputStream.close()
            outputStream.flush()
            outputStream.close()

            LPHLog.d("response:" + result)
            return result

        }

        @Throws(ParseException::class, IOException::class)
        fun httpFileUpload(urlTo: String, params: HashMap<String, String>, filepathList: List<String>, fileField: String): String {
            val connection: HttpURLConnection
            val outputStream: DataOutputStream
            val inputStream: InputStream

            val twoHyphens = "--"
            val boundary = "*****" + java.lang.Long.toString(System.currentTimeMillis()) + "*****"
            val lineEnd = "\r\n"

            val result: String

            var bytesRead: Int
            var bytesAvailable: Int
            var bufferSize: Int
            var buffer: ByteArray
            val maxBufferSize = 1024 * 1024


            val url = URL(urlTo)
            connection = url.openConnection() as HttpURLConnection

            connection.doInput = true
            connection.doOutput = true
            connection.useCaches = false

            connection.requestMethod = "POST"
            connection.setRequestProperty("Connection", "Keep-Alive")
            connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0")
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary)


            for ((key, value) in params) {
                connection.setRequestProperty(key, value)
            }


            outputStream = DataOutputStream(connection.outputStream)

            for ((i, filepath) in filepathList.withIndex()) {
                LPHLog.d("filePath upload: " + filepath)
                val q = filepath.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val idx = q.size - 1
                outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + fileField + "[" + i + "]" + "\"; filename=\"" + q[idx] + "\"" + lineEnd)
                outputStream.writeBytes("Content-Type: image/jpeg" + lineEnd)
                outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd)
                outputStream.writeBytes(lineEnd)

                val file = File(filepath)
                val fileInputStream = FileInputStream(file)
                bytesAvailable = fileInputStream.available()
                bufferSize = Math.min(bytesAvailable, maxBufferSize)
                buffer = ByteArray(bufferSize)
                bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize)
                    bytesAvailable = fileInputStream.available()
                    bufferSize = Math.min(bytesAvailable, maxBufferSize)
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                }

                outputStream.writeBytes(lineEnd)
                fileInputStream.close()
            }


            // Upload POST Data
            /*String post = getPostDataString(params);
        String[] posts = post.split("&");
        int max = posts.length;
        for (int i = 0; i < max; i++) {
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            String[] kv = posts[i].split("=");
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + kv[0] + "\"" + lineEnd);
            outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(kv[1]);
            outputStream.writeBytes(lineEnd);
        }*/

            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)

            inputStream = connection.inputStream
            result = convertStreamToString(inputStream)

            inputStream.close()
            outputStream.flush()
            outputStream.close()

            LPHLog.d("response:" + result)
            return result

        }

        @Throws(ParseException::class, IOException::class)
        fun httpFileUploadMultipleArray(urlTo: String, params: HashMap<String, String>, uploadPathHashMap: HashMap<String, List<String>>): String {
            val connection: HttpURLConnection
            val outputStream: DataOutputStream
            val inputStream: InputStream

            val twoHyphens = "--"
            val boundary = "*****" + java.lang.Long.toString(System.currentTimeMillis()) + "*****"
            val lineEnd = "\r\n"

            val result: String

            var bytesRead: Int
            var bytesAvailable: Int
            var bufferSize: Int
            var buffer: ByteArray
            val maxBufferSize = 1024 * 1024


            val url = URL(urlTo)
            connection = url.openConnection() as HttpURLConnection

            connection.doInput = true
            connection.doOutput = true
            connection.useCaches = false

            connection.requestMethod = "POST"
            connection.setRequestProperty("Connection", "Keep-Alive")
            connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0")
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary)


            for ((key, value) in params) {
                connection.setRequestProperty(key, value)
            }


            outputStream = DataOutputStream(connection.outputStream)

            for ((fileField, filepathList) in uploadPathHashMap) {
                for ((i, filepath) in filepathList.withIndex()) {
                    LPHLog.d("filePath upload: " + filepath)
                    val q = filepath.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val idx = q.size - 1
                    outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"" + fileField + "[" + i + "]" + "\"; filename=\"" + q[idx] + "\"" + lineEnd)
                    outputStream.writeBytes("Content-Type: image/jpeg" + lineEnd)
                    outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd)
                    outputStream.writeBytes(lineEnd)

                    val file = File(filepath)
                    val fileInputStream = FileInputStream(file)
                    bytesAvailable = fileInputStream.available()
                    bufferSize = Math.min(bytesAvailable, maxBufferSize)
                    buffer = ByteArray(bufferSize)
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                    while (bytesRead > 0) {
                        outputStream.write(buffer, 0, bufferSize)
                        bytesAvailable = fileInputStream.available()
                        bufferSize = Math.min(bytesAvailable, maxBufferSize)
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize)
                    }

                    outputStream.writeBytes(lineEnd)
                    fileInputStream.close()
                }
            }


            // Upload POST Data
            /*String post = getPostDataString(params);
        String[] posts = post.split("&");
        int max = posts.length;
        for (int i = 0; i < max; i++) {
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            String[] kv = posts[i].split("=");
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + kv[0] + "\"" + lineEnd);
            outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(kv[1]);
            outputStream.writeBytes(lineEnd);
        }*/

            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)

            inputStream = connection.inputStream
            result = convertStreamToString(inputStream)

            inputStream.close()
            outputStream.flush()
            outputStream.close()

            LPHLog.d("response:" + result)
            return result

        }

        @Throws(IOException::class)
        fun httpFileUploadProgress(urlTo: String, params: HashMap<String, String>, filepathList: List<String>, filefield: String): String {
            val inputStream: InputStream? = null

            val boundary = "---------------------------boundary"
            val tail = "\r\n--$boundary--\r\n"
            val lineEnd = "\r\n"

            val result = ""

            var bytesRead: Int? = -1
            val bytesAvailable: Int
            val bufferSize: Int
            val buffer: ByteArray
            val maxBufferSize = 1024 * 1024

            val url = URL(urlTo)
            val connection = url.openConnection() as HttpURLConnection

            connection.doOutput = true
            connection.useCaches = false

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary)

            val metadataPart = ("--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"metadata\"\r\n\r\n"
                    + "" + "\r\n")

            val i = 0
            //        for(String filepath: filepathList) {

            val filePathList = filepathList[0]
            val file = File(filePathList)
            val stringData = generateFileHeader(filePathList, boundary, filefield, lineEnd, tail, metadataPart, 0)
            val fileLength = file.length() + tail.length
            val requestLength = stringData.length + fileLength
            connection.setRequestProperty("Content-length", "" + requestLength)

            for ((key, value) in params) {
                connection.setRequestProperty(key, value)
            }
            connection.setFixedLengthStreamingMode(requestLength.toInt())
            connection.connect()


            val outputStream = DataOutputStream(connection.outputStream)


            outputStream.writeBytes(stringData)
            outputStream.flush()

            var progress = 0
            val buf = ByteArray(1024)
            val bufInput = BufferedInputStream(FileInputStream(file))
            while ({ bytesRead = bufInput.read(buf); bytesRead }() != null) {
                // write output
                outputStream.write(buf, 0, bytesRead!!)
                outputStream.flush()
                progress += bytesRead!!
                // update progress bar
                LPHLog.d("progress:" + progress)
            }

            // Write closing boundary and close stream
            outputStream.writeBytes(tail)
            outputStream.flush()
            outputStream.close()

            // Get server response
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            var line: String? = null
            val builder = StringBuilder()

            while ({ line = reader.readLine(); line }() != null) {
                builder.append(line)
            }
            LPHLog.d("response:" + builder.toString())
            return builder.toString()

        }


        private fun generateFileHeader(filepath: String, boundary: String, filefield: String, lineEnd: String, tail: String, metadataPart: String, i: Int): String {

            LPHLog.d("filePath upload: " + filepath)
            val q = filepath.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val idx = q.size - 1

            val fileHeader1 = ("--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"" + filefield + "[" + i + "]" + "\"; filename=\"" + q[idx] + "\"" + lineEnd
                    + "Content-Type: application/octet-stream\r\n"
                    + "Content-Transfer-Encoding: binary\r\n")

            val file = File(filepath)

            val fileLength = file.length() + tail.length
            val fileHeader2 = "Content-length: " + fileLength + "\r\n"
            val fileHeader = fileHeader1 + fileHeader2 + "\r\n"
            return metadataPart + fileHeader
        }

        @Throws(IOException::class)
        private fun convertStreamToString(`is`: InputStream): String {
            val reader = BufferedReader(InputStreamReader(`is`))
            val sb = StringBuilder()
            var line: String? = null
            while ({ line = reader.readLine(); line }() != null) {
                sb.append(line)
            }
            `is`.close()
            return sb.toString()
        }

    }

}