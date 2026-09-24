package de.westnordost.streetcomplete.data.elementfilter

import de.westnordost.streetcomplete.data.location.CurrentSeason
import de.westnordost.streetcomplete.testutils.node
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ContextFilterParserTest {
    @Test
    fun `fail on comparison of __season__`() {
        shouldFail("__season__ > test")
    }

    @Test
    fun `fail on age comparison of __season__`() {
        shouldFail("__season__ older test")
    }

    @Test fun `__season__ filter matches current season`() {
        val expr = "__season__ = " + CurrentSeason.getCurrentSeason
        matchesTags(expr)
    }

    @Test fun `__season__ filter does not match an unknown season`() {
        val expr = "__season__ = wombats"
        notMatchesTags(expr)
    }

    @Test fun `= __season__ value filter matches current season`() {
        val expr = "season = __season__"
        matchesTags(expr, mapOf("season" to CurrentSeason.getCurrentSeason))
    }

    @Test fun `!= __season__ value filter fails when equal to current season`() {
        val expr = "season != __season__"
        notMatchesTags(expr, mapOf("season" to CurrentSeason.getCurrentSeason))
    }

    @Test fun `list of all seasons always matches`() {
        val expr = "__season__ ~ winter|summer|autumn|spring"
        matchesTags(expr)
    }

    @Test fun `list of all seasons never matches`() {
        val expr = "__season__ !~ winter|summer|autumn|spring"
        notMatchesTags(expr)
    }

    @Test fun `__season__ filter fails on season that is different to the current one in the users timezone`() {
        var season = "winter"
        if (CurrentSeason.getCurrentSeason == "winter")
            season = "summer"
        val expr = "__season__ = $season"
        notMatchesTags(expr)
    }

    @Test fun `= __season__ value filter fails on different season`() {
        val expr = "season = __season__"
        var season = "winter"
        if (CurrentSeason.getCurrentSeason == "winter")
            season = "summer"
        notMatchesTags(expr, mapOf("season" to season))
    }

    private fun shouldFail(input: String) {
        assertFailsWith<ParseException> {
            input.toElementFilterExpression()
        }
    }

    private fun parse(input: String): ElementFilterExpression =
        input.toElementFilterExpression()

    private fun matchesTags(input: String, tags: Map<String, String> = emptyMap()) =
        assertTrue(("nodes with $input").toElementFilterExpression().matches(node(tags = tags)))

    private fun notMatchesTags(input: String, tags: Map<String, String> = emptyMap()) =
        assertFalse(("nodes with $input").toElementFilterExpression().matches(node(tags = tags)))
}
