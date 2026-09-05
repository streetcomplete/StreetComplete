package de.westnordost.streetcomplete.screens.main.map

import kotlin.test.Test
import kotlin.test.assertEquals

class MapStyleResourceUriTest {

    @Test
    fun glyphTemplateHandlesPercentEncodedFontName() {
        assertEquals(
            "file:/compose-resources/files/glyphs/{fontstack}/{range}.pbf",
            streetCompleteGlyphTemplate(
                "file:/compose-resources/files/glyphs/Roboto%20Regular/0-255.pbf"
            )
        )
    }

    @Test
    fun glyphTemplatePreservesMapLibreAndroidAssetFileUri() {
        assertEquals(
            "file:///android_asset/compose-resources/files/glyphs/{fontstack}/{range}.pbf",
            streetCompleteGlyphTemplate(
                "file:///android_asset/compose-resources/files/glyphs/Roboto Regular/0-255.pbf"
            )
        )
    }
}
