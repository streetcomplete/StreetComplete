package de.westnordost.streetcomplete.screens.main.map.sources

import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.overlay.TestOverlayA
import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.createMapData
import de.westnordost.streetcomplete.testutils.p
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StyleableOverlaySourceTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun loadsCurrentViewportWhenOverlayIsSelectedAndClearsWhenDeselected() = runTest {
        val node = Node(1, p(1.0, 2.0))
        val mapData = createMapData(mapOf(node to ElementPointGeometry(node.position)))
        var selectedValue: Overlay? = null
        lateinit var selectedOverlayListener: SelectedOverlaySource.Listener
        val selectedOverlaySource: SelectedOverlaySource = mock {
            every { selectedOverlay } calls { selectedValue }
            every { addListener(any()) } calls { (listener: SelectedOverlaySource.Listener) ->
                selectedOverlayListener = listener
            }
        }
        val mapDataSource: MapDataWithEditsSource = mock {
            every { getMapDataWithGeometry(any()) } returns mapData
        }
        val source = StyleableOverlaySource(
            selectedOverlaySource,
            mapDataSource,
            workerDispatcher = StandardTestDispatcher(testScheduler),
        )

        source.onViewportChanged(14.0, bbox(0.9999, 1.9999, 1.0001, 2.0001))
        advanceUntilIdle()
        assertTrue(source.styledElements.value.isEmpty())

        selectedValue = AllNodesOverlay()
        selectedOverlayListener.onSelectedOverlayChanged()
        advanceUntilIdle()
        assertEquals(node, source.styledElements.value.single().element)

        selectedValue = null
        selectedOverlayListener.onSelectedOverlayChanged()
        advanceUntilIdle()
        assertTrue(source.styledElements.value.isEmpty())
        source.close()
    }
}

private class AllNodesOverlay : TestOverlayA() {
    override fun getStyledElements(mapData: MapDataWithGeometry) =
        mapData.asSequence().map { it to OverlayStyle.Point(icon) }
}
