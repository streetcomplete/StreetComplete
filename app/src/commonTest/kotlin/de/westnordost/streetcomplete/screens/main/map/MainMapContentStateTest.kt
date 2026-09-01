package de.westnordost.streetcomplete.screens.main.map

import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_bench_poi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MainMapContentStateTest {
    @Test fun defaultsToVisibleQuestPinsAndOverlay() {
        val state = MainMapContentState()
        assertEquals(MainMapPinMode.QUESTS, state.pinMode)
        assertTrue(state.showPins)
        assertTrue(state.showStyleableOverlay)
    }

    @Test fun editModeAndVisibilityChangeIndependently() {
        val state = MainMapContentState()
        state.updatePinMode(MainMapPinMode.EDITS)
        state.hidePins()
        state.hideOverlay()
        assertEquals(MainMapPinMode.EDITS, state.pinMode)
        assertFalse(state.showPins)
        assertFalse(state.showStyleableOverlay)
    }

    @Test fun geometryMarkersAndSelectedPinsAreRetained() {
        val state = MainMapContentState()
        val geometry = ElementPointGeometry(LatLon(1.0, 2.0))
        state.showGeometry(geometry)
        state.selectPins(Res.drawable.quest_bench_poi, listOf(geometry.center))

        assertEquals(geometry, state.highlightedGeometry)
        assertEquals(listOf(geometry.center), state.selectedPins?.positions)
    }

    @Test fun clearHighlightingRestoresVisibilityWithoutChangingPinMode() {
        val state = MainMapContentState()
        state.updatePinMode(MainMapPinMode.EDITS)
        state.showGeometry(ElementPointGeometry(LatLon(1.0, 2.0)))
        state.selectPins(Res.drawable.quest_bench_poi, listOf(LatLon(1.0, 2.0)))
        state.hidePins()
        state.hideOverlay()

        state.clearHighlighting()

        assertNull(state.highlightedGeometry)
        assertNull(state.selectedPins)
        assertTrue(state.showPins)
        assertTrue(state.showStyleableOverlay)
        assertEquals(MainMapPinMode.EDITS, state.pinMode)
    }
}
