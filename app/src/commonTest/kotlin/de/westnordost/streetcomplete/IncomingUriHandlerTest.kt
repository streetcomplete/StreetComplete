package de.westnordost.streetcomplete

import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class IncomingUriHandlerTest {
    @Test fun buffersUrisBeforeCollectionAndRetainsOrder() = runTest {
        val handler = IncomingUriHandler()

        handler.submit("geo:1,2?z=18")
        handler.submit("streetcomplete://preset")

        assertEquals(
            listOf("geo:1,2?z=18", "streetcomplete://preset"),
            handler.uris.take(2).toList(),
        )
    }

    @Test fun eachSubmissionRequestsMainNavigation() {
        val handler = IncomingUriHandler()

        assertEquals(0L, handler.submissionSequence.value)
        handler.submit("geo:1,2")
        handler.submit("https://streetcomplete.app/config/test")

        assertEquals(2L, handler.submissionSequence.value)
    }
}
