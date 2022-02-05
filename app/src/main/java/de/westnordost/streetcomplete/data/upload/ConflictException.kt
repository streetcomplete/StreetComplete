package de.westnordost.streetcomplete.data.upload

class ConflictException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)
