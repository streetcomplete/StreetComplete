package de.westnordost.streetcomplete.data.osm.tql

import org.junit.Test

import org.junit.Assert.*

class HasKeyTest {

    @Test fun matches() {
        val key = HasKey("name")

        assertTrue(key.matches(mapOf("name" to "yes")))
        assertTrue(key.matches(mapOf("name" to "no")))
        assertFalse(key.matches(mapOf("neme" to "no")))
        assertFalse(key.matches(mapOf()))
    }

    @Test fun toOverpassQLString() {
        assertEquals("name", HasKey("name").toOverpassQLString())
        assertEquals("'name:old'", HasKey("name:old").toOverpassQLString())
    }
}

class NotHasKeyTest {

    @Test fun matches() {
        val key = NotHasKey("name")

        assertFalse(key.matches(mapOf("name" to "yes")))
        assertFalse(key.matches(mapOf("name" to "no")))
        assertTrue(key.matches(mapOf("neme" to "no")))
        assertTrue(key.matches(mapOf()))
    }

    @Test fun toOverpassQLString() {
        assertEquals("!name", NotHasKey("name").toOverpassQLString())
        assertEquals("!'name:old'", NotHasKey("name:old").toOverpassQLString())
    }
}

class HasTagTest {

    @Test fun matches() {
        val eq = HasTag("highway", "residential")

        assertTrue(eq.matches(mapOf("highway" to "residential")))
        assertFalse(eq.matches(mapOf("highway" to "residental")))
        assertFalse(eq.matches(mapOf("hipway" to "residential")))
        assertFalse(eq.matches(mapOf()))
    }

    @Test fun toOverpassQLString() {
        val eq = HasTag("highway", "residential")
        assertEquals("highway = residential", eq.toOverpassQLString())
    }
}

class NotHasTagTest {

    @Test fun matches() {
        val neq = NotHasTag("highway", "residential")

        assertFalse(neq.matches(mapOf("highway" to "residential")))
        assertTrue(neq.matches(mapOf("highway" to "residental")))
        assertTrue(neq.matches(mapOf("hipway" to "residential")))
        assertTrue(neq.matches(mapOf()))
    }

    @Test fun toOverpassQLString() {
        val neq = NotHasTag("highway", "residential")
        assertEquals("highway != residential", neq.toOverpassQLString())
    }
}

class HasTagValueLikeTest {

    @Test fun `matches like dot`() {
        val like = HasTagValueLike("highway", ".esidential")

        assertTrue(like.matches(mapOf("highway" to "residential")))
        assertTrue(like.matches(mapOf("highway" to "wesidential")))
        assertFalse(like.matches(mapOf("highway" to "rresidential")))
        assertFalse(like.matches(mapOf()))
    }

    @Test fun `matches like or`() {
        val like = HasTagValueLike("highway", "residential|unclassified")

        assertTrue(like.matches(mapOf("highway" to "residential")))
        assertTrue(like.matches(mapOf("highway" to "unclassified")))
        assertFalse(like.matches(mapOf("highway" to "blub")))
        assertFalse(like.matches(mapOf()))
    }

    @Test fun `groups values properly`() {
        val like = HasTagValueLike("highway", "residential|unclassified")

        assertEquals("highway ~ '^(residential|unclassified)$'",like.toOverpassQLString())
    }

    @Test fun `key value to string`() {
        val eq = HasTagValueLike("highway", ".*")
        assertEquals("highway ~ '^(.*)$'", eq.toOverpassQLString())
    }

}

class NotHasTagValueLikeTest {

    @Test fun `matches not like dot`() {
        val notlike = NotHasTagValueLike("highway", ".*")

        assertFalse(notlike.matches(mapOf("highway" to "anything")))
        assertTrue(notlike.matches(mapOf()))
    }

    @Test fun `matches not like or`() {
        val notlike = NotHasTagValueLike("noname", "yes")

        assertFalse(notlike.matches(mapOf("noname" to "yes")))
        assertTrue(notlike.matches(mapOf("noname" to "no")))
        assertTrue(notlike.matches(mapOf()))
    }

    @Test fun `groups values properly`() {
        val like = NotHasTagValueLike("highway", "residential|unclassified")

        assertEquals("highway !~ '^(residential|unclassified)$'",like.toOverpassQLString())
    }

    @Test fun `key not value to string`() {
        val neq = NotHasTagValueLike("highway", ".*")
        assertEquals("highway !~ '^(.*)$'", neq.toOverpassQLString())
    }
}

class HasTagLikeTest {

    @Test fun `matches regex key and value`() {
        val eq = HasTagLike(".ame", "y.s")

        assertTrue(eq.matches(mapOf("name" to "yes")))
        assertTrue(eq.matches(mapOf("lame" to "yos")))
        assertFalse(eq.matches(mapOf("lame" to "no")))
        assertFalse(eq.matches(mapOf("good" to "yes")))
        assertFalse(eq.matches(mapOf("neme" to "no")))
        assertFalse(eq.matches(mapOf("names" to "yess")))
        assertFalse(eq.matches(mapOf()))
    }

    @Test fun `to string`() {
        val eq = HasTagLike(".ame", "y.s")
        assertEquals("~'^(.ame)$' ~ '^(y.s)$'", eq.toOverpassQLString())
    }
}
