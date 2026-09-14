package de.westnordost.streetcomplete.screens.main.map

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class MapTilesTest {
    @Test fun `offline download style uses the same tile source as the map`() {
        val style = Json.parseToJsonElement(
            File("src/commonMain/composeResources/files/map-download-style.json").readText()
        ).jsonObject
        val source = style.getValue("sources").jsonObject.values.single().jsonObject

        assertEquals(listOf(MapTiles.URL_TEMPLATE), source.getValue("tiles").jsonArray.map { it.jsonPrimitive.content })
        assertEquals(MapTiles.MAX_ZOOM, source.getValue("maxzoom").jsonPrimitive.int)
    }
}
