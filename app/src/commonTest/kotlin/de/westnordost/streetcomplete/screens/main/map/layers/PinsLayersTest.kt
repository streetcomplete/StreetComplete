package de.westnordost.streetcomplete.screens.main.map.layers

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_bench_poi
import de.westnordost.streetcomplete.ui.ktx.id
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.maplibre.compose.expressions.ast.FunctionCall
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import de.westnordost.streetcomplete.screens.main.map.toPosition
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
            referencePinFeatureCollection(listOf(pin, pin)).toJson(),
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
        val feature = serializedPinFeatures(
            listOf(
                Pin(
                    position = LatLon(1.0, 2.0),
                    icon = icon,
                    properties = listOf("quest_type" to "AddBench"),
                    order = 7,
                )
            )
        ).single().jsonObject

        val properties = feature.getValue("properties").jsonObject
        assertEquals(
            Json.parseToJsonElement("""{"type":"Point","coordinates":[2.0,1.0]}"""),
            feature["geometry"],
        )
        assertEquals(icon.id, (properties["icon-image"] as JsonPrimitive).content)
        assertEquals(57, (properties["icon-order"] as JsonPrimitive).content.toInt())
        assertEquals("AddBench", (properties["quest_type"] as JsonPrimitive).content)
    }

    @Test fun callerPropertiesPreserveLegacyReservedKeyOverride() {
        val feature = serializedPinFeatures(
            listOf(
                Pin(
                    LatLon(1.0, 2.0),
                    Res.drawable.quest_bench_poi,
                    properties = listOf("icon-order" to "custom"),
                )
            )
        ).single().jsonObject

        val properties = feature.getValue("properties").jsonObject
        assertEquals("custom", (properties["icon-order"] as JsonPrimitive).content)
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
            Json.parseToJsonElement(referencePinFeatureCollection(pins).toJson()),
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

private fun referencePinFeatureCollection(pins: Collection<Pin>): FeatureCollection<Point, JsonObject> =
    FeatureCollection(pins.map(Pin::toGeoJsonFeature))

private fun Pin.toGeoJsonFeature(): Feature<Point, JsonObject> {
    val values = mutableMapOf<String, JsonElement>(
        "icon-image" to JsonPrimitive(icon.id ?: error("Pin icon is not a Compose resource")),
        "icon-order" to JsonPrimitive(order + 50),
    )
    properties.forEach { (key, value) -> values[key] = JsonPrimitive(value) }
    return Feature(Point(position.toPosition()), JsonObject(values))
}

private fun serializedPinFeatures(pins: Collection<Pin>) =
    Json.parseToJsonElement(pinGeoJson(pins)).jsonObject.getValue("features").jsonArray
