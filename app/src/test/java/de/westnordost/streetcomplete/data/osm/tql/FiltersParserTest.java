package de.westnordost.streetcomplete.data.osm.tql;

import junit.framework.TestCase;

import de.westnordost.osmapi.map.data.BoundingBox;

/** Integration test for the filter parser and the filter expression, the whole way from parsing
 *  the tag filters expression to returning it as a OQL string. More convenient this way since the
 *  easiest way to create a filter expresssion is to parse it from string. */
public class FiltersParserTest extends TestCase
{
	public void testNode()
	{
		check("nodes", "node;");
		check("NODES", "node;");
	}

	public void testWay()
	{
		check("ways", "way;");
		check("WAYS", "way;");
	}

	public void testRelation()
	{
		check("relations", "rel;");
		check("RELATIONS", "rel;");
	}

	public void testMultipleElementTypes()
	{
		check("nodes, ways, relations", "(node;way;rel;);");
		check("nodes ,ways", "(node;way;);");
		check("nodes , ways", "(node;way;);");
	}

	public void testAnyQuote()
	{
		check("nodes with 'shop'", "node['shop'];");
		check("nodes with \"shop\"", "node[\"shop\"];");
		check("nodes with \"shoppin'\"", "node[\"shoppin'\"];");
		check("nodes with '\"shop\"ping'", "node['\"shop\"ping'];");
	}

	public void testMultipleElementTypesWithTag()
	{
		check("nodes, ways, relations with shop",
				"(node[\"shop\"];way[\"shop\"];rel[\"shop\"];);");
	}

	public void testWhitespaceInFrontOkay()
	{
		check("\t\n nodes", "node;");
	}

	public void testFailIfNoElementDeclarationInFront()
	{
		shouldFail("butter");
	}

	public void testFailIfElementDeclarationInFrontDuplicate()
	{
		shouldFail("nodes, nodes");
	}

	public void testFailIfElementDeclarationInFrontAnyInvalid()
	{
		shouldFail("nodes, butter");
	}

	public void testTagNoSeparator()
	{
		shouldFail("nodeswith");
		shouldFail("nodes withhighway");
	}

	public void testTagKeyLikeReservedWord()
	{
		shouldFail("nodes with with");
		shouldFail("nodes with around");
		shouldFail("nodes with or");
		shouldFail("nodes with and");
	}

	public void testTagKeyLikeReservedWordWithQuotationMarks()
	{
		check("nodes with \"with\"", "node[\"with\"];");
		check("nodes with \"with\"=\"with\"", "node[\"with\"=\"with\"];");
	}

	public void testTagKeyWithQuotationMarks()
	{
		check("nodes with \"highway = residential or bla\"",
				"node[\"highway = residential or bla\"];");
	}

	public void testTagValueWithQuotationMarks()
	{
		check("nodes with highway = \"residential or bla\"",
				"node[\"highway\"=\"residential or bla\"];");
	}

	public void testTagValueGarbage()
	{
		check("nodes with highway = §$%&%/??",
				"node[\"highway\"=\"§$%&%/??\"];");
	}

	public void testTagKeyQuotationMarksNotClosed()
	{
		shouldFail("nodes with \"highway = residential or bla");
	}

	public void testTagValueQuotationMarksNotClosed()
	{
		shouldFail("nodes with highway = \"residential or bla");
	}

	public void testTagKey()
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

	public void testDanglingOp()
	{
		shouldFail("nodes with highway=");
	}

	public void testDanglingBoolOp()
	{
		shouldFail("nodes with highway and");
		shouldFail("nodes with highway or ");
	}

	public void testBracketNotClosed()
	{
		shouldFail("nodes with (highway");
	}

	public void testBracketClosedTooOften()
	{
		shouldFail("nodes with highway)");
	}

	public void testUnknownThingAfterTag()
	{
		shouldFail("nodes with highway what is this");
	}

	public void testTagNegation()
	{
		check("nodes with !highway", "node[\"highway\"!~\".\"];");
	}

	public void testTagOperatorWhitespaces()
	{
		String expect = "node[\"highway\"=\"residential\"];";

		check("nodes with highway=residential", expect);
		check("nodes with highway =residential", expect);
		check("nodes with highway= residential", expect);
		check("nodes with highway = residential", expect);
	}

	public void testTagOperator()
	{
		check("nodes with highway=residential", "node[\"highway\"=\"residential\"];");
		check("nodes with highway!=residential", "node[\"highway\"!=\"residential\"];");
		check("nodes with highway~residential", "node[\"highway\"~\"^(residential)$\"];");
		check("nodes with highway!~residential", "node[\"highway\"!~\"^(residential)$\"];");
	}

	public void testTagNegationNotCombinableWithOperator()
	{
		shouldFail("nodes with !highway=residential");
		shouldFail("nodes with !highway!=residential");
		shouldFail("nodes with !highway~residential");
		shouldFail("nodes with !highway!~residential");
	}

	public void testTwoTags()
	{
		check("nodes with highway and name", "node[\"highway\"][\"name\"];");
		check("nodes with highway or name", "(node[\"highway\"];node[\"name\"];);");
	}

	public void testOrInAnd()
	{
		check("nodes with(highway or railway)and name",
				"(node[\"highway\"][\"name\"];node[\"railway\"][\"name\"];);");
	}

	public void testBoundingBox()
	{
		BoundingBox bbox = new BoundingBox(0,0,5,10);
		check("nodes", "[bbox:0.0,0.0,5.0,10.0];node;", bbox);
		check("nodes with highway", "[bbox:0.0,0.0,5.0,10.0];node[\"highway\"];", bbox);
		check("nodes with highway or railway",
				"[bbox:0.0,0.0,5.0,10.0];(node[\"highway\"];node[\"railway\"];);", bbox);
	}

	public void testBoundingBoxWithMultipleElementTypes()
	{
		BoundingBox bbox = new BoundingBox(0,0,5,10);

		check("nodes, ways, relations", "[bbox:0.0,0.0,5.0,10.0];(node;way;rel;);", bbox);
		check("nodes, ways, relations with highway",
				"[bbox:0.0,0.0,5.0,10.0];(" +
				"node[\"highway\"];" +
				"way[\"highway\"];" +
				"rel[\"highway\"];" +
				");", bbox);

		check("nodes, ways, relations with highway or railway",
				"[bbox:0.0,0.0,5.0,10.0];(" +
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
