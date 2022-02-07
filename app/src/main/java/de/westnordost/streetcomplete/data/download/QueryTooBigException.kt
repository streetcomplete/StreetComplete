package de.westnordost.streetcomplete.data.download

class QueryTooBigException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)
