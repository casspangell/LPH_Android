package com.olayg.network

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.ProcessLifecycleOwner
import com.olayg.network.ProcessLifecycleObserver
import com.olayg.network.core.ActivityLifecycleCallbacksImp
import com.olayg.network.core.NetworkCallbackImp
import com.olayg.network.core.NetworkEvent
import com.olayg.network.core.NetworkStateImp

object ConnectivityStateHolder : ConnectivityState {

    private val mutableSet: MutableSet<NetworkState> = mutableSetOf()
    private val callBacks: MutableSet<NetworkCallbackImp> = mutableSetOf()
    private var mNeedUnregister: Boolean = false
    private lateinit var connectivityManager: ConnectivityManager

    override val networkStats: Iterable<NetworkState>
        get() = mutableSet

    private val processLifecycleObserver by lazy {
        ProcessLifecycleObserver()
    }

    private fun networkEventHandler(state: NetworkState, event: NetworkEvent) {
        when (event) {
            is NetworkEvent.AvailabilityEvent -> {
                if (isConnected != event.oldNetworkAvailability) {
                    NetworkEvents.notify(Event.ConnectivityEvent(state.isAvailable))
                }
            }

            else -> {}
        }
    }

    /**
     * This starts the broadcast of network events to NetworkState and all Activity implementing NetworkConnectivityListener
     * @see NetworkState
     * @see NetworkConnectivityListener
     */
    fun registerConnectivityBroadcaster() {
        if (callBacks.size != 0) {
            //has register
            return
        }

        //register to network events
        listOf(
            NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build(),
            NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build()
        ).forEach {
            val stateHolder = NetworkStateImp { a, b -> networkEventHandler(a, b) }
            val callBack = NetworkCallbackImp(stateHolder)
            mutableSet.add(stateHolder)
            callBacks.add(callBack)
            connectivityManager.registerNetworkCallback(it, callBack)
        }

    }

    fun unregisterNetworkCallback() {
        if (mNeedUnregister) {
            callBacks.forEach {
                connectivityManager.unregisterNetworkCallback(it)
            }
            callBacks.clear()
            mutableSet.clear()
        }
    }

    fun Application.initNetworkMonitorConfig(needUnregister: Boolean = true) {
        //get connectivity manager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        mNeedUnregister = needUnregister
        ProcessLifecycleOwner.get().lifecycle.addObserver(processLifecycleObserver)
        //register the Activity Broadcaster
        registerActivityLifecycleCallbacks(ActivityLifecycleCallbacksImp())
    }


}