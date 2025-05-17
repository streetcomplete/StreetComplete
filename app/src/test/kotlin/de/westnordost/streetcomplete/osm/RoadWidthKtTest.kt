package de.westnordost.streetcomplete.osm

import kotlin.test.Test
import kotlin.test.assertEquals

class RoadWidthKtTest {
    @Test fun `roadway width`() {
        assertEquals(12.4f, estimateRoadwayWidth(mapOf("width:carriageway" to "12.4")))
        assertEquals(null, estimateRoadwayWidth(mapOf("width:carriageway" to "12 meters")))

        assertEquals(4.3f, estimateRoadwayWidth(mapOf("width" to "4.3")))
        assertEquals(null, estimateRoadwayWidth(mapOf("width" to "4 meters")))

        assertEquals(11f, estimateRoadwayWidth(mapOf("lanes" to "4")))
        assertEquals(null, estimateRoadwayWidth(mapOf("lanes" to "4.5")))
        assertEquals(2.75f, estimateRoadwayWidth(mapOf("lanes" to "0")))

        assertEquals(11.5f, estimateRoadwayWidth(mapOf("highway" to "motorway", "lanes" to "2")))
        assertEquals(7.5f, estimateRoadwayWidth(mapOf("highway" to "trunk", "lanes" to "2")))
        assertEquals(5.5f, estimateRoadwayWidth(mapOf("highway" to "residential", "lanes" to "2")))

        assertEquals(99.5f, estimateRoadwayWidth(mapOf(
            "highway" to "residential",
            "lanes" to "2",
            "shoulder" to "right",
            "shoulder:width" to "94"
        )))
    }

    @Test fun `roadway width checks best tags first`() {
        val tags = mutableMapOf(
            "width" to "11.5",
            "width:carriageway" to "9.4",
            "lanes" to "3"
        )
        assertEquals(9.4f, estimateRoadwayWidth(tags))
        tags.remove("width:carriageway")
        assertEquals(11.5f, estimateRoadwayWidth(tags))
        tags.remove("width")
        assertEquals(8.25f, estimateRoadwayWidth(tags))
    }

    @Test fun `guess roadway width observes oneway tag`() {
        assertEquals(5.5f, guessRoadwayWidth(mapOf("highway" to "residential")))
        assertEquals(2.75f, guessRoadwayWidth(mapOf("highway" to "residential", "oneway" to "-1")))
    }

    @Test fun `usable roadway width`() {
        assertEquals(null, estimateUsableRoadwayWidth(mapOf("width" to "murks")))

        assertEquals(
            10f,
            estimateUsableRoadwayWidth(mapOf(
                "width" to "12",
                "parking:lane:both" to "parallel",
                "parking:lane:both:parallel" to "half_on_kerb" // 1m (both sides)
            ))
        )

        assertEquals(
            10.5f,
            estimateUsableRoadwayWidth(mapOf(
                "width" to "12",
                "cycleway:right" to "lane",
                "cycleway:right:lane" to "exclusive", // 1.5m
                "cycleway:left" to "track"
            ))
        )

        assertEquals(
            10f,
            estimateUsableRoadwayWidth(mapOf(
                "width" to "12",
                "shoulder" to "left"
            ))
        )

        assertEquals(
            2f,
            estimateUsableRoadwayWidth(mapOf(
                "width" to "12",
                "cycleway:both" to "lane",
                "cycleway:right:lane" to "exclusive", // 1.5m
                "cycleway:left:lane" to "advisory", // 1m
                "parking:lane:left" to "diagonal",
                "parking:lane:left:diagonal" to "half_on_kerb", // 1.5m
                "parking:lane:right" to "parallel",
                "parking:lane:right:parallel" to "on_street", // 2m
                "shoulder" to "both" // 4m
            ))
        )
    }

    @Test fun `shoulders width`() {
        assertEquals(null, estimateShouldersWidth(mapOf()))
        assertEquals(4f, estimateShouldersWidth(mapOf("shoulder" to "both")))
        assertEquals(123f, estimateShouldersWidth(mapOf(
            "shoulder" to "right",
            "shoulder:width" to "123"
        )))
        assertEquals(246f, estimateShouldersWidth(mapOf(
            "shoulder" to "yes",
            "shoulder:width" to "123"
        )))
    }

    @Test fun `cycleway lane width`() {
        assertEquals(null, estimateCycleLanesWidth(mapOf()))
        assertEquals(3f, estimateCycleLanesWidth(mapOf(
            "cycleway:both" to "lane",
            "cycleway:both:lane" to "exclusive"
        )))
        assertEquals(1.5f, estimateCycleLanesWidth(mapOf(
            "cycleway:left" to "lane",
            "cycleway:right" to "track",
            "cycleway:left:lane" to "exclusive"
        )))
    }

    @Test fun `specified cycleway lane width takes precedence`() {
        assertEquals(123f, estimateCycleLanesWidth(mapOf(
            "cycleway:left" to "lane",
            "cycleway:left:lane" to "exclusive",
            "cycleway:left:width" to "123"
        )))

        assertEquals(124.5f, estimateCycleLanesWidth(mapOf(
            "cycleway:both" to "lane",
            "cycleway:both:lane" to "exclusive",
            "cycleway:left:width" to "123"
        )))
    }
}
