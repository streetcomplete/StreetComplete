package de.westnordost.streetcomplete.osm.address

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AddressNumberValidatorKtTest {

    @Test
    fun `housenumber regex`() {
        val r = VALID_HOUSE_NUMBER_REGEX
        assertTrue("1".matches(r))
        assertTrue("1234".matches(r))

        assertTrue("1234a".matches(r))
        assertTrue("1234/a".matches(r))
        assertTrue("1234 / a".matches(r))
        assertTrue("1234 / A".matches(r))
        assertTrue("1234/ab".matches(r))
        assertTrue("1234A".matches(r))
        assertTrue("1234/9".matches(r))
        assertTrue("1234 / 9".matches(r))
        assertTrue("1234/55".matches(r))

        assertTrue("12345".matches(r))

        assertTrue("1234AB".matches(r))
        assertFalse("123456".matches(r))
        assertFalse("1234 5".matches(r))
    }

    @Test
    fun `blocknumber regex`() {
        val r = VALID_BLOCK_NUMBER_REGEX
        assertTrue("1".matches(r))
        assertTrue("1234".matches(r))
        assertFalse("12345".matches(r))

        assertTrue("1234a".matches(r))
        assertTrue("1234 a".matches(r))
        assertFalse("1234 ab".matches(r))
    }
}
