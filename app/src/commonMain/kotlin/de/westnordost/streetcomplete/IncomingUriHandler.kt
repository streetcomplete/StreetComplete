package de.westnordost.streetcomplete

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/** Buffers external application URLs until the shared main view model can consume them. */
class IncomingUriHandler {
    private val channel = Channel<String>(capacity = Channel.UNLIMITED)
    private val _submissionSequence = MutableStateFlow(0L)

    internal val uris: Flow<String> = channel.receiveAsFlow()
    internal val submissionSequence: StateFlow<Long> = _submissionSequence.asStateFlow()

    fun submit(uri: String) {
        check(channel.trySend(uri).isSuccess) { "The application URI channel is unavailable" }
        _submissionSequence.update { it + 1 }
    }
}
