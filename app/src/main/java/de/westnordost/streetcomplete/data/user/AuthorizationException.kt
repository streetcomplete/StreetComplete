package de.westnordost.streetcomplete.data.user

class AuthorizationException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)
