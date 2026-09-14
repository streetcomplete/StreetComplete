package de.westnordost.streetcomplete.screens.main.map.sources

import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.MutableMapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.screens.main.map.layers.StyledElement
import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.collectEmissions
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.pGeom
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StyleableOverlaySourceTest {

    private lateinit var selectedOverlaySource: SelectedOverlaySource
    private lateinit var mapDataSource: MapDataWithEditsSource
    private lateinit var overlayListener: SelectedOverlaySource.Listener
    private lateinit var dataListener: MapDataWithEditsSource.Listener
    private lateinit var source: StyleableOverlaySource

    private val view = bbox(0.0, 0.0, 0.001, 0.001)
    private val nodeInView = node(1, p(0.0005, 0.0005))
    private val style = OverlayStyle.Point(icon = null)

    /** styles every element it is given */
    private val overlay = mock<Overlay> {
        every { getStyledElements(any()) } calls { (mapData: MapDataWithGeometry) ->
            mapData.map { it to style }.asSequence()
        }
    }

    @BeforeTest fun setUp() {
        selectedOverlaySource = mock {
            every { addListener(any()) } calls { (listener: SelectedOverlaySource.Listener) ->
                overlayListener = listener
            }
            every { selectedOverlay } returns overlay
        }
        mapDataSource = mock {
            every { addListener(any()) } calls { (listener: MapDataWithEditsSource.Listener) ->
                dataListener = listener
            }
            every { getMapDataWithGeometry(any()) } returns mapDataOf(nodeInView)
        }
        source = StyleableOverlaySource(selectedOverlaySource, mapDataSource)
    }

    @Test fun `nothing while the displayed area is unknown`() = runBlocking {
        assertTrue(source.styledElements.first().isEmpty())
    }

    @Test fun `nothing while no overlay is selected`() = runBlocking {
        every { selectedOverlaySource.selectedOverlay } returns null
        source.onMapMoved(16.0, view)
        assertTrue(source.styledElements.first().isEmpty())
    }

    @Test fun `styled elements in the displayed area`() = runBlocking {
        source.onMapMoved(16.0, view)
        val elements = source.styledElements.first()
        assertEquals(listOf(StyledElement(nodeInView, pGeom(0.0005, 0.0005), style)), elements)
    }

    @Test fun `elements are updated when map data changes`() = runBlocking {
        source.onMapMoved(16.0, view)
        val elements = collectEmissions(source.styledElements)
        assertEquals(1, elements.next().size)

        val addedOutsideView = node(2, p(5.0, 5.0))
        dataListener.onUpdated(mapDataOf(addedOutsideView), emptyList())
        assertEquals(1, elements.next().size)

        val addedInView = node(3, p(0.0006, 0.0006))
        dataListener.onUpdated(mapDataOf(addedInView), emptyList())
        assertEquals(setOf(nodeInView.key, addedInView.key), elements.next().map { it.element.key }.toSet())

        dataListener.onUpdated(mapDataOf(), listOf(nodeInView.key))
        assertEquals(listOf(addedInView.key), elements.next().map { it.element.key })

        dataListener.onCleared()
        assertTrue(elements.next().isEmpty())

        elements.stop()
    }

    @Test fun `elements are reloaded when the overlay changes`() = runBlocking {
        source.onMapMoved(16.0, view)
        val elements = collectEmissions(source.styledElements)
        assertEquals(1, elements.next().size)

        every { selectedOverlaySource.selectedOverlay } returns null
        overlayListener.onSelectedOverlayChanged()
        assertTrue(elements.next().isEmpty())

        elements.stop()
    }

    private fun mapDataOf(vararg nodes: Node) =
        MutableMapDataWithGeometry().also { data ->
            nodes.forEach { data.put(it, pGeom(it.position.latitude, it.position.longitude)) }
        }
}
