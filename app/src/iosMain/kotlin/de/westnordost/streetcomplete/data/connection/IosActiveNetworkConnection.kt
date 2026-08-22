package de.westnordost.streetcomplete.data.connection

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import platform.Network.nw_path_get_status
import platform.Network.nw_path_is_expensive
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_t
import platform.darwin.dispatch_queue_create

class IosActiveNetworkConnection : ActiveNetworkConnection {
    private val queue = dispatch_queue_create("network-monitor", null)
    private val _flow = MutableStateFlow<NetworkCapabilities?>(null)

    private fun mapPath(path: nw_path_t): NetworkCapabilities? {
        if(path == null) return null

        val status = nw_path_get_status(path)
        if (status != nw_path_status_satisfied) return null
        val isMetered = nw_path_is_expensive(path)
        return NetworkCapabilities(hasInternet = true, isMetered = isMetered)
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val state: StateFlow<NetworkCapabilities?> = callbackFlow {
        val monitor = nw_path_monitor_create()
        nw_path_monitor_set_queue(monitor, queue)
        nw_path_monitor_set_update_handler(monitor) { path ->
            _flow.value = mapPath(path)
            trySend(_flow.value)
        }
        nw_path_monitor_start(monitor)
        awaitClose { nw_path_monitor_cancel(monitor) }
    }.stateIn(scope, SharingStarted.WhileSubscribed(), null)

    override val capabilitiesFlow: Flow<NetworkCapabilities?> get() = state
    override val capabilities: NetworkCapabilities? get() = state.value
}
