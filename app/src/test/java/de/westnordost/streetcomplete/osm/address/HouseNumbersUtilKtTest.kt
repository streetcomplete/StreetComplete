package de.westnordost.streetcomplete.osm.address

import kotlin.test.Test
import kotlin.test.assertEquals

class HouseNumbersUtilKtTest {

    @Test fun addZero() {
        assertEquals("1", addToHouseNumber("1", 0))
        assertEquals("1c", addToHouseNumber("1c", 0))
    }

    @Test fun addOne() {
        assertEquals("2", addToHouseNumber("1", 1))
        assertEquals("2", addToHouseNumber("1c", 1))
        assertEquals("5", addToHouseNumber("1-4", 1))
        assertEquals("5", addToHouseNumber("1c-4a", 1))
        assertEquals("7", addToHouseNumber("2,6,3", 1))
        assertEquals("7", addToHouseNumber("2,6/3,3", 1))
        assertEquals("9", addToHouseNumber("2,6-8,3", 1))
    }

    @Test fun subtractOne() {
        assertEquals(null, addToHouseNumber("1", -1))
        assertEquals(null, addToHouseNumber("1a", -1))
        assertEquals("1", addToHouseNumber("2", -1))
        assertEquals("1", addToHouseNumber("2/5", -1))
        assertEquals(null, addToHouseNumber("1-4", -1))
        assertEquals(null, addToHouseNumber("1f-4", -1))
        assertEquals("2", addToHouseNumber("3-7", -1))
        assertEquals("2", addToHouseNumber("3c-7", -1))
        assertEquals(null, addToHouseNumber("6,1,3", -1))
        assertEquals(null, addToHouseNumber("6,1/4,3", -1))
        assertEquals("1", addToHouseNumber("6,2,3", -1))
        assertEquals("1", addToHouseNumber("6,2/4,3", -1))
        assertEquals(null, addToHouseNumber("6,1-8,3", -1))
        assertEquals(null, addToHouseNumber("6,1/4-8,3", -1))
        assertEquals("1", addToHouseNumber("6,2-8,3", -1))
        assertEquals("1", addToHouseNumber("6,2f-8,3", -1))
    }
}
