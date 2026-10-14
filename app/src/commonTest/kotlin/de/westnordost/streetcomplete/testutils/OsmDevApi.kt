package de.westnordost.streetcomplete.testutils

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry

object OsmDevApi {
    const val URL = "https://master.apis.dev.openstreetmap.org/api/0.6/"

    // copied straight from osmapi (Java), because, why not
    const val ALLOW_EVERYTHING_TOKEN = "qzaxWiG2tprF1IfEcwf4-mn7Al4f2lsM3CNrvGEaIL0"
    const val ALLOW_NOTHING_TOKEN = "fp2SjHKQ55rSdI2x4FN_s0wNUh67dgNbf9x3WdjCa5Y"

    /**
     * Creates an [HttpClient] that automatically retries requests that fail due to a connection problem
     * or a server error. Use this instead of a plain [HttpClient] in tests that talk to a live server,
     * since those occasionally fail due to transient issues unrelated to the code under test.
     */
    fun httpClient(): HttpClient = HttpClient {
        install(HttpRequestRetry) {
            retryOnExceptionOrServerErrors(maxRetries = 3)
            exponentialDelay()
        }
    }
}
