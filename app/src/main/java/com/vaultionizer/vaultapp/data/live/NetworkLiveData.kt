package com.vaultionizer.vaultapp.data.live

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData

class NetworkLiveData(private val connectivityManager: ConnectivityManager) : LiveData<Boolean>() {

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            postValue(true)

        }

        override fun onLost(network: Network) {
            super.onLost(network)
            postValue(false)
        }
    }

    private val networkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

    override fun onActive() {
        super.onActive()
        connectivityManager.registerNetworkCallback(networkRequest, callback)
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(callback)
    }

}