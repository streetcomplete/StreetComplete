package de.westnordost.streetcomplete.data.elementfilter

import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ElementFiltersParserTest {
    @Test fun `whitespaces do not matter for element declaration`() {
        val elements = listOf(node(), way(), rel())
        for (e in elements) {
            assertTrue(parse("nodes, ways,  relations").matches(e))
            assertTrue(parse("  nodes,ways,relations  ").matches(e))
            assertTrue(parse("nodes ,ways  ,relations").matches(e))
            assertTrue(parse("\tnodes\n,\t\tways\n\n,relations").matches(e))
        }
    }

    @Test fun `order does not matter for element declaration`() {
        val elements = listOf(node(), way(), rel())

        for (e in elements) {
            assertTrue(parse("relations, ways, nodes").matches(e))
            assertTrue(parse("relations, nodes, ways").matches(e))
        }
    }

    @Test fun `fail if invalid element declaration in front`() {
        shouldFail("butter")
    }

    @Test fun `fail if element declaration in front is duplicate`() {
        shouldFail("nodes, nodes")
    }

    @Test fun `fail if any element declaration in front is invalid`() {
        shouldFail("nodes, butter")
    }

    @Test fun `fail if no whitespace between reserved words`() {
        shouldFail("nodeswith")
        shouldFail("nodes withhighway")
        parse("nodes with(highway)")
    }

    @Test fun `fail if tag key is like reserved word`() {
        shouldFail("nodes with with")
        shouldFail("nodes with or")
        shouldFail("nodes with and")
        shouldFail("nodes with older")
        shouldFail("nodes with with = abc")
        shouldFail("nodes with or = abc")
        shouldFail("nodes with and = abc")
    }

    @Test fun `tag key like reserved word in quotation marks is ok`() {
        val tags = mapOf("with" to "with")
        matchesTags(tags, "'with'")
        matchesTags(tags, "'with'='with'")
    }

    @Test fun `quotes are optional`() {
        val tags = mapOf("shop" to "yes")
        matchesTags(tags, "shop")
        matchesTags(tags, "'shop'")
        matchesTags(tags, "\"shop\"")
    }

    @Test fun `quoting empty string`() {
        matchesTags(mapOf("shop" to ""), "shop = ''")
    }

    @Test fun `escaping quotes`() {
        matchesTags(mapOf("shop\"" to "yes"), "\"shop\\\"\"")
        matchesTags(mapOf("shop'" to "yes"), "'shop\\\''")
        matchesTags(mapOf("shop" to "yes\""), "shop = \"yes\\\"\"")
        matchesTags(mapOf("shop" to "yes'"), "shop = 'yes\\\''")
        matchesTags(mapOf("sh'op" to "yes'"), "sh\\'op = yes\\'")
    }

    @Test fun `unquoted tag may start with reserved word`() {
        matchesTags(mapOf("withdrawn" to "with"), "withdrawn = with")
        matchesTags(mapOf("orchard" to "or"), "orchard = or")
        matchesTags(mapOf("android" to "and"), "android = and")
    }

    @Test fun `tag key with quotation marks is ok`() {
        matchesTags(
            mapOf("highway = residential or bla" to "yes"),
            "\"highway = residential or bla\""
        )
    }

    @Test fun `tag value with quotation marks is ok`() {
        matchesTags(
            mapOf("highway" to "residential or bla"),
            "highway = \"residential or bla\""
        )
    }

    @Test fun `fail if tag key quotation marks not closed`() {
        shouldFail("nodes with \"highway = residential or bla")
    }

    @Test fun `fail if tag value quotation marks not closed`() {
        shouldFail("nodes with highway = \"residential or bla")
    }

    @Test fun `whitespaces around tag key do not matter`() {
        val tags = mapOf("shop" to "yes")

        matchesTags(tags, "shop")
        matchesTags(tags, " \t\n\t\n shop \t\n\t\n ")
        matchesTags(tags, " \t\n\t\n ( \t\n\t\n shop \t\n\t\n ) \t\n\t\n ")
    }

    @Test fun `whitespaces around tag value do not matter`() {
        val tags = mapOf("shop" to "yes")

        matchesTags(tags, "shop=yes")
        matchesTags(tags, "shop \t\n\t\n = \t\n\t\n yes \t\n\t\n ")
        matchesTags(tags, " \t\n\t\n ( \t\n\t\n shop \t\n\t\n = \t\n\t\n yes \t\n\t\n ) \t\n\t\n ")
    }

    @Test fun `whitespaces in tag do matter`() {
        val tags = mapOf(" \t\n\t\n shop \t\n\t\n " to " \t\n\t\n yes \t\n\t\n ")
        matchesTags(tags, "\" \t\n\t\n shop \t\n\t\n \" = \" \t\n\t\n yes \t\n\t\n \"")
    }

    @Test fun `fail on dangling operator`() {
        shouldFail("nodes with highway=")
    }

    @Test fun `fail on dangling boolean operator`() {
        shouldFail("nodes with highway and")
        shouldFail("nodes with highway or ")
    }

    @Test fun `fail if bracket not closed`() {
        shouldFail("nodes with (highway")
    }

    @Test fun `fail if too many brackets closed`() {
        shouldFail("nodes with highway)")
    }

    @Test fun `fail on unknown thing after tag`() {
        shouldFail("nodes with highway what is this")
    }

    @Test fun `fail if neither a number nor a date is used for comparison`() {
        shouldFail("nodes with width > x")
        shouldFail("nodes with width >=x ")
        shouldFail("nodes with width < x")
        shouldFail("nodes with width <=x")
    }

    @Test fun `tag negation not combinable with operator`() {
        shouldFail("nodes with !highway=residential")
        shouldFail("nodes with !highway!=residential")
        shouldFail("nodes with !highway~residential")
        shouldFail("nodes with !highway!~residential")
    }

    @Test fun `comparisons work with units`() {
        matchesTags(mapOf("maxspeed" to "30.1 mph"), "maxspeed > 30mph")
        matchesTags(mapOf("maxspeed" to "48.3"), "maxspeed > 30mph")
        matchesTags(mapOf("maxspeed" to "48.3 km/h"), "maxspeed > 30mph")

        notMatchesTags(mapOf("maxspeed" to "30.0 mph"), "maxspeed > 30mph")
        notMatchesTags(mapOf("maxspeed" to "48.2"), "maxspeed > 30mph")
        notMatchesTags(mapOf("maxspeed" to "48.2 km/h"), "maxspeed > 30mph")
    }

    @Test fun `comparisons work with extra special units`() {
        matchesTags(mapOf("maxwidth" to "4 ft 7 in"), "maxwidth > 4'6\"")
        matchesTags(mapOf("maxwidth" to "4'7\""), "maxwidth > 4'6\"")
        matchesTags(mapOf("maxwidth" to "1.4 m"), "maxwidth > 4'6\"")
        matchesTags(mapOf("maxwidth" to "1.4m"), "maxwidth > 4'6\"")
        matchesTags(mapOf("maxwidth" to "1.4"), "maxwidth > 4'6\"")

        notMatchesTags(mapOf("maxwidth" to "4'6\""), "maxwidth > 4'6\"")
        notMatchesTags(mapOf("maxwidth" to "1.3"), "maxwidth > 4'6\"")
    }

    // todo check expressions...

    private fun shouldFail(input: String) {
        try {
            input.toElementFilterExpression()
            fail()
        } catch (ignore: ParseException) {}
    }

    private fun parse(input: String): ElementFilterExpression =
        input.toElementFilterExpression()

    private fun matchesTags(tags: Map<String,String>, input: String) =
        assertTrue(("nodes with $input").toElementFilterExpression().matches(node(tags = tags)))

    private fun notMatchesTags(tags: Map<String,String>, input: String) =
        assertFalse(("nodes with $input").toElementFilterExpression().matches(node(tags = tags)))
}
