package de.westnordost.streetcomplete.overlays.street_furniture

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.osm.IS_STREET_FURNITURE_INCLUDING_DISUSED_EXPRESSION
import kotlin.test.*
import kotlin.test.Test

class FlagpoleMatching {

    @Test fun `flagpole matches`() {
        val fakeElement = Node(-1L, LatLon(0.0, 0.0), mapOf("man_made" to "flagpole"), 0)
        assertEquals(true, IS_STREET_FURNITURE_INCLUDING_DISUSED_EXPRESSION.matches(fakeElement))
    }

    @Test fun `specific flagpole matches`() {
        // note that in search currently you may need to type "PL - Poland"
        val fakeElement = Node(-1L, LatLon(0.0, 0.0), mapOf(
            "country" to "PL",
            "flag:name" to "Poland",
            "flag:type" to "national",
            "flag:wikidata" to "Q42436",
            "man_made" to "flagpole",
            "subject" to "Poland",
            "subject:wikidata" to "Q36",
        ), 0)
        assertEquals(true, IS_STREET_FURNITURE_INCLUDING_DISUSED_EXPRESSION.matches(fakeElement))
    }
}
