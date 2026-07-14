package de.westnordost.streetcomplete.data.connection

import kotlinx.coroutines.flow.Flow

class IosActiveNetworkConnection : ActiveNetworkConnection {
    override val capabilitiesFlow: Flow<NetworkCapabilities?>
        get() = TODO("Not yet implemented")
    override val capabilities: NetworkCapabilities?
        get() = TODO("Not yet implemented")
}
