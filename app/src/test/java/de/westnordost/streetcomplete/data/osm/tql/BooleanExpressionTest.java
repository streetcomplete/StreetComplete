package de.westnordost.streetcomplete.data.osm.tql;

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class BooleanExpressionTest
{

	@Test public void expand00() { checkExpand("a", "a"); }
	@Test public void expand01() { checkExpand("a*b*c", "a*b*c"); }
	@Test public void expand02() { checkExpand("a+b+c", "a+b+c"); }
	@Test public void expand03() { checkExpand("a+b*c+d", "a+b*c+d"); }
	@Test public void expand04() { checkExpand("a*b+c*d", "a*b+c*d"); }

	@Test public void expand10() { checkExpand("(a+b)*c", "a*c+b*c"); }
	@Test public void expand11() { checkExpand("a*(b+c)", "a*b+a*c"); }
	@Test public void expand12() { checkExpand("a*(b+c)*d", "a*b*d+a*c*d"); }

	@Test public void expand20() { checkExpand("a*(b+c*d)", "a*b+a*c*d"); }
	@Test public void expand21() { checkExpand("a+b*(c+d)", "a+b*c+b*d"); }
	@Test public void expand22() { checkExpand("(a+b)*c+d", "a*c+b*c+d"); }

	@Test public void expand30() { checkExpand("((a+b)*c+d)*e", "a*c*e+b*c*e+d*e"); }
	@Test public void expand31() { checkExpand("a*(b+c*(d+e))", "a*b+a*c*d+a*c*e"); }
	@Test public void expand32() { checkExpand("z*(y+x*(a+b)*c+d)*e", "z*y*e+z*x*a*c*e+z*x*b*c*e+z*d*e"); }

	@Test public void expand40() { checkExpand("(x+y)*z*(a+b)", "x*z*a+x*z*b+y*z*a+y*z*b"); }

	@Test public void matchLeaf()
	{
		assertTrue(evalExpression("1"));
		assertFalse(evalExpression("0"));
	}

	@Test public void matchOr()
	{
		assertTrue(evalExpression("1+1"));
		assertTrue(evalExpression("1+0"));
		assertTrue(evalExpression("0+1"));
		assertFalse(evalExpression("0+0"));

		assertTrue(evalExpression("0+0+1"));
	}

	@Test public void matchAnd()
	{
		assertTrue(evalExpression("1*1"));
		assertFalse(evalExpression("1*0"));
		assertFalse(evalExpression("0*1"));
		assertFalse(evalExpression("0*0"));

		assertTrue(evalExpression("1*1*1"));
		assertFalse(evalExpression("1*1*0"));
	}

	@Test public void matchAndInOr()
	{
		assertTrue(evalExpression("(1*0)+1"));
		assertFalse(evalExpression("(1*0)+0"));
		assertTrue(evalExpression("(1*1)+0"));
		assertTrue(evalExpression("(1*1)+1"));
	}

	@Test public void matchOrInAnd()
	{
		assertTrue(evalExpression("(1+0)*1"));
		assertFalse(evalExpression("(1+0)*0"));
		assertFalse(evalExpression("(0+0)*0"));
		assertFalse(evalExpression("(0+0)*1"));
	}

	@Test public void typeNotInitiallySet()
	{
		BooleanExpression x = new BooleanExpression();
		assertFalse(x.isAnd());
		assertFalse(x.isOr());
		assertFalse(x.isRoot());
		assertFalse(x.isValue());
	}

	@Test public void addAnd()
	{
		BooleanExpression x = new BooleanExpression();
		assertTrue(x.addAnd().isAnd());
	}

	@Test public void addOr()
	{
		BooleanExpression x = new BooleanExpression();
		assertTrue(x.addOr().isOr());
	}

	@Test public void setAsRoot()
	{
		BooleanExpression x = new BooleanExpression(true);
		assertTrue(x.isRoot());
	}

	@Test public void setAsValue()
	{
		BooleanExpression<BooleanExpressionValue> x = new BooleanExpression<>();
		x.addValue(new TestBooleanExpressionValue("jo"));
		assertTrue(x.getFirstChild().isValue());
		assertEquals("jo", ((TestBooleanExpressionValue)x.getFirstChild().getValue()).getValue());
	}

	@Test public void getParent()
	{
		BooleanExpression parent = new BooleanExpression();
		assertNull(parent.getParent());
		assertEquals(parent, parent.addOpenBracket().getParent());
	}

	@Test public void copyStringEquals()
	{
		BooleanExpression tree = TestBooleanExpressionParser.parse("(a+b)*c");
		BooleanExpression treeCopy = tree.copy();

		assertEquals(treeCopy.toString(), tree.toString());
	}

	@Test public void copyIsDeep()
	{
		BooleanExpression<BooleanExpressionValue> tree = TestBooleanExpressionParser.parse("(a+b)*c");
		BooleanExpression<BooleanExpressionValue> treeCopy = tree.copy();
		checkRecursiveEqualsButNotSame(tree, treeCopy);
	}

	private void checkRecursiveEqualsButNotSame(BooleanExpression<BooleanExpressionValue> tree,
												BooleanExpression<BooleanExpressionValue> treeCopy)
	{
		assertNotSame(tree, treeCopy);
		assertEquals(tree.toString(), treeCopy.toString());

		Iterator<BooleanExpression<BooleanExpressionValue>> treeIt = tree.getChildren().iterator();
		Iterator<BooleanExpression<BooleanExpressionValue>> treeCopyIt = treeCopy.getChildren().iterator();

		while(treeIt.hasNext())
		{
			checkRecursiveEqualsButNotSame(treeIt.next(), treeCopyIt.next());
		}
	}


	private void checkExpand(String input, String expected)
	{
		BooleanExpression tree = TestBooleanExpressionParser.parse(input);
		tree.expand();
		assertEquals(expected, translateOutput(tree.toString()));
	}

	private String translateOutput(String output)
	{
		return output.replaceAll(" and ", "*").replaceAll(" or ", "+");
	}

	private boolean evalExpression(String input)
	{
		BooleanExpression<BooleanExpressionValue> expr = TestBooleanExpressionParser.parse(input);
		return expr.matches("1");
	}
}
