package de.westnordost.streetcomplete.data.osm.tql

import org.junit.Test

import de.westnordost.osmapi.map.data.OsmNode

import org.junit.Assert.*

class KeyFilterTest {

	@Test fun `matches key`() {
		val key = KeyFilter("name", true)

		assertTrue(key.matches(elementWithTag("name", "yes")))
		assertTrue(key.matches(elementWithTag("name", "no")))
		assertFalse(key.matches(elementWithTag("neme", "no")))
		assertFalse(key.matches(elementWithNoTags()))
	}

	@Test fun `not matches key`() {
		val key = KeyFilter("name", false)

		assertFalse(key.matches(elementWithTag("name", "yes")))
		assertFalse(key.matches(elementWithTag("name", "no")))
		assertTrue(key.matches(elementWithTag("neme", "no")))
		assertTrue(key.matches(elementWithNoTags()))
	}

	@Test fun `key to string`() {
		assertEquals("name", KeyFilter("name", true).toOverpassQLString())
		assertEquals("'name:old'", KeyFilter("name:old", true).toOverpassQLString())
	}

	@Test fun `not key to string`() {
		assertEquals("!name", KeyFilter("name", false).toOverpassQLString())
		assertEquals("!'name:old'", KeyFilter("name:old", false).toOverpassQLString())
	}
}

class KeyValueFilterTest {

	@Test fun `matches equal`() {
		val eq = KeyValueFilter("highway", "residential", true)

		assertTrue(eq.matches(elementWithTag("highway", "residential")))
		assertFalse(eq.matches(elementWithTag("highway", "residental")))
		assertFalse(eq.matches(elementWithTag("hipway", "residential")))
		assertFalse(eq.matches(elementWithNoTags()))
	}

	@Test fun `matches not equal`() {
		val neq = KeyValueFilter("highway", "residential", false)

		assertFalse(neq.matches(elementWithTag("highway", "residential")))
		assertTrue(neq.matches(elementWithTag("highway", "residental")))
		assertTrue(neq.matches(elementWithTag("hipway", "residential")))
		assertTrue(neq.matches(elementWithNoTags()))
	}

	@Test fun `key value to string`() {
		val eq = KeyValueFilter("highway", "residential", true)
		assertEquals("highway = residential", eq.toOverpassQLString())
	}

	@Test fun `key not value to string`() {
		val neq = KeyValueFilter("highway", "residential", false)
		assertEquals("highway != residential", neq.toOverpassQLString())
	}
}

class KeyRegexValueFilterTest {

	@Test fun `matches like dot`() {
		val like = KeyRegexValueFilter("highway", ".esidential", true)

		assertTrue(like.matches(elementWithTag("highway", "residential")))
		assertTrue(like.matches(elementWithTag("highway", "wesidential")))
		assertFalse(like.matches(elementWithTag("highway", "rresidential")))
		assertFalse(like.matches(elementWithNoTags()))
	}

	@Test fun `matches like or`() {
		val like = KeyRegexValueFilter("highway", "residential|unclassified", true)

		assertTrue(like.matches(elementWithTag("highway", "residential")))
		assertTrue(like.matches(elementWithTag("highway", "unclassified")))
		assertFalse(like.matches(elementWithTag("highway", "blub")))
		assertFalse(like.matches(elementWithNoTags()))
	}

	@Test fun `matches not like dot`() {
		val notlike = KeyRegexValueFilter("highway", ".*", false)

		assertFalse(notlike.matches(elementWithTag("highway", "anything")))
		assertTrue(notlike.matches(elementWithNoTags()))
	}

	@Test fun `matches not like or`() {
		val notlike = KeyRegexValueFilter("noname", "yes", false)

		assertFalse(notlike.matches(elementWithTag("noname", "yes")))
		assertTrue(notlike.matches(elementWithTag("noname", "no")))
		assertTrue(notlike.matches(elementWithNoTags()))
	}

	@Test fun `key value to string`() {
		val eq = KeyRegexValueFilter("highway", ".*", true)
		assertEquals("highway ~ '^.*$'", eq.toOverpassQLString())
	}

	@Test fun `key not value to string`() {
		val neq = KeyRegexValueFilter("highway", ".*", false)
		assertEquals("highway !~ '^.*$'", neq.toOverpassQLString())
	}
}

class RegexKeyRegexValueFilterTest {

	@Test fun `matches regex key and value`() {
		val eq = RegexKeyRegexValueFilter(".ame", "y.s")

		assertTrue(eq.matches(elementWithTag("name", "yes")))
		assertTrue(eq.matches(elementWithTag("lame", "yos")))
		assertFalse(eq.matches(elementWithTag("lame", "no")))
		assertFalse(eq.matches(elementWithTag("good", "yes")))
		assertFalse(eq.matches(elementWithTag("neme", "no")))
		assertFalse(eq.matches(elementWithTag("names", "yess")))
		assertFalse(eq.matches(elementWithNoTags()))
	}

	@Test fun `to string`() {
		val eq = RegexKeyRegexValueFilter(".ame", "y.s")
		assertEquals("~'^.ame$' ~ '^y.s$'", eq.toOverpassQLString())
	}
}

private fun elementWithTag(k: String, v: String) = OsmNode(0, 0, 0.0, 0.0, mapOf(k to v))
private fun elementWithNoTags() = OsmNode(0, 0, 0.0, 0.0, null)
