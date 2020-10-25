package org.lovepeaceharmony.androidapp.utility.http

import java.io.IOException
import java.io.Serializable
import java.net.SocketException
import java.net.UnknownHostException

/**
 * Response
 * Created by Naveen Kumar M on 02/01/18.
 */
class Response<T:Any>: Serializable {

    private var throwable: Throwable? = null
    private var isSuccess: Boolean = false
    private var serverMessage: String? = ""
    private var result: T? = null
    private var metaData: Any? = null

    fun Response() {
        isSuccess = false
    }

    fun Response(throwable: Throwable){
        this.throwable = throwable
        handleException()
    }

    fun getThrowable(): Throwable? {
        return throwable
    }

    fun setThrowable(throwable: Throwable) {
        this.throwable = throwable
        if (throwable is LPHException) {
            serverMessage = throwable.responseMessage
        }
        handleException()
    }

    fun getServerMessage(): String? {
        return serverMessage
    }

    fun setServerMessage(serverMessage: String) {
        this.serverMessage = serverMessage
    }

    fun getResult(): T? {
        return result
    }

    fun setResult(result: T) {
        this.result = result
    }

    fun isSuccess(): Boolean {
        return isSuccess
    }

    fun setSuccess(isSuccess: Boolean) {
        this.isSuccess = isSuccess
    }

    fun getMetaData(): Any? {
        return metaData
    }

    fun setMetaData(metaData: Any) {
        this.metaData = metaData
    }

    private fun handleException() {
        isSuccess = false
        serverMessage = if (throwable is UnknownHostException || throwable is IOException) {
            "Server not reachable, please try again after sometimes."
        } else if (throwable is SocketException) {
            "Server is taking too much time to respond, please try again."
        } else if (throwable is LPHException) {
            "Please check your internet connection."
        } else {
            "Unexpected error occurred while contacting the server, please try again."
        }
    }

}
