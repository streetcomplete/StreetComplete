package de.westnordost.streetcomplete.data.connection

import java.net.NetworkInterface
import java.util.Collections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Best-effort active-network observation using the JVM's live network-interface table. */
class DesktopActiveNetworkConnection(scope: CoroutineScope) : ActiveNetworkConnection {
    private val state = MutableStateFlow(readCapabilities())

    init {
        scope.launch {
            while (isActive) {
                delay(POLL_INTERVAL_MILLIS)
                state.value = readCapabilities()
            }
        }
    }

    override val capabilitiesFlow: Flow<NetworkCapabilities?> = state
    override val capabilities: NetworkCapabilities? get() = state.value

    private companion object {
        const val POLL_INTERVAL_MILLIS = 5_000L

        fun readCapabilities(): NetworkCapabilities? = try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
                ?.let(Collections::list)
                .orEmpty()
            val connected = interfaces.any { it.isUp && !it.isLoopback && !it.isVirtual }
            if (connected) {
                // The JVM has no portable metered-network signal. Desktop transports are treated
                // as unmetered, matching the ordinary Ethernet/Wi-Fi case.
                NetworkCapabilities(hasInternet = true, isMetered = false)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
