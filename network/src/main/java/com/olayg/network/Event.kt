package com.olayg.network

sealed class Event {

    class ConnectivityEvent(val isConnected: Boolean) : Event()
}