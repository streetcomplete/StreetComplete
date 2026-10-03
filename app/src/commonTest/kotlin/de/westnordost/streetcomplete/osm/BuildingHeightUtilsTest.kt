package de.westnordost.streetcomplete.osm

import kotlin.test.Test
import kotlin.test.assertEquals

class BuildingHeightUtilsTest {
    @Test fun `explicit height takes precedence over levels`() {
        assertEquals(12.5f, estimateBuildingHeight(mapOf(
            "height" to "12.5",
            "building:levels" to "3"
        )))
    }

    @Test fun `unparseable height without levels`() {
        assertEquals(null, estimateBuildingHeight(mapOf("height" to "47 m")))
        assertEquals(null, estimateBuildingHeight(mapOf("height" to "23,42")))
        assertEquals(null, estimateBuildingHeight(mapOf("height" to "S")))
        assertEquals(null, estimateBuildingHeight(mapOf("height" to "art")))
        assertEquals(null, estimateBuildingHeight(mapOf("height" to "")))
    }

    @Test fun `unparseable height falls back to levels`() {
        assertEquals(11f, estimateBuildingHeight(mapOf(
            "height" to "47 m",
            "building:levels" to "3",
            "roof:levels" to "1"
        )))
    }

    @Test fun `explicit minimum height takes precedence over minimum level`() {
        assertEquals(1.5f, estimateMinBuildingHeight(mapOf(
            "min_height" to "1.5",
            "building:min_level" to "1"
        )))
    }

    @Test fun `unparseable minimum height without minimum level`() {
        assertEquals(null, estimateMinBuildingHeight(mapOf("min_height" to "3 m")))
        assertEquals(null, estimateMinBuildingHeight(mapOf("min_height" to "unknown")))
        assertEquals(null, estimateMinBuildingHeight(mapOf("min_height" to "")))
    }

    @Test fun `unparseable minimum height falls back to minimum level`() {
        assertEquals(3f, estimateMinBuildingHeight(mapOf(
            "min_height" to "unknown",
            "building:min_level" to "1"
        )))
    }
}
