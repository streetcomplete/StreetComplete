package de.westnordost.streetcomplete.data.elementfilter

import de.westnordost.streetcomplete.osm.toCheckDateString
import kotlin.test.Test
import kotlin.test.assertEquals

/** Integration test for the filter parser, filter expression and creator, the whole way from
 *  parsing the tag filters expression to returning it as a OQL string. More convenient this way
 *  since the easiest way to create a filter expressions is to parse it from string.  */
class ElementFiltersParserAndOverpassQueryCreatorTest {
    @Test fun node() {
        check("nodes", "node;")
    }

    @Test fun way() {
        check("ways", "way;")
    }

    @Test fun relation() {
        check("relations", "rel;")
    }

    @Test fun `multiple element types`() {
        check("nodes, ways, relations", "nwr;")
        check("nodes, ways", "nw;")
        check("relations, ways", "wr;")
    }

    @Test fun `multiple element types with tag`() {
        check("nodes, ways, relations with shop", "nwr[shop];")
        check("nodes, relations with shop", "node[shop]->.n1;rel[shop]->.r1;(.n1;.r1;);")
    }

    @Test fun `tag operator`() {
        check("nodes with highway=residential", "node[highway = residential];")
        check("nodes with highway!=residential", "node[highway != residential];")
        check("nodes with highway~residential", "node[highway ~ '^(residential)$'];")
        check("nodes with ~highway~residential", "node[~'^(highway)$' ~ '^(residential)$'];")
        check("nodes with highway!~residential", "node[highway !~ '^(residential)$'];")
        check("nodes with ~highway", "node[~'^(highway)$' ~ '.*'];")
        check("nodes with !~highway", "node[!~'^(highway)$' ~ '.*'];")
    }

    @Test fun `tag value comparison operator`() {
        check("nodes with width>5", "node[width](if:number(t['width']) > 5);")
        check("nodes with width>=5", "node[width](if:number(t['width']) >= 5);")
        check("nodes with width<5", "node[width](if:number(t['width']) < 5);")
        check("nodes with width<=5", "node[width](if:number(t['width']) <= 5);")
    }

    @Test fun `tag date comparison operator`() {
        check("nodes with check_date > 2000-11-11", "node[check_date](if:date(t['check_date']) > date('2000-11-11'));")
        check("nodes with check_date >= 2000-11-11", "node[check_date](if:date(t['check_date']) >= date('2000-11-11'));")
        check("nodes with check_date < 2000-11-11", "node[check_date](if:date(t['check_date']) < date('2000-11-11'));")
        check("nodes with check_date <= 2000-11-11", "node[check_date](if:date(t['check_date']) <= date('2000-11-11'));")
    }

    @Test fun `tag date comparison variants`() {
        check("nodes with check_date < 2000-11", "node[check_date](if:date(t['check_date']) < date('2000-11-01'));")

        val date = dateDaysAgo(0f).toCheckDateString()
        check("nodes with check_date < today", "node[check_date](if:date(t['check_date']) < date('$date'));")

        val twoDaysAgo = dateDaysAgo(2f).toCheckDateString()
        check("nodes with check_date < today -2 days", "node[check_date](if:date(t['check_date']) < date('$twoDaysAgo'));")

        val twoDaysInFuture = dateDaysAgo(-2f).toCheckDateString()
        check("nodes with check_date < today + 2 days", "node[check_date](if:date(t['check_date']) < date('$twoDaysInFuture'));")
    }

    @Test fun `element older x days`() {
        val date = dateDaysAgo(14f).toCheckDateString()
        check("nodes with older today -14 days", "node(if: date(timestamp()) < date('$date'));")
    }

    @Test fun `element newer x days`() {
        val date = dateDaysAgo(-14f).toCheckDateString()
        check("nodes with newer today + 14 days", "node(if: date(timestamp()) > date('$date'));")
    }

    @Test fun `tag older x days`() {
        val date = dateDaysAgo(14f).toCheckDateString()
        check("nodes with surface older today -14 days", "node[surface](if: " +
                "date(timestamp()) < date('$date') ||" +
                "date(t['surface:check_date']) < date('$date') ||" +
                "date(t['check_date:surface']) < date('$date') ||" +
                "date(t['surface:lastcheck']) < date('$date') ||" +
                "date(t['lastcheck:surface']) < date('$date') ||" +
                "date(t['surface:last_checked']) < date('$date') ||" +
                "date(t['last_checked:surface']) < date('$date')" +
                ");")
    }

    @Test fun `tag newer x days`() {
        val date = dateDaysAgo(14f).toCheckDateString()
        check("nodes with surface newer today - 14 days", "node[surface](if: " +
                "date(timestamp()) > date('$date') ||" +
                "date(t['surface:check_date']) > date('$date') ||" +
                "date(t['check_date:surface']) > date('$date') ||" +
                "date(t['surface:lastcheck']) > date('$date') ||" +
                "date(t['lastcheck:surface']) > date('$date') ||" +
                "date(t['surface:last_checked']) > date('$date') ||" +
                "date(t['last_checked:surface']) > date('$date')" +
                ");")
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
            """
        )
    }

    @Test fun `two or`() {
        check("nodes with highway or name or ref",
            """
            node[highway]->.n1;
            node[name]->.n2;
            node[ref]->.n3;
            (.n1;.n2;.n3;);
            """
        )
    }

    @Test fun `or as first child in and`() {
        check(
            "nodes with (highway or railway) and name and ref",
            """
            node[highway]->.n2;
            node[railway]->.n3;
            (.n2;.n3;)->.n1;
            node.n1[name][ref];
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

    @Test fun `and in bracket followed by another and`() {
        check(
            "nodes with (noname or (name and ref)) and highway",
            """
            node[noname]->.n2;
            node[name][ref]->.n3;
            (.n2;.n3;)->.n1;
            node.n1[highway];
            """
        )
    }

    private fun check(input: String, output: String) {
        val expr = input.toElementFilterExpression()
        assertEquals(
            output.replace("\n", "").replace(" ", ""),
            expr.toOverpassQLString().replace("\n", "").replace(" ", "")
        )
    }
}
