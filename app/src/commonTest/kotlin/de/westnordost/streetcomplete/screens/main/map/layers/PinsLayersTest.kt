package de.westnordost.streetcomplete.screens.main.map.layers

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_bench_poi
import de.westnordost.streetcomplete.ui.ktx.id
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.compose.expressions.ast.FunctionCall
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.toJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class PinsLayersTest {

    @Test fun pinSnapshotReusesPreparedDataUntilContentsChange() {
        val pin = Pin(LatLon(1.0, 2.0), Res.drawable.quest_bench_poi)
        val first = PinSnapshot.Empty.updated(listOf(pin, pin))

        assertEquals(listOf(Res.drawable.quest_bench_poi), first.icons)
        assertEquals(
            pinFeatureCollection(listOf(pin, pin)).toJson(),
            (first.data as GeoJsonData.JsonString).json,
        )
        assertSame(first, first.updated(listOf(pin, pin)))

        val cleared = first.updated(emptyList())
        assertEquals(emptyList(), cleared.pins)
        assertEquals(emptyList(), cleared.icons)
    }

    @Test fun pinIconLooksUpTheFeatureImageIdDirectly() {
        val expression = pinIconExpression()

        assertEquals("image", (expression as FunctionCall).name)
    }

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

    @Test fun pinGeoJsonMatchesFeatureSerializationForEscapedAndDuplicateProperties() {
        val pins = listOf(
            Pin(
                LatLon(1.25, -2.5),
                Res.drawable.quest_bench_poi,
                properties = listOf(
                    "label" to "first",
                    "quoted\"key" to "line one\nline two",
                    "label" to "last",
                    "icon-image" to "custom-icon",
                    "icon-order" to "custom-order",
                ),
                order = 7,
            )
        )

        assertEquals(
            Json.parseToJsonElement(pinFeatureCollection(pins).toJson()),
            Json.parseToJsonElement(pinGeoJson(pins)),
        )
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
        assertTrue(IllegalStateException("No ready loaded style").isStyleHandleRace())
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
