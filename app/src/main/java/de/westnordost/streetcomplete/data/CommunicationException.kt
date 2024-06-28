package de.westnordost.streetcomplete.data

/** An error while communicating with an API over the internet occured. */
open class CommunicationException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/** An error that indicates that the user either does not have the necessary authorization or
 *  authentication to executes an action through an API over the internet. */
class AuthorizationException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null
) : CommunicationException(message, cause)

/** An error occurred while trying to communicate with an API over the internet. Either the API is
 *  not reachable or replies with a server error.
 *  Like with an IO error, there is nothing we can do about that other than trying again later. */
class ConnectionException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null
) : CommunicationException(message, cause)

/** While posting an update to an API over the internet, the API reports that our data is based on
 *  outdated data */
class ConflictException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null
) : CommunicationException(message, cause)
