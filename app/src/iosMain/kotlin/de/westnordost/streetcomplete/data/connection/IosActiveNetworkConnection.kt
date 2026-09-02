package de.westnordost.streetcomplete.data.connection

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import platform.Network.nw_path_get_status
import platform.Network.nw_path_is_expensive
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_invalid
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_t
import platform.darwin.dispatch_queue_create
import kotlin.time.Duration.Companion.milliseconds

class IosActiveNetworkConnection : ActiveNetworkConnection {
    private val queue = dispatch_queue_create("network-monitor", null)

    private fun mapPath(path: nw_path_t): NetworkCapabilities? {
        if (path == null) return null
        val status = nw_path_get_status(path)
        if (status == nw_path_status_invalid) return null
        return NetworkCapabilities(
            hasInternet = status == nw_path_status_satisfied,
            isMetered = nw_path_is_expensive(path),
        )
    }

    override val capabilities = callbackFlow {
        val monitor = nw_path_monitor_create()
        nw_path_monitor_set_queue(monitor, queue)
        nw_path_monitor_set_update_handler(monitor) { path ->
            val capabilities = mapPath(path)
            trySend(capabilities)
        }
        nw_path_monitor_start(monitor)
        awaitClose { nw_path_monitor_cancel(monitor) }
    }
}
