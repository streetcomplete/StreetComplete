package de.westnordost.streetcomplete.data.connection

import kotlinx.coroutines.flow.Flow

/** Provides information about the default active network connection */
interface ActiveNetworkConnection {
    val capabilitiesFlow: Flow<NetworkCapabilities?>

    val capabilities: NetworkCapabilities?
}

data class NetworkCapabilities(
    val hasInternet: Boolean,
    val isMetered: Boolean
)
