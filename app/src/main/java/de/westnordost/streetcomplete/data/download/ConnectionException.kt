package de.westnordost.streetcomplete.data.download

class ConnectionException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)
