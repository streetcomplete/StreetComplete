package de.westnordost.streetcomplete.data.connection

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Network.nw_path_get_status
import platform.Network.nw_path_is_expensive
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_t
import platform.darwin.dispatch_queue_create

class IosActiveNetworkConnection : ActiveNetworkConnection {
    private val monitor = nw_path_monitor_create()
    private val queue = dispatch_queue_create("network-monitor", null)
    private val _flow = MutableStateFlow<NetworkCapabilities?>(null)

    init {
        nw_path_monitor_set_queue(monitor, queue)
        nw_path_monitor_set_update_handler(monitor) { path ->
            if(path != null) _flow.value = mapPath(path)
        }
        nw_path_monitor_start(monitor)
    }

    private fun mapPath(path: nw_path_t): NetworkCapabilities? {
        val status = nw_path_get_status(path)
        if (status != nw_path_status_satisfied) return null
        val isMetered = nw_path_is_expensive(path)
        return NetworkCapabilities(hasInternet = true, isMetered = isMetered)
    }

    override val capabilitiesFlow: Flow<NetworkCapabilities?>
        get() = _flow
    override val capabilities: NetworkCapabilities?
        get() = _flow.value
}
