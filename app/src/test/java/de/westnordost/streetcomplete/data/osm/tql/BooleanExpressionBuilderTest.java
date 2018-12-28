package de.westnordost.streetcomplete.data.osm.tql;

import org.junit.Test;

import static org.junit.Assert.*;

public class BooleanExpressionBuilderTest
{
	@Test public void leaf() { check("a"); }
	@Test public void and() { check("a*b"); }
	@Test public void or() { check("a+b"); }

	@Test public void and3() { check("a*b*c"); }
	@Test public void or3() { check("a+b+c"); }
	@Test public void andOr() { check("a*b+c"); }
	@Test public void orAnd() { check("a+b*c"); }

	@Test public void andInOr() { check("a+b*c+d"); }
	@Test public void andInOr2() { check("a*b+c*d"); }

	@Test public void brackets0() { check("(a)", "a"); }

	@Test public void brackets1() { check("(a*b)", "a*b"); }
	@Test public void brackets2() { check("(a+b)", "a+b"); }
	@Test public void brackets3() { check("((a*b))", "a*b"); }
	@Test public void brackets4() { check("((a+b))", "a+b"); }

	@Test public void brackets5() { check("(a+b)*c"); }
	@Test public void brackets6() { check("a*(b+c)"); }
	@Test public void brackets7() { check("a*(b+c)*d"); }

	@Test public void brackets8() { check("(a*b)+c", "a*b+c"); }
	@Test public void brackets9() { check("(a*b)*c", "a*b*c"); }
	@Test public void brackets10() { check("(a+b)+c", "a+b+c"); }

	@Test public void brackets11() { check("a+(b*c)", "a+b*c"); }
	@Test public void brackets12() { check("a*(b*c)", "a*b*c"); }
	@Test public void brackets13() { check("a+(b+c)", "a+b+c"); }

	@Test public void merge1() { check("a+(b+(c+(d)))", "a+b+c+d"); }
	@Test public void merge2() { check("a*(b*(c*(d)))", "a*b*c*d"); }
	@Test public void merge3() { check("a*(b+(c*(d)))", "a*(b+c*d)"); }
	@Test public void merge4() { check("a+(b*(c+(d)))", "a+b*(c+d)"); }

	@Test public void merge5() { check("(((a)+b)+c)+d", "a+b+c+d"); }
	@Test public void merge6() { check("(((a)*b)*c)*d", "a*b*c*d"); }
	@Test public void merge7() { check("(((a)+b)*c)+d", "(a+b)*c+d"); }
	@Test public void merge8() { check("(((a)*b)+c)*d", "(a*b+c)*d"); }


	@Test public void closedTooManyBrackets()
	{
		try
		{
			TestBooleanExpressionParser.parse("a+b)");
			fail();
		}
		catch(IllegalStateException ignored) {}
		try
		{
			TestBooleanExpressionParser.parse("(a+b))");
			fail();
		}
		catch(IllegalStateException ignored) {}
		try
		{
			TestBooleanExpressionParser.parse("((b+c)*a)+d)");
			fail();
		}
		catch(IllegalStateException ignored) {}
	}

	@Test public void closeTooFewBrackets()
	{
		try
		{
			TestBooleanExpressionParser.parse("(a+b");
			fail();
		}
		catch(IllegalStateException ignored) {}
		try
		{
			TestBooleanExpressionParser.parse("((a+b)");
			fail();
		}
		catch(IllegalStateException ignored) {}
		try
		{
			TestBooleanExpressionParser.parse("((a*(b+c))");
			fail();
		}
		catch(IllegalStateException ignored) {}
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
