package de.westnordost.streetcomplete.testutils

import kotlin.test.assertContains
import kotlin.test.assertEquals

fun <T> assertSetsAreEqual(expected: Set<T>, actual: Set<T>) {
    assertEquals(expected.size, actual.size)
    expected.forEach{ assertContains(actual, it)}
    actual.forEach{ assertContains(expected, it)}
}
