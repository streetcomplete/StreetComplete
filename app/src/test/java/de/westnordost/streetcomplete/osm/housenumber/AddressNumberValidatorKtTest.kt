package de.westnordost.streetcomplete.osm.housenumber

import org.junit.Assert
import org.junit.Test

@Test fun `housenumber regex`() {
    val r = VALID_HOUSE_NUMBER_REGEX
    Assert.assertTrue("1".matches(r))
    Assert.assertTrue("1234".matches(r))

    Assert.assertTrue("1234a".matches(r))
    Assert.assertTrue("1234/a".matches(r))
    Assert.assertTrue("1234 / a".matches(r))
    Assert.assertTrue("1234 / A".matches(r))
    Assert.assertTrue("1234/ab".matches(r))
    Assert.assertTrue("1234A".matches(r))
    Assert.assertTrue("1234/9".matches(r))
    Assert.assertTrue("1234 / 9".matches(r))
    Assert.assertTrue("1234/55".matches(r))

    Assert.assertTrue("12345".matches(r))

    Assert.assertTrue("1234AB".matches(r))
    Assert.assertFalse("123456".matches(r))
    Assert.assertFalse("1234 5".matches(r))
}

@Test fun `blocknumber regex`() {
    val r = VALID_BLOCK_NUMBER_REGEX
    Assert.assertTrue("1".matches(r))
    Assert.assertTrue("1234".matches(r))
    Assert.assertFalse("12345".matches(r))

    Assert.assertTrue("1234a".matches(r))
    Assert.assertTrue("1234 a".matches(r))
    Assert.assertFalse("1234 ab".matches(r))
}
