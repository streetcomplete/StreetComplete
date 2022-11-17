package de.westnordost.streetcomplete.osm

import org.junit.Assert.*
import org.junit.Test

class TagsKtTest {

    @Test fun `expand sides`() {
        val tags = Tags(mapOf("a:both" to "blub", "a" to "norg"))
        tags.expandSides("a", false)
        assertEquals(
            mapOf("a:left" to "blub", "a:right" to "blub", "a" to "norg"),
            tags.toMap()
        )
    }

    @Test fun `expand sides with bare tag`() {
        val tags = Tags(mapOf("a" to "blub"))
        tags.expandSides("a", true)
        assertEquals(
            mapOf("a:left" to "blub", "a:right" to "blub"),
            tags.toMap()
        )
    }

    @Test fun `when expanding sides, explicit tag takes precedence over bare tag`() {
        val tags = Tags(mapOf("a" to "blub", "a:both" to "burg"))
        tags.expandSides("a", true)
        assertEquals(
            mapOf("a:left" to "burg", "a:right" to "burg"),
            tags.toMap()
        )
    }

    @Test fun `when expanding sides, does not overwrite left and right tags`() {
        val tags = Tags(mapOf("a:both" to "gah", "a:left" to "le", "a:right" to "ri"))
        tags.expandSides("a", true)
        assertEquals(
            mapOf("a:left" to "le", "a:right" to "ri"),
            tags.toMap()
        )
    }

    @Test fun `don't expand unrelated tags sides`() {
        val tags = Tags(mapOf("abc:both" to "blub"))
        tags.expandSides("a", false)
        assertEquals(
            mapOf("abc:both" to "blub"),
            tags.toMap()
        )
    }

    @Test fun `merge sides`() {
        val tags = Tags(mapOf("a:left" to "blub", "a:right" to "blub"))
        tags.mergeSides("a")
        assertEquals(
            mapOf("a:both" to "blub"),
            tags.toMap()
        )
    }

    @Test fun `merge sides only merges if both are the same`() {
        val tags = Tags(mapOf("a:left" to "blub"))
        tags.mergeSides("a")
        assertEquals(
            mapOf("a:left" to "blub"),
            tags.toMap()
        )

        val tags2 = Tags(mapOf("a:left" to "blub", "a:right" to "blab"))
        tags2.mergeSides("a")
        assertEquals(
            mapOf("a:left" to "blub", "a:right" to "blab"),
            tags2.toMap()
        )
    }

    @Test fun `merge sides overwrites previous both tag`() {
        val tags = Tags(mapOf("a:left" to "blub", "a:right" to "blub", "a:both" to "old"))
        tags.mergeSides("a")
        assertEquals(
            mapOf("a:both" to "blub"),
            tags.toMap()
        )
    }
}
