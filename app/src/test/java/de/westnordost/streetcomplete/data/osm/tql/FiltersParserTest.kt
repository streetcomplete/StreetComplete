package de.westnordost.streetcomplete.data.osm.tql

import org.junit.Test

import de.westnordost.osmapi.map.data.BoundingBox

import org.junit.Assert.*
import java.text.ParseException

/** Integration test for the filter parser and the filter expression, the whole way from parsing
 * the tag filters expression to returning it as a OQL string. More convenient this way since the
 * easiest way to create a filter expressions is to parse it from string.  */
class FiltersParserTest {
    @Test fun node() {
        check("nodes", "node;")
        check("NODES", "node;")
    }

    @Test fun way() {
        check("ways", "way;")
        check("WAYS", "way;")
    }

    @Test fun relation() {
        check("relations", "rel;")
        check("RELATIONS", "rel;")
    }

    @Test fun `multiple element types`() {
        check("nodes, ways, relations", "nwr;")
        check("nodes ,ways", "(node;way;);")
        check("nodes , ways", "(node;way;);")
    }

    @Test fun `any quote`() {
        check("nodes with 'shop'", "node[shop];")
        check("nodes with \"shop\"", "node[shop];")
        check("nodes with \"shoppin'\"", "node['shoppin\''];")
        check("nodes with '\"shop\"ping'", "node['\"shop\"ping'];")
    }

    @Test fun `multiple element types with tag`() {
        check("nodes, ways, relations with shop", "nwr[shop];")
        check("nodes, ways with shop", "(node[shop];way[shop];);")
    }

    @Test fun `whitespace in front is okay`() {
        check("\t\n nodes", "node;")
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
    }

    @Test fun `fail if tag key is like reserved word`() {
        shouldFail("nodes with with")
        shouldFail("nodes with around")
        shouldFail("nodes with or")
        shouldFail("nodes with and")
    }

    @Test fun `tag key like reserved word in quotation marks is ok`() {
        check("nodes with \"with\"", "node[with];")
        check("nodes with \"with\"=\"with\"", "node[with = with];")
    }

    @Test fun `tag key with quotation marks is ok`() {
        check(
            "nodes with \"highway = residential or bla\"",
            "node['highway = residential or bla'];"
        )
    }

    @Test fun `tag value with quotation marks is ok`() {
        check(
            "nodes with highway = \"residential or bla\"",
            "node[highway = 'residential or bla'];"
        )
    }

    @Test fun `tag value garbage`() {
        check("nodes with highway = §$%&%/??", "node[highway = '§$%&%/??'];")
    }

    @Test fun `tag key quotation marks not closed`() {
        shouldFail("nodes with \"highway = residential or bla")
    }

    @Test fun `tag value quotation marks not closed`() {
        shouldFail("nodes with highway = \"residential or bla")
    }

    @Test fun `tag key`() {
        val expect = "node[highway];"

        check("nodes with highway", expect)
        check("nodes with(highway)", expect)
        check("nodes with (highway)", expect)
        check("nodes with ( highway)", expect)
        check("nodes with( highway)", expect)
        check("nodes with(highway )", expect)
        check("nodes with(highway ) ", expect)
        check("nodes with(highway) ", expect)
    }

    @Test fun `fail on dangling operator`() {
        shouldFail("nodes with highway=")
    }

    @Test fun `fail on dangling boolean operator`() {
        shouldFail("nodes with highway and")
        shouldFail("nodes with highway or ")
    }

    @Test fun `fail when bracket not closed`() {
        shouldFail("nodes with (highway")
    }

    @Test fun `failed when too many brackets closed`() {
        shouldFail("nodes with highway)")
    }

    @Test fun `fail on unknown thing after tag`() {
        shouldFail("nodes with highway what is this")
    }

    @Test fun `tag negation`() {
        check("nodes with !highway", "node[!highway];")
    }

    @Test fun `tag operator whitespaces allowed everywhere`() {
        val expect = "node[highway = residential];"

        check("nodes with highway=residential", expect)
        check("nodes with highway =residential", expect)
        check("nodes with highway= residential", expect)
        check("nodes with highway = residential", expect)
    }

    @Test fun `tag operator`() {
        check("nodes with highway=residential", "node[highway = residential];")
        check("nodes with highway!=residential", "node[highway != residential];")
        check("nodes with highway~residential", "node[highway ~ '^residential$'];")
        check("nodes with ~highway~residential", "node[~'^highway$' ~ '^residential$'];")
        check("nodes with highway!~residential", "node[highway !~ '^residential$'];")
    }

    @Test fun `tag negation not combinable with operator`() {
        shouldFail("nodes with !highway=residential")
        shouldFail("nodes with !highway!=residential")
        shouldFail("nodes with !highway~residential")
        shouldFail("nodes with !highway!~residential")
    }

    @Test fun `two tags`() {
        check("nodes with highway and name", "node[highway][name];")
        check("nodes with highway or name", "(node[highway];node[name];);")
    }

    @Test fun `or in and`() {
        check(
            "nodes with(highway or railway)and name",
            "(node[highway][name];node[railway][name];);"
        )
    }

    @Test fun boundingBox() {
        val bbox = BoundingBox(0.0, 0.0, 5.0, 10.0)
        check("nodes", "[bbox:0,0,5,10];node;", bbox)
        check("nodes with highway", "[bbox:0,0,5,10];node[highway];", bbox)
        check(
            "nodes with highway or railway",
            "[bbox:0,0,5,10];(node[highway];node[railway];);", bbox
        )
    }

    @Test fun `boundingBox with multiple element types`() {
        val bbox = BoundingBox(0.0, 0.0, 5.0, 10.0)

        check("nodes, ways, relations", "[bbox:0,0,5,10];nwr;", bbox)
        check(
            "nodes, ways, relations with highway",
            "[bbox:0,0,5,10];nwr[highway];", bbox
        )

        check(
            "nodes, ways with highway or railway",
	        "[bbox:0,0,5,10];(node[highway];node[railway];way[highway];way[railway];);", bbox
        )
    }

    private fun shouldFail(input: String) {
        try {
            FiltersParser().parse(input)
            fail()
        } catch (ignore: ParseException) {
        }
    }

    private fun check(input: String, output: String, bbox: BoundingBox? = null) {
        val expr = FiltersParser().parse(input)
        assertEquals(output, expr.toOverpassQLString(bbox))
    }
}
