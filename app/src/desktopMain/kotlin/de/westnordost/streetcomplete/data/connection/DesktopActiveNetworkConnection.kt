package de.westnordost.streetcomplete.data.connection

import dev.nucleusframework.systeminfo.SystemInfo
import dev.nucleusframework.systeminfo.model.MeteredStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.time.Duration.Companion.seconds

class DesktopActiveNetworkConnection : ActiveNetworkConnection {
    // Nucleus can only query the current connection, it doesn't notify about changes
    override val capabilities: Flow<NetworkCapabilities?> = flow {
        while (true) {
            emit(currentCapabilities())
            delay(POLL_INTERVAL)
        }
    }.distinctUntilChanged().flowOn(Dispatchers.IO)

    private fun currentCapabilities(): NetworkCapabilities {
        // if it is not available on this system, assume an unmetered connection, like before
        val info = SystemInfo.connectivityInfo()
            ?: return NetworkCapabilities(hasInternet = true, isMetered = false)
        return NetworkCapabilities(
            hasInternet = info.isConnected,
            isMetered = info.meteredStatus == MeteredStatus.METERED,
        )
    }

    private companion object {
        val POLL_INTERVAL = 10.seconds
    }
}
