package de.westnordost.streetcomplete.data.osm.tql;

import org.junit.Test;

import de.westnordost.osmapi.map.data.BoundingBox;

import static org.junit.Assert.*;

/** Integration test for the filter parser and the filter expression, the whole way from parsing
 *  the tag filters expression to returning it as a OQL string. More convenient this way since the
 *  easiest way to create a filter expressions is to parse it from string. */
public class FiltersParserTest
{
	@Test public void node()
	{
		check("nodes", "node;");
		check("NODES", "node;");
	}

	@Test public void way()
	{
		check("ways", "way;");
		check("WAYS", "way;");
	}

	@Test public void relation()
	{
		check("relations", "rel;");
		check("RELATIONS", "rel;");
	}

	@Test public void multipleElementTypes()
	{
		check("nodes, ways, relations", "(node;way;rel;);");
		check("nodes ,ways", "(node;way;);");
		check("nodes , ways", "(node;way;);");
	}

	@Test public void anyQuote()
	{
		check("nodes with 'shop'", "node['shop'];");
		check("nodes with \"shop\"", "node[\"shop\"];");
		check("nodes with \"shoppin'\"", "node[\"shoppin'\"];");
		check("nodes with '\"shop\"ping'", "node['\"shop\"ping'];");
	}

	@Test public void multipleElementTypesWithTag()
	{
		check("nodes, ways, relations with shop",
				"(node[\"shop\"];way[\"shop\"];rel[\"shop\"];);");
	}

	@Test public void whitespaceInFrontOkay()
	{
		check("\t\n nodes", "node;");
	}

	@Test public void failIfNoElementDeclarationInFront()
	{
		shouldFail("butter");
	}

	@Test public void failIfElementDeclarationInFrontDuplicate()
	{
		shouldFail("nodes, nodes");
	}

	@Test public void failIfElementDeclarationInFrontAnyInvalid()
	{
		shouldFail("nodes, butter");
	}

	@Test public void tagNoSeparator()
	{
		shouldFail("nodeswith");
		shouldFail("nodes withhighway");
	}

	@Test public void tagKeyLikeReservedWord()
	{
		shouldFail("nodes with with");
		shouldFail("nodes with around");
		shouldFail("nodes with or");
		shouldFail("nodes with and");
	}

	@Test public void tagKeyLikeReservedWordWithQuotationMarks()
	{
		check("nodes with \"with\"", "node[\"with\"];");
		check("nodes with \"with\"=\"with\"", "node[\"with\"=\"with\"];");
	}

	@Test public void tagKeyWithQuotationMarks()
	{
		check("nodes with \"highway = residential or bla\"",
				"node[\"highway = residential or bla\"];");
	}

	@Test public void tagValueWithQuotationMarks()
	{
		check("nodes with highway = \"residential or bla\"",
				"node[\"highway\"=\"residential or bla\"];");
	}

	@Test public void tagValueGarbage()
	{
		check("nodes with highway = §$%&%/??",
				"node[\"highway\"=\"§$%&%/??\"];");
	}

	@Test public void tagKeyQuotationMarksNotClosed()
	{
		shouldFail("nodes with \"highway = residential or bla");
	}

	@Test public void tagValueQuotationMarksNotClosed()
	{
		shouldFail("nodes with highway = \"residential or bla");
	}

	@Test public void tagKey()
	{
		String expect = "node[\"highway\"];";

		check("nodes with highway", expect);
		check("nodes with(highway)", expect);
		check("nodes with (highway)", expect);
		check("nodes with ( highway)", expect);
		check("nodes with( highway)", expect);
		check("nodes with(highway )", expect);
		check("nodes with(highway ) ", expect);
		check("nodes with(highway) ", expect);
	}

	@Test public void danglingOp()
	{
		shouldFail("nodes with highway=");
	}

	@Test public void danglingBoolOp()
	{
		shouldFail("nodes with highway and");
		shouldFail("nodes with highway or ");
	}

	@Test public void bracketNotClosed()
	{
		shouldFail("nodes with (highway");
	}

	@Test public void bracketClosedTooOften()
	{
		shouldFail("nodes with highway)");
	}

	@Test public void unknownThingAfterTag()
	{
		shouldFail("nodes with highway what is this");
	}

	@Test public void tagNegation()
	{
		check("nodes with !highway", "node[\"highway\"!~\".\"];");
	}

	@Test public void tagOperatorWhitespaces()
	{
		String expect = "node[\"highway\"=\"residential\"];";

		check("nodes with highway=residential", expect);
		check("nodes with highway =residential", expect);
		check("nodes with highway= residential", expect);
		check("nodes with highway = residential", expect);
	}

	@Test public void tagOperator()
	{
		check("nodes with highway=residential", "node[\"highway\"=\"residential\"];");
		check("nodes with highway!=residential", "node[\"highway\"!=\"residential\"];");
		check("nodes with highway~residential", "node[\"highway\"~\"^(residential)$\"];");
		check("nodes with highway!~residential", "node[\"highway\"!~\"^(residential)$\"];");
	}

	@Test public void tagNegationNotCombinableWithOperator()
	{
		shouldFail("nodes with !highway=residential");
		shouldFail("nodes with !highway!=residential");
		shouldFail("nodes with !highway~residential");
		shouldFail("nodes with !highway!~residential");
	}

	@Test public void twoTags()
	{
		check("nodes with highway and name", "node[\"highway\"][\"name\"];");
		check("nodes with highway or name", "(node[\"highway\"];node[\"name\"];);");
	}

	@Test public void orInAnd()
	{
		check("nodes with(highway or railway)and name",
				"(node[\"highway\"][\"name\"];node[\"railway\"][\"name\"];);");
	}

	@Test public void boundingBox()
	{
		BoundingBox bbox = new BoundingBox(0,0,5,10);
		check("nodes", "[bbox:0,0,5,10];node;", bbox);
		check("nodes with highway", "[bbox:0,0,5,10];node[\"highway\"];", bbox);
		check("nodes with highway or railway",
				"[bbox:0,0,5,10];(node[\"highway\"];node[\"railway\"];);", bbox);
	}

	@Test public void boundingBoxWithMultipleElementTypes()
	{
		BoundingBox bbox = new BoundingBox(0,0,5,10);

		check("nodes, ways, relations", "[bbox:0,0,5,10];(node;way;rel;);", bbox);
		check("nodes, ways, relations with highway",
				"[bbox:0,0,5,10];(" +
				"node[\"highway\"];" +
				"way[\"highway\"];" +
				"rel[\"highway\"];" +
				");", bbox);

		check("nodes, ways, relations with highway or railway",
				"[bbox:0,0,5,10];(" +
				"node[\"highway\"];" +
				"node[\"railway\"];" +
				"way[\"highway\"];" +
				"way[\"railway\"];" +
				"rel[\"highway\"];" +
				"rel[\"railway\"];" +
				");", bbox);
	}

	private void shouldFail(String input)
	{
		try
		{
			new FiltersParser().parse(input);
			fail();
		}
		catch(RuntimeException ignore) {}
	}

	private void check(String input, String output)
	{
		check(input, output, null);
	}

	private void check(String input, String output, BoundingBox bbox)
	{
		TagFilterExpression expr = new FiltersParser().parse(input);
		assertEquals(output, expr.toOverpassQLString(bbox));
	}
}
