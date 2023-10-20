package de.westnordost.streetcomplete.data.upload

import kotlin.test.Test
import kotlin.test.assertEquals

class VersionIsBannedCheckerTest {
    @Test fun `banned version `() {
        assertEquals(IsBanned(null), VersionIsBannedChecker(URL, "StreetComplete 0.1").get())
    }

    @Test fun `not banned version `() {
        assertEquals(IsNotBanned, VersionIsBannedChecker(URL, "StreetComplete 3.0").get())
    }

    @Test fun `banned version with reason`() {
        assertEquals(
            IsBanned("This version does not correctly determine in which country you are, necessary to tag certain answers correctly."),
            VersionIsBannedChecker(URL, "StreetComplete 8.0").get()
        )
    }
}

private const val URL = "https://westnordost.de/streetcomplete/banned_versions.txt"
