package de.westnordost.streetcomplete.data.osm.tql

import org.junit.Test

import de.westnordost.osmapi.map.data.BoundingBox

import org.junit.Assert.*
import java.text.ParseException

/** Integration test for the filter parser, filter expression and creator, the whole way from parsing
 * the tag filters expression to returning it as a OQL string. More convenient this way since the
 * easiest way to create a filter expressions is to parse it from string.  */
class FiltersParserAndOverpassQueryCreatorTest {
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
        check("nodes, ways with shop", "node[shop]->.n1;way[shop]->.w1;(.n1;.w1;);")
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

    @Test fun `fail if tag key quotation marks not closed`() {
        shouldFail("nodes with \"highway = residential or bla")
    }

    @Test fun `fail if tag value quotation marks not closed`() {
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

    @Test fun `fail if bracket not closed`() {
        shouldFail("nodes with (highway")
    }

    @Test fun `failed if too many brackets closed`() {
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

    @Test fun and() {
        check("nodes with highway and name", "node[highway][name];")
    }

    @Test fun `two and`() {
        check("nodes with highway and name and ref", "node[highway][name][ref];")
    }

    @Test fun or() {
        check("nodes with highway or name",
            """
            node[highway]->.n1;
            node[name]->.n2;
            (.n1;.n2;);
            """)
    }

    @Test fun `two or`() {
        check("nodes with highway or name or ref",
            """
            node[highway]->.n1;
            node[name]->.n2;
            node[ref]->.n3;
            (.n1;.n2;.n3;);
            """)
    }

    @Test fun `or as first child in and`() {
        check(
            "nodes with (highway or railway) and name and ref",
            """
            node[highway]->.n1;
            node[railway]->.n2;
            (.n2;.n3;)->.n3;
            node.n3[name][ref];
            """
        )
    }

    @Test fun `or as last child in and`() {
        check(
            "nodes with name and ref and (highway or railway)",
            """
            node[name][ref]->.n1;
            node.n1[highway]->.n2;
            node.n1[railway]->.n3;
            (.n2;.n3;);
            """
        )
    }

    @Test fun `or in the middle of and`() {
        check(
            "nodes with name and (highway or railway) and ref",
            """
            node[name]->.n1;
            node.n1[highway]->.n2;
            node.n1[railway]->.n3;
            (.n2;.n3;)->.n1;
            node.n1[ref];
            """
        )
    }

    @Test fun `and as first child in or`() {
        check(
            "nodes with (name and waterway) or highway or railway",
            """
            node[name][waterway]->.n1;
            node[highway]->.n2;
            node[railway]->.n3;
            (.n1;.n2;.n3;);
            """
        )
    }

    @Test fun `and as last child in or`() {
        check(
            "nodes with highway or railway or (name and waterway)",
            """
            node[highway]->.n1;
            node[railway]->.n2;
            node[name][waterway]->.n3;
            (.n1;.n2;.n3;);
            """
        )
    }

    @Test fun `and in the middle of or`() {
        check(
            "nodes with highway or (name and waterway) or railway",
            """
            node[highway]->.n1;
            node[name][waterway]->.n2;
            node[railway]->.n3;
            (.n1;.n2;.n3;);
            """
        )
    }

    @Test fun `and in or in and`() {
        check(
            "nodes with name and (highway and ref or waterway) and width",
            """
            node[name]->.n1;
            node.n1[highway][ref]->.n2;
            node.n1[waterway]->.n3;
            (.n2;.n3;)->.n1;
            node.n1[width];
            """
        )
    }

    @Test fun `and in or in and in or`() {
        check(
            "nodes with waterway or (highway and (noname or (name and ref)) and width)",
            """
            node[waterway]->.n1;
            node[highway]->.n2;
            node.n2[noname]->.n3;
            node.n2[name][ref]->.n4;
            (.n3;.n4;)->.n2;
            node.n2[width]->.n2;
            (.n1;.n2;);
            """
        )
    }

    @Test fun boundingBox() {
        val bbox = BoundingBox(0.0, 0.0, 5.0, 10.0)
        check("nodes", "[bbox:0,0,5,10];node;", bbox)
        check("nodes with highway", "[bbox:0,0,5,10];node[highway];", bbox)
        check(
            "nodes with highway or railway",
            """
            [bbox:0,0,5,10];
            node[highway]->.n1;
            node[railway]->.n2;
            (.n1;.n2;);
            """,
            bbox
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
	        """
            [bbox:0,0,5,10];
            node[highway]->.n2;
            node[railway]->.n3;
            (.n2;.n3;)->.n1;
            way[highway]->.w2;
            way[railway]->.w3;
            (.w2;.w3;)->.w1;
            (.n1;.w1;);
            """,
            bbox
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
        assertEquals(
            output.replace("\n","").replace(" ",""),
            expr.toOverpassQLString(bbox).replace("\n","").replace(" ",""))
    }
}
