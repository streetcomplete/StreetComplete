package de.westnordost.streetcomplete

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/** Buffers external application URLs until the shared main view model can consume them. */
class IncomingUriHandler {
    private val channel = Channel<String>(capacity = Channel.UNLIMITED)

    internal val uris: Flow<String> = channel.receiveAsFlow()

    fun submit(uri: String) {
        check(channel.trySend(uri).isSuccess) { "The application URI channel is unavailable" }
    }
}
