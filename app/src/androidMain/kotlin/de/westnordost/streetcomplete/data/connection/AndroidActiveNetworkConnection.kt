package de.westnordost.streetcomplete.data.connection

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private typealias AndroidNetworkCapabilities = android.net.NetworkCapabilities

class AndroidActiveNetworkConnection(private val context: Context) : ActiveNetworkConnection {
    override val capabilitiesFlow: Flow<NetworkCapabilities?> = callbackFlow {
        trySend(capabilities)

        val networkCallback = object : NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: AndroidNetworkCapabilities) {
                trySend(networkCapabilities.toNetworkCapabilities())
            }

            override fun onLost(network: Network) {
                trySend(null)
            }
        }

        context.connectivityManager.registerDefaultNetworkCallback(networkCallback)

        awaitClose { context.connectivityManager.unregisterNetworkCallback(networkCallback) }
    }

    override val capabilities: NetworkCapabilities? get() {
        val activeNetwork = context.connectivityManager.activeNetwork ?: return null
        val networkCapabilities = context.connectivityManager.getNetworkCapabilities(activeNetwork)

        return networkCapabilities?.toNetworkCapabilities()
    }
}

private val Context.connectivityManager get() = getSystemService<ConnectivityManager>()!!

private fun AndroidNetworkCapabilities.toNetworkCapabilities() = NetworkCapabilities(
    hasInternet =
        hasCapability(AndroidNetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        hasCapability(AndroidNetworkCapabilities.NET_CAPABILITY_VALIDATED),
    isMetered =
        !hasCapability(AndroidNetworkCapabilities.NET_CAPABILITY_NOT_METERED)
)

