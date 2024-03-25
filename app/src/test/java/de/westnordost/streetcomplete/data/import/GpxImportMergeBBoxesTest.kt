package de.westnordost.streetcomplete.data.import

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.toPolygon
import de.westnordost.streetcomplete.util.math.area
import de.westnordost.streetcomplete.util.math.isCompletelyInside
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GpxImportMergeBBoxesTest {
    @Test
    fun `gracefully handles empty sequence`() {
        assertEquals(
            0,
            emptySequence<DecoratedBoundingBox>().mergeBBoxes().count(),
            "empty sequence not retained"
        )
    }
    @Test
    fun `merges same size squares in an L-shape`() {
        val inputBoxes = listOf(
            BoundingBox(LatLon(0.00, 0.00), LatLon(0.01, 0.01)),
            BoundingBox(LatLon(0.01, 0.00), LatLon(0.02, 0.01)),
            BoundingBox(LatLon(0.00, 0.01), LatLon(0.01, 0.02)),
        ).map { DecoratedBoundingBox(it.toPolygon()) }
        val downloadBoxes = inputBoxes.asSequence().mergeBBoxes().toList()
        assertEquals(
            1,
            downloadBoxes.size,
            "failed to merge all boxes into one"
        )
        val downloadBBox = downloadBoxes[0].boundingBox
        inputBoxes.map { it.boundingBox }.forEach {
            assertTrue(
                it.isCompletelyInside(downloadBBox),
                "input bounding box $it is not contained in download box $downloadBBox"
            )
        }
    }

    @Test
    fun `partially merges L-shape with one long leg`() {
        val inputBoxes = listOf(
            BoundingBox(LatLon(0.00, 0.00), LatLon(0.01, 0.01)),
            BoundingBox(LatLon(0.01, 0.00), LatLon(0.02, 0.01)),
            BoundingBox(LatLon(0.00, 0.01), LatLon(0.01, 0.06)),
        ).map { DecoratedBoundingBox(it.toPolygon()) }
        val downloadBoxes = inputBoxes.asSequence().mergeBBoxes().toList()
        assertEquals(
            2,
            downloadBoxes.size,
            "failed to merge one side of the L-shape"
        )
        val downloadBBoxes = downloadBoxes.map { it.boundingBox }
        inputBoxes.map { it.boundingBox }.forEach {
            assertTrue(
                downloadBBoxes.any { downloadBBox -> it.isCompletelyInside(downloadBBox) },
                "input bounding box $it is not contained in any download box $downloadBBoxes"
            )
        }
        assertEquals(
            inputBoxes.sumOf { it.boundingBox.area() },
            downloadBBoxes.sumOf { it.area() },
            1.0,
            "area to download is not the same as area of input boxes"
        )
    }
}
