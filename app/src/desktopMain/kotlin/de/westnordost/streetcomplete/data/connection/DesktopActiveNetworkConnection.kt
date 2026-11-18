package de.westnordost.streetcomplete.data.connection

import dev.nucleusframework.systeminfo.SystemInfo
import dev.nucleusframework.systeminfo.model.ConnectivityInfo
import dev.nucleusframework.systeminfo.model.ConnectivityLevel
import dev.nucleusframework.systeminfo.model.MeteredStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class DesktopActiveNetworkConnection : ActiveNetworkConnection {
    override val capabilities: Flow<NetworkCapabilities?> =
        SystemInfo.connectivityFlow()
            .map { it.toNetworkCapabilities() }
            // collecting starts a native monitor and cancelling stops it, both blocking
            .flowOn(Dispatchers.IO)
}

private fun ConnectivityInfo.toNetworkCapabilities() = NetworkCapabilities(
    // the level is unknown e.g. on Linux without NetworkManager or the network monitor portal,
    // so assume a connection then
    hasInternet = level == ConnectivityLevel.FULL || level == ConnectivityLevel.UNKNOWN,
    isMetered = meteredStatus == MeteredStatus.METERED,
)
