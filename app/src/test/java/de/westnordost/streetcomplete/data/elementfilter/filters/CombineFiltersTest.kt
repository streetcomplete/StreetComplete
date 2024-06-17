package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.testutils.node
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.every
import io.mockative.mock
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CombineFiltersTest {

    // TODO this is causing a compile problem
    // @Mock private lateinit var bla: ElementFilter

    @Test fun `does not match if one doesn't match`() {
        val f1: ElementFilter = mock(classOf<ElementFilter>())
        every { f1.matches(any()) }.returns(true)
        val f2: ElementFilter = mock(classOf<ElementFilter>())
        every { f2.matches(any()) }.returns(false)
        assertFalse(CombineFilters(f1, f2).matches(node()))
    }

    @Test fun `does match if all match`() {
        val f1: ElementFilter = mock(classOf<ElementFilter>())
        every { f1.matches(any()) }.returns(true)
        val f2: ElementFilter = mock(classOf<ElementFilter>())
        every { f2.matches(any()) }.returns(true)
        assertTrue(CombineFilters(f1, f2).matches(node()))
    }
}
