package de.westnordost.streetcomplete.data.upload

class QueryTooBigException @JvmOverloads constructor(
    message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
