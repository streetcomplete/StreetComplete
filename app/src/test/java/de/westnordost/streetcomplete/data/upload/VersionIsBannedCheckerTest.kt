package de.westnordost.streetcomplete.data.upload

import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class VersionIsBannedCheckerTest {
    private val httpClient = HttpClient()

    @Test fun `banned version `() = runBlocking {
        assertEquals(IsBanned(null), VersionIsBannedChecker(httpClient, URL, "StreetComplete 0.1").get())
    }

    @Test fun `not banned version `() = runBlocking {
        assertEquals(IsNotBanned, VersionIsBannedChecker(httpClient, URL, "StreetComplete 3.0").get())
    }

    @Test fun `banned version with reason`() = runBlocking {
        assertEquals(
            IsBanned("This version does not correctly determine in which country you are, necessary to tag certain answers correctly."),
            VersionIsBannedChecker(httpClient, URL, "StreetComplete 8.0").get()
        )
    }
}

private const val URL = "https://westnordost.de/streetcomplete/banned_versions.txt"
