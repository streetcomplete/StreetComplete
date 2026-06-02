package de.westnordost.streetcomplete.osm.things

import de.westnordost.streetcomplete.testutils.node
import kotlin.test.Test
import kotlin.test.assertEquals

class ThingsKtTest {

    @Test fun `disused bench matches`() {
        val node = node(tags = mapOf("disused:amenity" to "bench"))
        assertEquals(true, node.isDisusedThing())
        assertEquals(false, node.isThing())
    }

    @Test fun `flagpole matches`() {
        val node = node(tags = mapOf("man_made" to "flagpole"))
        assertEquals(true, node.isThing())
    }

    @Test fun `specific flagpole matches`() {
        // note that in search currently you may need to type "PL - Poland"
        val node = node(tags = mapOf(
            "country" to "PL",
            "flag:name" to "Poland",
            "flag:type" to "national",
            "flag:wikidata" to "Q42436",
            "man_made" to "flagpole",
            "subject" to "Poland",
            "subject:wikidata" to "Q36",
        ))
        assertEquals(true, node.isThing())
    }
}
