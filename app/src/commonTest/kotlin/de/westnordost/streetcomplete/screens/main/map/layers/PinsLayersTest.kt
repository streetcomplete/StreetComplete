package de.westnordost.streetcomplete.screens.main.map.layers

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_bench_poi
import de.westnordost.streetcomplete.ui.ktx.id
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PinsLayersTest {

    @Test fun createsPointFeatureWithIconOrderAndClickProperties() {
        val icon = Res.drawable.quest_bench_poi
        val feature = pinFeatureCollection(
            listOf(
                Pin(
                    position = LatLon(1.0, 2.0),
                    icon = icon,
                    properties = listOf("quest_type" to "AddBench"),
                    order = 7,
                )
            )
        ).features.single()

        assertEquals(Point(Position(2.0, 1.0)), feature.geometry)
        assertEquals(icon.id, (feature.properties["icon-image"] as JsonPrimitive).content)
        assertEquals(57, (feature.properties["icon-order"] as JsonPrimitive).content.toInt())
        assertEquals("AddBench", (feature.properties["quest_type"] as JsonPrimitive).content)
    }

    @Test fun callerPropertiesPreserveLegacyReservedKeyOverride() {
        val feature = pinFeatureCollection(
            listOf(
                Pin(
                    LatLon(1.0, 2.0),
                    Res.drawable.quest_bench_poi,
                    properties = listOf("icon-order" to "custom"),
                )
            )
        ).features.single()

        assertEquals("custom", (feature.properties["icon-order"] as JsonPrimitive).content)
    }

    @Test fun clickPropertiesConvertPrimitivesAndIgnoreNulls() {
        val properties = JsonObject(
            mapOf(
                "text" to JsonPrimitive("value"),
                "number" to JsonPrimitive(12),
                "missing" to JsonNull,
            )
        )

        assertEquals(mapOf("text" to "value", "number" to "12"), properties.toStringMap())
    }

    @Test fun onlyKnownStyleGenerationRacesAreRecoverable() {
        assertTrue(
            IllegalStateException(
                "Style operation belongs to a stale loaded-style identity"
            ).isStyleHandleRace()
        )
        assertTrue(
            IllegalStateException(
                "Style operation belongs to a stale or unready loaded-style identity"
            ).isStyleHandleRace()
        )
        assertTrue(
            IllegalStateException(
                "Style operation crossed a loaded-style resource change"
            ).isStyleHandleRace()
        )
        assertFalse(IllegalStateException("Could not parse GeoJSON").isStyleHandleRace())
    }
}
