package de.westnordost.streetcomplete.data.elementfilter

import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ElementFiltersParserTest {

    @Test fun `fail if no space after or before and or`() {
        shouldFail("shop andfail")
        shouldFail("'shop'and fail")
    }

    @Test fun `fail on unknown like operator`() {
        shouldFail("~speed > 3")
    }

    @Test fun `fail on no number for comparison`() {
        shouldFail("speed > walk")
    }

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

    @Test fun `fail on dangling quote`() {
        shouldFail("shop = yes '")
        shouldFail("shop = yes \"")
    }

    @Test fun `fail on dangling prefix operator`() {
        shouldFail("shop = yes and !")
        shouldFail("shop = yes and ~")
    }

    @Test fun `fail if bracket not closed`() {
        shouldFail("nodes with (highway")
        shouldFail("nodes with (highway = service and (service = alley)")
    }

    @Test fun `fail if too many brackets closed`() {
        shouldFail("nodes with highway)")
        shouldFail("nodes with (highway = service))")
    }

    @Test fun `whitespaces do not matter for brackets`() {
        val tags = mapOf("shop" to "yes", "fee" to "yes")
        matchesTags(tags, "shop and((fee=yes))")
        matchesTags(tags, "shop and \t\n\t\n ( \t\n\t\n ( \n\t\n\t fee=yes \n\t\n\t ))")
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

    @Test fun `quotes for comparisons are not allowed`() {
        shouldFail("nodes with width >  '3'")
        shouldFail("nodes with width >= '3'")
        shouldFail("nodes with width < '3'")
        shouldFail("nodes with width <= '3'")
        shouldFail("nodes with date >  '2022-12-12'")
        shouldFail("nodes with date >= '2022-12-12'")
        shouldFail("nodes with date <  '2022-12-12'")
        shouldFail("nodes with date <= '2022-12-12'")
        shouldFail("nodes with date >  'today' + 3 years")
        shouldFail("nodes with date >= 'today + 3 years'")
        shouldFail("nodes with date <  'today +' 3 years")
        shouldFail("nodes with date <= 'today + 3' years")
        shouldFail("nodes with older '2022-12-12'")
        shouldFail("nodes with newer '2022-12-12'")
        shouldFail("nodes with lit older '2022-12-12'")
        shouldFail("nodes with lit newer '2022-12-12'")
    }

    @Test fun `tag negation not combinable with operator`() {
        shouldFail("nodes with !highway=residential")
        shouldFail("nodes with !highway!=residential")
        shouldFail("nodes with !highway~residential")
        shouldFail("nodes with !highway!~residential")
    }

    @Test fun `empty key and value`() {
        matchesTags(mapOf("" to ""), "'' = ''")
    }

    @Test fun `not key operator is parsed correctly`() {
        matchesTags(mapOf(), "!shop")
        matchesTags(mapOf(), "!  shop")
        notMatchesTags(mapOf("shop" to "yes"), "!shop")
    }

    @Test fun `not key like operator is parsed correctly`() {
        matchesTags(mapOf(), "!~...")
        matchesTags(mapOf(), "!~  ...")
        notMatchesTags(mapOf("abc" to "yes"), "!~...")
    }

    @Test fun `key like operator is parsed correctly`() {
        matchesTags(mapOf("abc" to "yes"), "~...")
        matchesTags(mapOf("abc" to "yes"), "~   ...")
        notMatchesTags(mapOf("ab" to "yes"), "~   ...")
    }

    @Test fun `tag like operator is parsed correctly`() {
        matchesTags(mapOf("abc" to "yes"), "~...~...")
        matchesTags(mapOf("abc" to "yes"), "~  ...  ~  ...")
        notMatchesTags(mapOf("abc" to "ye"), "~  ...  ~  ...")
        notMatchesTags(mapOf("ab" to "yes"), "~  ...  ~  ...")
    }

    @Test fun `older operator is parsed correctly`() {
        matchesTags(mapOf(), "older 2199-12-12")
        matchesTags(mapOf(), "older today +2 days")
        matchesTags(mapOf(), "older today  +3   years")
        matchesTags(mapOf(), "older today  +4   months")
        notMatchesTags(mapOf(), "older today -2 days ")
    }

    @Test fun `newer operator is parsed correctly`() {
        matchesTags(mapOf(), "newer 2021-12-12")
        matchesTags(mapOf(), "newer today -2 days")
        matchesTags(mapOf(), "newer today  -3   years")
        matchesTags(mapOf(), "newer today  -4   months")
        notMatchesTags(mapOf(), "newer today +2 days")
    }

    @Test fun `key operator is parsed correctly`() {
        matchesTags(mapOf("shop" to "yes"), "shop")
        notMatchesTags(mapOf("snop" to "yes"), "shop")
    }

    @Test fun `tag older operator is parsed correctly`() {
        matchesTags(mapOf("lit" to "yes"), "lit older 2199-12-12")
        matchesTags(mapOf("lit" to "yes"), "lit  older today +2 days")
        matchesTags(mapOf("lit" to "yes"), "lit  older today  +3   years")
        matchesTags(mapOf("lit" to "yes"), "lit  older today  +4   months")
        notMatchesTags(mapOf("lit" to "yes"), "lit older today -2 days ")
    }

    @Test fun `tag newer operator is parsed correctly`() {
        matchesTags(mapOf("lit" to "yes"), "lit newer 2021-12-12")
        matchesTags(mapOf("lit" to "yes"), "lit  newer today -2 days")
        matchesTags(mapOf("lit" to "yes"), "lit  newer today  -3   years")
        matchesTags(mapOf("lit" to "yes"), "lit  newer today  -4   months")
        notMatchesTags(mapOf("lit" to "yes"), "lit newer today +2 days ")
    }

    @Test fun `has tag operator is parsed correctly`() {
        matchesTags(mapOf("lit" to "yes"), "lit = yes")
        matchesTags(mapOf("lit" to "yes"), "lit=yes")
        matchesTags(mapOf("lit" to "yes"), "lit   =   yes")
        notMatchesTags(mapOf("lit" to "yesnt"), "lit = yes")
    }

    @Test fun `not has tag operator is parsed correctly`() {
        matchesTags(mapOf("lit" to "no"), "lit != yes")
        matchesTags(mapOf("lit" to "no"), "lit!=yes")
        matchesTags(mapOf("lit" to "no"), "lit   !=   yes")
        notMatchesTags(mapOf("lit" to "yes"), "lit   !=   yes")
    }

    @Test fun `has tag value like operator is parsed correctly`() {
        matchesTags(mapOf("lit" to "yes"), "lit ~ ...")
        matchesTags(mapOf("lit" to "yes"), "lit~...")
        matchesTags(mapOf("lit" to "yes"), "lit   ~   ...")
        notMatchesTags(mapOf("lit" to "ye"), "lit   ~   ...")
    }

    @Test fun `not has tag value like operator is parsed correctly`() {
        matchesTags(mapOf("lit" to "ye"), "lit !~ ...")
        matchesTags(mapOf("lit" to "ye"), "lit!~...")
        matchesTags(mapOf("lit" to "ye"), "lit   !~   ...")
        notMatchesTags(mapOf("lit" to "yes"), "lit   !~   ...")
    }

    @Test fun `tag value greater than operator is parsed correctly`() {
        matchesTags(mapOf("width" to "5"), "width > 3")
        matchesTags(mapOf("width" to "5"), "width>3.0")
        matchesTags(mapOf("width" to "5"), "width   >   3")
        notMatchesTags(mapOf("width" to "3"), "width   >   3")
        matchesTags(mapOf("width" to "0.4"), "width>0.3")
        matchesTags(mapOf("width" to ".4"), "width>.3")
        notMatchesTags(mapOf("width" to ".3"), "width>.3")
    }

    @Test fun `tag value greater or equal than operator is parsed correctly`() {
        matchesTags(mapOf("width" to "3"), "width >= 3")
        matchesTags(mapOf("width" to "3"), "width>=3.0")
        matchesTags(mapOf("width" to "3"), "width   >=   3")
        notMatchesTags(mapOf("width" to "2"), "width   >=   3")
        matchesTags(mapOf("width" to "0.3"), "width>=0.3")
        matchesTags(mapOf("width" to ".3"), "width>=.3")
        notMatchesTags(mapOf("width" to ".2"), "width>=.3")
    }

    @Test fun `tag value less than operator is parsed correctly`() {
        matchesTags(mapOf("width" to "2"), "width < 3")
        matchesTags(mapOf("width" to "2"), "width<3.0")
        matchesTags(mapOf("width" to "2"), "width   <   3")
        notMatchesTags(mapOf("width" to "3"), "width   <   3")
        matchesTags(mapOf("width" to "0.2"), "width<0.3")
        matchesTags(mapOf("width" to ".2"), "width<.3")
        notMatchesTags(mapOf("width" to ".3"), "width<.3")
    }

    @Test fun `tag value less or equal than operator is parsed correctly`() {
        matchesTags(mapOf("width" to "3"), "width <= 3")
        matchesTags(mapOf("width" to "3"), "width<=3.0")
        matchesTags(mapOf("width" to "3"), "width   <=   3")
        notMatchesTags(mapOf("width" to "4"), "width   <=   3")
        matchesTags(mapOf("width" to "0.3"), "width<=0.3")
        matchesTags(mapOf("width" to ".3"), "width<=.3")
        notMatchesTags(mapOf("width" to ".4"), "width<=.3")
    }

    @Test fun `comparisons with dates`() {
        matchesTags(mapOf("date" to "2022-12-12"), "date <= 2022-12-12")
        notMatchesTags(mapOf("date" to "2022-12-13"), "date <= 2022-12-12")

        matchesTags(mapOf("date" to "2022-12-12"), "date >= 2022-12-12")
        notMatchesTags(mapOf("date" to "2022-12-11"), "date >= 2022-12-12")

        matchesTags(mapOf("date" to "2022-12-11"), "date < 2022-12-12")
        notMatchesTags(mapOf("date" to "2022-12-12"), "date < 2022-12-12")

        matchesTags(mapOf("date" to "2022-12-13"), "date > 2022-12-12")
        notMatchesTags(mapOf("date" to "2022-12-12"), "date > 2022-12-12")
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

    @Test fun and() {
        val expr = "a and b"
        matchesTags(mapOfKeys("a", "b"), expr)
        notMatchesTags(mapOfKeys("a"), expr)
        notMatchesTags(mapOfKeys("b"), expr)
    }

    @Test fun `two and`() {
        val expr = "a and b and c"
        matchesTags(mapOfKeys("a", "b", "c"), expr)
        notMatchesTags(mapOfKeys("a", "b"), expr)
        notMatchesTags(mapOfKeys("a", "c"), expr)
        notMatchesTags(mapOfKeys("b", "c"), expr)
    }

    @Test fun or() {
        val expr = "a or b"
        matchesTags(mapOfKeys("b"), expr)
        matchesTags(mapOfKeys("a"), expr)
        notMatchesTags(mapOfKeys(), expr)
    }

    @Test fun `two or`() {
        val expr = "a or b or c"
        matchesTags(mapOfKeys("c"), expr)
        matchesTags(mapOfKeys("b"), expr)
        matchesTags(mapOfKeys("a"), expr)
        notMatchesTags(mapOfKeys(), expr)
    }

    @Test fun `or as first child in and`() {
        val expr = "(a or b) and c"
        matchesTags(mapOfKeys("c", "a"), expr)
        matchesTags(mapOfKeys("c", "b"), expr)
        notMatchesTags(mapOfKeys("b"), expr)
        notMatchesTags(mapOfKeys("a"), expr)
    }

    @Test fun `or as last child in and`() {
        val expr = "c and (a or b)"
        matchesTags(mapOfKeys("c", "a"), expr)
        matchesTags(mapOfKeys("c", "b"), expr)
        notMatchesTags(mapOfKeys("b"), expr)
        notMatchesTags(mapOfKeys("a"), expr)
    }

    @Test fun `or in the middle of and`() {
        val expr = "c and (a or b) and d"
        matchesTags(mapOfKeys("c", "d", "a"), expr)
        matchesTags(mapOfKeys("c", "d", "b"), expr)
        notMatchesTags(mapOfKeys("b"), expr)
        notMatchesTags(mapOfKeys("a"), expr)
    }

    private fun mapOfKeys(vararg key: String) =
        key.mapIndexed { i, s -> s to i.toString() }.toMap()

    @Test fun `and as first child in or`() {
        val expr = "a and b or c"
        matchesTags(mapOfKeys("a", "b"), expr)
        matchesTags(mapOfKeys("c"), expr)
        notMatchesTags(mapOfKeys("a"), expr)
        notMatchesTags(mapOfKeys("b"), expr)
    }

    @Test fun `and as last child in or`() {
        val expr = "c or a and b"
        matchesTags(mapOfKeys("a", "b"), expr)
        matchesTags(mapOfKeys("c"), expr)
        notMatchesTags(mapOfKeys("a"), expr)
        notMatchesTags(mapOfKeys("b"), expr)
    }

    @Test fun `and in the middle of or`() {
        val expr = "c or a and b or d"
        matchesTags(mapOfKeys("a", "b"), expr)
        matchesTags(mapOfKeys("c"), expr)
        matchesTags(mapOfKeys("d"), expr)
        notMatchesTags(mapOfKeys("a"), expr)
        notMatchesTags(mapOfKeys("b"), expr)
    }

    @Test fun `and in or in and`() {
        val expr = "a and (b and c or d)"
        matchesTags(mapOfKeys("a", "d"), expr)
        matchesTags(mapOfKeys("a", "b", "c"), expr)
        notMatchesTags(mapOfKeys("a"), expr)
        notMatchesTags(mapOfKeys("b", "c"), expr)
        notMatchesTags(mapOfKeys("d"), expr)
    }

    @Test fun `and in or in and in or`() {
        val expr = "a or (b and (c or (d and e)))"
        matchesTags(mapOfKeys("a"), expr)
        matchesTags(mapOfKeys("b", "c"), expr)
        matchesTags(mapOfKeys("b", "d", "e"), expr)
        notMatchesTags(mapOfKeys(), expr)
        notMatchesTags(mapOfKeys("b"), expr)
        notMatchesTags(mapOfKeys("c"), expr)
        notMatchesTags(mapOfKeys("b", "d"), expr)
        notMatchesTags(mapOfKeys("b", "e"), expr)
    }

    @Test fun `and in bracket followed by another and`() {
        val expr = "(a or (b and c)) and d"
        matchesTags(mapOfKeys("a", "d"), expr)
        matchesTags(mapOfKeys("b", "c", "d"), expr)
        notMatchesTags(mapOfKeys("a"), expr)
        notMatchesTags(mapOfKeys("d"), expr)
        notMatchesTags(mapOfKeys("b", "c"), expr)
    }

    private fun shouldFail(input: String) {
        try {
            input.toElementFilterExpression()
            fail()
        } catch (ignore: ParseException) {}
    }

    private fun parse(input: String): ElementFilterExpression =
        input.toElementFilterExpression()

    private fun matchesTags(tags: Map<String, String>, input: String) =
        assertTrue(("nodes with $input").toElementFilterExpression().matches(node(tags = tags)))

    private fun notMatchesTags(tags: Map<String, String>, input: String) =
        assertFalse(("nodes with $input").toElementFilterExpression().matches(node(tags = tags)))
}
