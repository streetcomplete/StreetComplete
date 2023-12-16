package de.westnordost.streetcomplete.data.user.oauth

/** OAuth failed due to an issue with the connection or a malformed server response */
class OAuthConnectionException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
}
