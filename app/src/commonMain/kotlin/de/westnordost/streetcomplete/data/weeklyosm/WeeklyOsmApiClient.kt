package de.westnordost.streetcomplete.data.weeklyosm

import kotlin.time.Instant

/** Talks with the WeeklyOsm "API", i.e. just the RSS feed */
interface WeeklyOsmApiClient {
    /** Just gets the last date an edition of WeeklyOSM has been published */
    suspend fun getLastPublishDate(): Instant?
}
