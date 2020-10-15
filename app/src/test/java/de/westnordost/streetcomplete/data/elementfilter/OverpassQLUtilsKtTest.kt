package de.westnordost.streetcomplete.data.elementfilter

import de.westnordost.osmapi.map.data.BoundingBox
import org.junit.Assert.*
import org.junit.Test

class OverpassQLUtilsKtTest {

    @Test fun `truncates overpass bbox`() {
        assertEquals(
            "[bbox:0.0001235,1.0001235,2.0001235,3.0001235];",
            BoundingBox(
            0.0001234567890123456789,
            1.0001234567890123456789,
            2.0001234567890123456789,
            3.0001234567890123456789
        ).toGlobalOverpassBBox())
    }

}
