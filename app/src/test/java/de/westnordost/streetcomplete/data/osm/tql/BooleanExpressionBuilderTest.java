package de.westnordost.streetcomplete.data.osm.tql;

import junit.framework.TestCase;

public class BooleanExpressionBuilderTest extends TestCase
{
	public void testLeaf() { check("a"); }
	public void testAnd() { check("a*b"); }
	public void testOr() { check("a+b"); }

	public void testAnd3() { check("a*b*c"); }
	public void testOr3() { check("a+b+c"); }
	public void testAndOr() { check("a*b+c"); }
	public void testOrAnd() { check("a+b*c"); }

	public void testAndInOr() { check("a+b*c+d"); }
	public void testAndInOr2() { check("a*b+c*d"); }

	public void testBrackets0() { check("(a)", "a"); }

	public void testBrackets1() { check("(a*b)", "a*b"); }
	public void testBrackets2() { check("(a+b)", "a+b"); }
	public void testBrackets3() { check("((a*b))", "a*b"); }
	public void testBrackets4() { check("((a+b))", "a+b"); }

	public void testBrackets5() { check("(a+b)*c"); }
	public void testBrackets6() { check("a*(b+c)"); }
	public void testBrackets7() { check("a*(b+c)*d"); }

	public void testBrackets8() { check("(a*b)+c", "a*b+c"); }
	public void testBrackets9() { check("(a*b)*c", "a*b*c"); }
	public void testBrackets10() { check("(a+b)+c", "a+b+c"); }

	public void testBrackets11() { check("a+(b*c)", "a+b*c"); }
	public void testBrackets12() { check("a*(b*c)", "a*b*c"); }
	public void testBrackets13() { check("a+(b+c)", "a+b+c"); }

	public void testMerge1() { check("a+(b+(c+(d)))", "a+b+c+d"); }
	public void testMerge2() { check("a*(b*(c*(d)))", "a*b*c*d"); }
	public void testMerge3() { check("a*(b+(c*(d)))", "a*(b+c*d)"); }
	public void testMerge4() { check("a+(b*(c+(d)))", "a+b*(c+d)"); }

	public void testMerge5() { check("(((a)+b)+c)+d", "a+b+c+d"); }
	public void testMerge6() { check("(((a)*b)*c)*d", "a*b*c*d"); }
	public void testMerge7() { check("(((a)+b)*c)+d", "(a+b)*c+d"); }
	public void testMerge8() { check("(((a)*b)+c)*d", "(a*b+c)*d"); }


	public void testClosedTooManyBrackets()
	{
		try
		{
			TestBooleanExpressionParser.parse("a+b)");
			fail();
		}
		catch(IllegalStateException e) {}
		try
		{
			TestBooleanExpressionParser.parse("(a+b))");
			fail();
		}
		catch(IllegalStateException e) {}
		try
		{
			TestBooleanExpressionParser.parse("((b+c)*a)+d)");
			fail();
		}
		catch(IllegalStateException e) {}
	}

	public void testCloseTooLittleBrackets()
	{
		try
		{
			TestBooleanExpressionParser.parse("(a+b");
			fail();
		}
		catch(IllegalStateException e) {}
		try
		{
			TestBooleanExpressionParser.parse("((a+b)");
			fail();
		}
		catch(IllegalStateException e) {}
		try
		{
			TestBooleanExpressionParser.parse("((a*(b+c))");
			fail();
		}
		catch(IllegalStateException e) {}
	}

	private void check(String input)
	{
		check(input, input);
	}

	private void check(String input, String expected)
	{
		BooleanExpression tree = TestBooleanExpressionParser.parse(input);
		assertEquals(expected, translateOutput(tree.toString()));
	}

	private String translateOutput(String output)
	{
		return output.replaceAll(" and ", "*").replaceAll(" or ", "+");
	}
}
