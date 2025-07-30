package de.westnordost.streetcomplete.util.math

import kotlin.test.Test
import kotlin.test.assertEquals

class Vector3dTest {
    @Test fun `minus subtracts components`() {
        val a = Vector3d(4.0, 3.0, 2.0)
        val b = Vector3d(1.0, 1.5, 0.5)
        assertEquals(Vector3d(3.0, 1.5, 1.5), a - b)
    }

    @Test fun `division divides components`() {
        val v = Vector3d(4.0, 2.0, 1.0)
        assertEquals(Vector3d(2.0, 1.0, 0.5), v / 2.0)
    }
}
