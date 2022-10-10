package de.westnordost.streetcomplete.data.osm.created_elements

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class CreatedElementsControllerTest {

    private lateinit var ctrl: CreatedElementsController
    private lateinit var db: CreatedElementsDao

    @Before fun setUp() {
        db = mock()
        ctrl = CreatedElementsController(db)
    }

    @Test fun `contains works with old and new ids`() {
        on(db.getAll()).thenReturn(listOf(
            CreatedElementKey(ElementType.NODE, 1, 123)
        ))
        assertTrue(ctrl.contains(ElementType.NODE, 1))
        assertTrue(ctrl.contains(ElementType.NODE, 123))
        assertFalse(ctrl.contains(ElementType.NODE, 124))
        assertFalse(ctrl.contains(ElementType.WAY, 1))
    }

    @Test fun `getId works with old and new ids`() {
        on(db.getAll()).thenReturn(listOf(
            CreatedElementKey(ElementType.NODE, 1, 123)
        ))
        assertEquals(123L, ctrl.getId(ElementType.NODE, 1))
        assertEquals(123L, ctrl.getId(ElementType.NODE, 123))
        assertNull(ctrl.getId(ElementType.NODE, 124))
        assertNull(ctrl.getId(ElementType.WAY, 1))
    }
}
