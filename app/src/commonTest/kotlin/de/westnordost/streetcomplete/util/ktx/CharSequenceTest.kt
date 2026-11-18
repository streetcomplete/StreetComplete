package de.westnordost.streetcomplete.util.ktx

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CharSequenceTest {
    @Test fun isCodePointCountSmallerOrEqualThan() {
        assertTrue("".isCodePointCountSmallerOrEqualThan(0))
        assertTrue("".isCodePointCountSmallerOrEqualThan(1))

        assertFalse("a".isCodePointCountSmallerOrEqualThan(0))
        assertTrue("a".isCodePointCountSmallerOrEqualThan(1))
        assertTrue("a".isCodePointCountSmallerOrEqualThan(2))

        val brokkoli = "\uD83E\uDD66" // 🥦
        assertFalse(brokkoli.isCodePointCountSmallerOrEqualThan(0))
        assertTrue(brokkoli.isCodePointCountSmallerOrEqualThan(1))
        assertTrue(brokkoli.isCodePointCountSmallerOrEqualThan(2))

        assertTrue("abc".isCodePointCountSmallerOrEqualThan(3))
        assertFalse("abc".isCodePointCountSmallerOrEqualThan(2))

        val surfing = "\uD83C\uDF0A\uD83C\uDFC4\uD83C\uDF0A oh yeah!" // 🌊🏄🌊 oh yeah!
        assertFalse(surfing.isCodePointCountSmallerOrEqualThan(11))
        assertTrue(surfing.isCodePointCountSmallerOrEqualThan(12))
    }
}
