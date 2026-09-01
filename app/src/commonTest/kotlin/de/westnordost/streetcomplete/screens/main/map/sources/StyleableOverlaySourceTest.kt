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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun supersededViewportCannotPublishAfterNewerViewport() = runTest {
        val first = Node(1, p(1.0, 2.0))
        val second = Node(2, p(20.0, 30.0))
        val firstData = createMapData(mapOf(first to ElementPointGeometry(first.position)))
        val secondData = createMapData(mapOf(second to ElementPointGeometry(second.position)))
        val overlay = AllNodesOverlay()
        val selectedOverlaySource: SelectedOverlaySource = mock {
            every { selectedOverlay } returns overlay
        }
        lateinit var source: StyleableOverlaySource
        var loads = 0
        val mapDataSource: MapDataWithEditsSource = mock {
            every { getMapDataWithGeometry(any()) } calls {
                if (loads++ == 0) {
                    source.onViewportChanged(14.0, bbox(19.9999, 29.9999, 20.0001, 30.0001))
                    firstData
                } else {
                    secondData
                }
            }
        }
        source = StyleableOverlaySource(
            selectedOverlaySource,
            mapDataSource,
            UnconfinedTestDispatcher(testScheduler),
        )

        source.onViewportChanged(14.0, bbox(0.9999, 1.9999, 1.0001, 2.0001))
        advanceUntilIdle()

        assertEquals(second, source.styledElements.value.single().element)
        source.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun clearedSourceCannotRepublishInFlightViewport() = runTest {
        val node = Node(1, p(1.0, 2.0))
        val mapData = createMapData(mapOf(node to ElementPointGeometry(node.position)))
        val overlay = AllNodesOverlay()
        val selectedOverlaySource: SelectedOverlaySource = mock {
            every { selectedOverlay } returns overlay
        }
        lateinit var mapDataListener: MapDataWithEditsSource.Listener
        val mapDataSource: MapDataWithEditsSource = mock {
            every { addListener(any()) } calls { (listener: MapDataWithEditsSource.Listener) ->
                mapDataListener = listener
            }
            every { getMapDataWithGeometry(any()) } calls {
                mapDataListener.onCleared()
                mapData
            }
        }
        val source = StyleableOverlaySource(
            selectedOverlaySource,
            mapDataSource,
            UnconfinedTestDispatcher(testScheduler),
        )

        source.onViewportChanged(14.0, bbox(0.9999, 1.9999, 1.0001, 2.0001))
        advanceUntilIdle()

        assertTrue(source.styledElements.value.isEmpty())
        source.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun clearedSourceCannotRepublishInFlightDelta() = runTest {
        val node = Node(1, p(1.0, 2.0))
        val mapData = createMapData(mapOf(node to ElementPointGeometry(node.position)))
        lateinit var mapDataListener: MapDataWithEditsSource.Listener
        var clearWhileStyling = false
        val overlay = AllNodesOverlay {
            if (clearWhileStyling) {
                clearWhileStyling = false
                mapDataListener.onCleared()
            }
        }
        val selectedOverlaySource: SelectedOverlaySource = mock {
            every { selectedOverlay } returns overlay
        }
        val mapDataSource: MapDataWithEditsSource = mock {
            every { addListener(any()) } calls { (listener: MapDataWithEditsSource.Listener) ->
                mapDataListener = listener
            }
            every { getMapDataWithGeometry(any()) } returns mapData
        }
        val source = StyleableOverlaySource(
            selectedOverlaySource,
            mapDataSource,
            UnconfinedTestDispatcher(testScheduler),
        )
        source.onViewportChanged(14.0, bbox(0.9999, 1.9999, 1.0001, 2.0001))
        advanceUntilIdle()
        assertEquals(node, source.styledElements.value.single().element)

        clearWhileStyling = true
        mapDataListener.onUpdated(mapData, emptyList())
        advanceUntilIdle()

        assertTrue(source.styledElements.value.isEmpty())
        source.close()
    }
}

private class AllNodesOverlay(private val onStyle: () -> Unit = {}) : TestOverlayA() {
    override fun getStyledElements(mapData: MapDataWithGeometry) =
        mapData.asSequence().map {
            onStyle()
            it to OverlayStyle.Point(icon)
        }
}
