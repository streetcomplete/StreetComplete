package de.westnordost.streetcomplete.data.user.oauth

/** OAuth failed due to a well-defined error */
class OAuthException @JvmOverloads constructor(
    val error: String,
    val description: String? = null,
    val uri: String? = null
) : RuntimeException(getErrorMessage(error, description, uri))

private fun getErrorMessage(error: String, description: String?, uri: String?): String =
    listOfNotNull(
        error,
        description?.let { ": $description" },
        uri?.let { " (see $uri)" }
    ).joinToString("")
