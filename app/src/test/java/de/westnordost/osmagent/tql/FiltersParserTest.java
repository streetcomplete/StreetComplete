package de.westnordost.osmagent.tql;

import junit.framework.TestCase;

import de.westnordost.osmapi.map.data.BoundingBox;

/** Integration test for the filter parser and the filter expression, the whole way from parsing
 *  the tag filters expression to returning it as a OQL string. More convenient this way since the
 *  easiest way to create a filter expresssion is to parse it from string. */
public class FiltersParserTest extends TestCase
{
	public void testNode()
	{
		check("nodes", "node;out body;");
		check("NODES", "node;out body;");
	}

	public void testWay()
	{
		check("ways", "way;(._;>;);out body;");
		check("WAYS", "way;(._;>;);out body;");
	}

	public void testRelation()
	{
		check("relations", "rel;(._;>;);out body;");
		check("RELATIONS", "rel;(._;>;);out body;");
	}

	public void testElement()
	{
		check("elements", "(node;way;rel;);(._;>;);out body;");
		check("ELEMENTS", "(node;way;rel;);(._;>;);out body;");
	}

	public void testAnyQuote()
	{
		check("nodes with 'shop'", "node['shop'];out body;");
		check("nodes with \"shop\"", "node[\"shop\"];out body;");
		check("nodes with \"shoppin'\"", "node[\"shoppin'\"];out body;");
		check("nodes with '\"shop\"ping'", "node['\"shop\"ping'];out body;");
	}

	public void testElementWithTag()
	{
		check("elements with shop",
				"(node[\"shop\"];way[\"shop\"];rel[\"shop\"];);(._;>;);out body;");
	}

	public void testWhitespaceInFrontOkay()
	{
		check("\t\n nodes", "node;out body;");
	}

	public void testFailIfNoElementDeclarationInFront()
	{
		shouldFail("butter");
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
		check("nodes with \"with\"", "node[\"with\"];out body;");
		check("nodes with \"with\"=\"with\"", "node[\"with\"=\"with\"];out body;");
	}

	public void testTagKeyWithQuotationMarks()
	{
		check("nodes with \"highway = residential or bla\"",
				"node[\"highway = residential or bla\"];out body;");
	}

	public void testTagValueWithQuotationMarks()
	{
		check("nodes with highway = \"residential or bla\"",
				"node[\"highway\"=\"residential or bla\"];out body;");
	}

	public void testTagValueGarbage()
	{
		check("nodes with highway = §$%&%/??",
				"node[\"highway\"=\"§$%&%/??\"];out body;");
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
		String expect = "node[\"highway\"];out body;";

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
		check("nodes with !highway", "node[\"highway\"!~\".\"];out body;");
	}

	public void testTagOperatorWhitespaces()
	{
		String expect = "node[\"highway\"=\"residential\"];out body;";

		check("nodes with highway=residential", expect);
		check("nodes with highway =residential", expect);
		check("nodes with highway= residential", expect);
		check("nodes with highway = residential", expect);
	}

	public void testTagOperator()
	{
		check("nodes with highway=residential", "node[\"highway\"=\"residential\"];out body;");
		check("nodes with highway!=residential", "node[\"highway\"!=\"residential\"];out body;");
		check("nodes with highway~residential", "node[\"highway\"~\"residential\"];out body;");
		check("nodes with highway!~residential", "node[\"highway\"!~\"residential\"];out body;");
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
		check("nodes with highway and name", "node[\"highway\"][\"name\"];out body;");
		check("nodes with highway or name", "(node[\"highway\"];node[\"name\"];);out body;");
	}

	public void testOrInAnd()
	{
		check("nodes with(highway or railway)and name",
				"(node[\"highway\"][\"name\"];node[\"railway\"][\"name\"];);out body;");
	}

	public void testBoundingBox()
	{
		BoundingBox bbox = new BoundingBox(0,0,5,10);
		check("nodes", "node(0.0,0.0,5.0,10.0);out body;", bbox);
		check("nodes with highway", "node[\"highway\"](0.0,0.0,5.0,10.0);out body;", bbox);
		check("nodes with highway or railway",
				"(node[\"highway\"](0.0,0.0,5.0,10.0);node[\"railway\"](0.0,0.0,5.0,10.0););out body;", bbox);
	}

	public void testBoundingBoxWithElement()
	{
		BoundingBox bbox = new BoundingBox(0,0,5,10);
		String b = "(0.0,0.0,5.0,10.0)";

		check("elements", "(node"+b+";way"+b+";rel"+b+";);(._;>;);out body;", bbox);
		check("elements with highway",
				"(" +
				"node[\"highway\"]"+b+";" +
				"way[\"highway\"]"+b+";" +
				"rel[\"highway\"]"+b+";" +
				");" +
				"(._;>;);out body;", bbox);

		check("elements with highway or railway",
				"(" +
				"node[\"highway\"]"+b+";" +
				"node[\"railway\"]"+b+";" +
				"way[\"highway\"]"+b+";" +
				"way[\"railway\"]"+b+";" +
				"rel[\"highway\"]"+b+";" +
				"rel[\"railway\"]"+b+";" +
				");" +
				"(._;>;);out body;", bbox);
	}

	private void shouldFail(String input)
	{
		try
		{
			new FiltersParser().parse(input);
			fail();
		}
		catch(RuntimeException e) {}
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
