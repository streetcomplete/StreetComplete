package de.westnordost.streetcomplete.data.import

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.math.area
import de.westnordost.streetcomplete.util.math.isCompletelyInside
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GpxImportDetermineBBoxesTest {
    @Test
    fun `gracefully handles empty sequence`() {
        assertEquals(
            0,
            emptySequence<BoundingBox>().determineBBoxes().count(),
            "empty sequence not retained"
        )
    }
    @Test
    fun `drops duplicates`() {
        val inputBoxes = listOf(
            BoundingBox(LatLon(-0.01, -0.01), LatLon(0.0, 0.0)),
            BoundingBox(LatLon(-0.01, -0.01), LatLon(0.0, 0.0)),
        )
        val downloadBoxes = inputBoxes.asSequence().determineBBoxes().toList()
        assertEquals(
            1,
            downloadBoxes.size,
            "failed to merge all boxes into one"
        )
        val downloadBBox = downloadBoxes[0].boundingBox
        inputBoxes.forEach {
            assertTrue(
                it.isCompletelyInside(downloadBBox),
                "input bounding box $it is not contained in download box $downloadBBox"
            )
        }
        assertEquals(
            inputBoxes[0].area(),
            downloadBBox.area(),
            1.0,
            "area to download is not the same as area of one input box"
        )
    }

    @Test
    fun `merges adjacent boxes forming a rectangle`() {
        val inputBoxes = listOf(
            BoundingBox(LatLon(0.00, 0.0), LatLon(0.01, 0.01)),
            BoundingBox(LatLon(0.01, 0.0), LatLon(0.02, 0.01)),
            BoundingBox(LatLon(0.02, 0.0), LatLon(0.03, 0.01)),
        )
        val downloadBoxes = inputBoxes.asSequence().determineBBoxes().toList()
        assertEquals(
            1,
            downloadBoxes.size,
            "failed to merge all boxes into one"
        )
        val downloadBBox = downloadBoxes[0].boundingBox
        inputBoxes.forEach {
            assertTrue(
                it.isCompletelyInside(downloadBBox),
                "input bounding box $it is not contained in download box $downloadBBox"
            )
        }
        assertEquals(
            inputBoxes.sumOf { it.area() },
            downloadBBox.area(),
            1.0,
            "area to download is not the same as area of input boxes"
        )
    }

    @Test
    fun `partially merges boxes in an L-shape`() {
        val inputBoxes = listOf(
            BoundingBox(LatLon(0.00, 0.00), LatLon(0.01, 0.01)),
            BoundingBox(LatLon(0.01, 0.00), LatLon(0.02, 0.01)),
            BoundingBox(LatLon(0.00, 0.01), LatLon(0.01, 0.06)),
        )
        val downloadBoxes = inputBoxes.asSequence().determineBBoxes().toList()
        assertEquals(
            2,
            downloadBoxes.size,
            "failed to merge one side of the L-shape"
        )
        val downloadBBoxes = downloadBoxes.map { it.boundingBox }
        inputBoxes.forEach {
            assertTrue(
                downloadBBoxes.any { downloadBBox -> it.isCompletelyInside(downloadBBox) },
                "input bounding box $it is not contained in any download box $downloadBBoxes"
            )
        }
        assertEquals(
            inputBoxes.sumOf { it.area() },
            downloadBBoxes.sumOf { it.area() },
            1.0,
            "area to download is not the same as area of input boxes"
        )
    }
}
