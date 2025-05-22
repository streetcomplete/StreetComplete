package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.testutils.node
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CombineFiltersTest {

    @Test fun `does not match if one doesn't match`() {
        val f1 = HasKey("a")
        val f2 = HasKey("b")
        val n = node(tags = mapOf("a" to "x"))
        assertFalse(CombineFilters(f1, f2).matches(n))
    }

    @Test fun `does match if all match`() {
        val f1 = HasKey("a")
        val f2 = HasKey("b")
        val n = node(tags = mapOf("a" to "x", "b" to "y"))
        assertTrue(CombineFilters(f1, f2).matches(n))
    }
}
