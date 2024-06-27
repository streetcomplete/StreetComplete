package de.westnordost.streetcomplete.data

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

inline fun <T> wrapApiClientExceptions(block: () -> T): T =
    try {
        block()
    }
    // server replied with (server) error 5xx
    catch (e: ServerResponseException) {
        throw ConnectionException(e.message, e)
    }
    // unexpected answer by server -> server issue
    catch (e: SerializationException) {
        throw ConnectionException(e.message, e)
    }
    // issue with establishing a connection -> nothing we can do about
    catch (e: IOException) {
        throw ConnectionException(e.message, e)
    }
    // server replied with (client) error 4xx
    catch (e: ClientRequestException) {
        when (e.response.status) {
            // request timeout is rather a temporary connection error
            HttpStatusCode.RequestTimeout -> {
                throw ConnectionException(e.message, e)
            }
            // rate limiting is treated like a temporary connection error, i.e. try again later
            HttpStatusCode.TooManyRequests -> {
                throw ConnectionException(e.message, e)
            }
            // authorization is something we can handle (by requiring (re-)login of the user)
            HttpStatusCode.Forbidden, HttpStatusCode.Unauthorized -> {
                throw AuthorizationException(e.message, e)
            }
            else -> {
                throw ApiClientException(e.message, e)
            }
        }
    }

/** The server responded with an unhandled error code */
class ApiClientException(message: String? = null, cause: Throwable? = null)
    : RuntimeException(message, cause)

/** An error occurred while trying to communicate with an API over the internet. Either the
 *  connection with the API cannot be established, the server replies with a server error (5xx),
 *  request timeout (408) or it responds with an unexpected response, i.e. an error occurs while
 *  parsing the response. */
class ConnectionException(message: String? = null, cause: Throwable? = null)
    : RuntimeException(message, cause)

/** While posting an update to an API over the internet, the API reports that our data is based on
 *  outdated data */
class ConflictException(message: String? = null, cause: Throwable? = null)
    : RuntimeException(message, cause)

/** When a query made on an API over an internet would (probably) return a too large result */
class QueryTooBigException (message: String? = null, cause: Throwable? = null)
    : RuntimeException(message, cause)

/** An error that indicates that the user either does not have the necessary authorization or
 *  authentication to execute an action through an API over the internet. */
class AuthorizationException(message: String? = null, cause: Throwable? = null)
    : RuntimeException(message, cause)
