package de.westnordost.streetcomplete.data.connection

import kotlinx.coroutines.flow.Flow

class IosActiveNetworkConnection : ActiveNetworkConnection {
    override val capabilities: Flow<NetworkCapabilities?>
        get() = TODO("Not yet implemented")
}
