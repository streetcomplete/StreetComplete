package de.westnordost.streetcomplete.data.osm.tql;

import junit.framework.TestCase;

import java.util.Iterator;

public class BooleanExpressionTest extends TestCase
{

	public void testExpand00() { checkExpand("a", "a"); }
	public void testExpand01() { checkExpand("a*b*c", "a*b*c"); }
	public void testExpand02() { checkExpand("a+b+c", "a+b+c"); }
	public void testExpand03() { checkExpand("a+b*c+d", "a+b*c+d"); }
	public void testExpand04() { checkExpand("a*b+c*d", "a*b+c*d"); }

	public void testExpand10() { checkExpand("(a+b)*c", "a*c+b*c"); }
	public void testExpand11() { checkExpand("a*(b+c)", "a*b+a*c"); }
	public void testExpand12() { checkExpand("a*(b+c)*d", "a*b*d+a*c*d"); }

	public void testExpand20() { checkExpand("a*(b+c*d)", "a*b+a*c*d"); }
	public void testExpand21() { checkExpand("a+b*(c+d)", "a+b*c+b*d"); }
	public void testExpand22() { checkExpand("(a+b)*c+d", "a*c+b*c+d"); }

	public void testExpand30() { checkExpand("((a+b)*c+d)*e", "a*c*e+b*c*e+d*e"); }
	public void testExpand31() { checkExpand("a*(b+c*(d+e))", "a*b+a*c*d+a*c*e"); }
	public void testExpand32() { checkExpand("z*(y+x*(a+b)*c+d)*e", "z*y*e+z*x*a*c*e+z*x*b*c*e+z*d*e"); }

	public void testExpand40() { checkExpand("(x+y)*z*(a+b)", "x*z*a+x*z*b+y*z*a+y*z*b"); }

	public void testMatchLeaf()
	{
		assertTrue(evalExpression("1"));
		assertFalse(evalExpression("0"));
	}

	public void testMatchOr()
	{
		assertTrue(evalExpression("1+1"));
		assertTrue(evalExpression("1+0"));
		assertTrue(evalExpression("0+1"));
		assertFalse(evalExpression("0+0"));

		assertTrue(evalExpression("0+0+1"));
	}

	public void testMatchAnd()
	{
		assertTrue(evalExpression("1*1"));
		assertFalse(evalExpression("1*0"));
		assertFalse(evalExpression("0*1"));
		assertFalse(evalExpression("0*0"));

		assertTrue(evalExpression("1*1*1"));
		assertFalse(evalExpression("1*1*0"));
	}

	public void testMatchAndInOr()
	{
		assertTrue(evalExpression("(1*0)+1"));
		assertFalse(evalExpression("(1*0)+0"));
		assertTrue(evalExpression("(1*1)+0"));
		assertTrue(evalExpression("(1*1)+1"));
	}

	public void testMatchOrInAnd()
	{
		assertTrue(evalExpression("(1+0)*1"));
		assertFalse(evalExpression("(1+0)*0"));
		assertFalse(evalExpression("(0+0)*0"));
		assertFalse(evalExpression("(0+0)*1"));
	}

	public void testTypeNotInitiallySet()
	{
		BooleanExpression x = new BooleanExpression();
		assertFalse(x.isAnd());
		assertFalse(x.isOr());
		assertFalse(x.isRoot());
		assertFalse(x.isValue());
	}

	public void testAddAnd()
	{
		BooleanExpression x = new BooleanExpression();
		assertTrue(x.addAnd().isAnd());
	}

	public void testAddOr()
	{
		BooleanExpression x = new BooleanExpression();
		assertTrue(x.addOr().isOr());
	}

	public void testSetAsRoot()
	{
		BooleanExpression x = new BooleanExpression(true);
		assertTrue(x.isRoot());
	}

	public void testSetAsValue()
	{
		BooleanExpression<BooleanExpressionValue> x = new BooleanExpression<>();
		x.addValue(new TestBooleanExpressionValue("jo"));
		assertTrue(x.getFirstChild().isValue());
		assertEquals("jo", ((TestBooleanExpressionValue)x.getFirstChild().getValue()).getValue());
	}

	public void testGetParent()
	{
		BooleanExpression parent = new BooleanExpression();
		assertNull(parent.getParent());
		assertEquals(parent, parent.addOpenBracket().getParent());
	}

	public void testCopyStringEquals()
	{
		BooleanExpression tree = TestBooleanExpressionParser.parse("(a+b)*c");
		BooleanExpression treeCopy = tree.copy();

		assertEquals(treeCopy.toString(), tree.toString());
	}

	public void testCopyIsDeep()
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
