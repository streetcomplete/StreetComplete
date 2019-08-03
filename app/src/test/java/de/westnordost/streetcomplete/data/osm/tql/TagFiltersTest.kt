package de.westnordost.streetcomplete.data.osm.tql

import org.junit.Test

import org.junit.Assert.*

class KeyFilterTest {

	@Test fun `matches key`() {
		val key = KeyFilter("name", true)

		assertTrue(key.matches(mapOf("name" to "yes")))
		assertTrue(key.matches(mapOf("name" to "no")))
		assertFalse(key.matches(mapOf("neme" to "no")))
		assertFalse(key.matches(mapOf()))
	}

	@Test fun `not matches key`() {
		val key = KeyFilter("name", false)

		assertFalse(key.matches(mapOf("name" to "yes")))
		assertFalse(key.matches(mapOf("name" to "no")))
		assertTrue(key.matches(mapOf("neme" to "no")))
		assertTrue(key.matches(mapOf()))
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

		assertTrue(eq.matches(mapOf("highway" to "residential")))
		assertFalse(eq.matches(mapOf("highway" to "residental")))
		assertFalse(eq.matches(mapOf("hipway" to "residential")))
		assertFalse(eq.matches(mapOf()))
	}

	@Test fun `matches not equal`() {
		val neq = KeyValueFilter("highway", "residential", false)

		assertFalse(neq.matches(mapOf("highway" to "residential")))
		assertTrue(neq.matches(mapOf("highway" to "residental")))
		assertTrue(neq.matches(mapOf("hipway" to "residential")))
		assertTrue(neq.matches(mapOf()))
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

		assertTrue(like.matches(mapOf("highway" to "residential")))
		assertTrue(like.matches(mapOf("highway" to "wesidential")))
		assertFalse(like.matches(mapOf("highway" to "rresidential")))
		assertFalse(like.matches(mapOf()))
	}

	@Test fun `matches like or`() {
		val like = KeyRegexValueFilter("highway", "residential|unclassified", true)

		assertTrue(like.matches(mapOf("highway" to "residential")))
		assertTrue(like.matches(mapOf("highway" to "unclassified")))
		assertFalse(like.matches(mapOf("highway" to "blub")))
		assertFalse(like.matches(mapOf()))
	}

	@Test fun `matches not like dot`() {
		val notlike = KeyRegexValueFilter("highway", ".*", false)

		assertFalse(notlike.matches(mapOf("highway" to "anything")))
		assertTrue(notlike.matches(mapOf()))
	}

	@Test fun `matches not like or`() {
		val notlike = KeyRegexValueFilter("noname", "yes", false)

		assertFalse(notlike.matches(mapOf("noname" to "yes")))
		assertTrue(notlike.matches(mapOf("noname" to "no")))
		assertTrue(notlike.matches(mapOf()))
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

		assertTrue(eq.matches(mapOf("name" to "yes")))
		assertTrue(eq.matches(mapOf("lame" to "yos")))
		assertFalse(eq.matches(mapOf("lame" to "no")))
		assertFalse(eq.matches(mapOf("good" to "yes")))
		assertFalse(eq.matches(mapOf("neme" to "no")))
		assertFalse(eq.matches(mapOf("names" to "yess")))
		assertFalse(eq.matches(mapOf()))
	}

	@Test fun `to string`() {
		val eq = RegexKeyRegexValueFilter(".ame", "y.s")
		assertEquals("~'^.ame$' ~ '^y.s$'", eq.toOverpassQLString())
	}
}
