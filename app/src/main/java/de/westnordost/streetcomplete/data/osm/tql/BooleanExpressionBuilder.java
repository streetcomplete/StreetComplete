package de.westnordost.streetcomplete.data.osm.tql;

import java.util.EmptyStackException;
import java.util.Stack;

/** Builds a boolean expression. Basically a BooleanExpression with a cursor. */
public class BooleanExpressionBuilder<T extends BooleanExpressionValue>
{
	private BooleanExpression<T> node;
	private Stack<BooleanExpression<T>> bracket;

	public BooleanExpressionBuilder()
	{
		node = new BooleanExpression<>(true);
		bracket = new Stack<>();
	}

	public void addOpenBracket()
	{
		bracket.push(node);
		node = node.addOpenBracket();
	}

	public void addCloseBracket()
	{
		try
		{
			node = bracket.pop();
		}
		catch(EmptyStackException e)
		{
			throw new IllegalStateException("Closed one bracket too much");
		}
	}

	public void addValue(T t)
	{
		node.addValue(t);
	}

	public void addAnd()
	{
		node = node.addAnd();
	}

	public void addOr()
	{
		node = node.addOr();
	}

	public BooleanExpression<T> getResult()
	{
		if(!bracket.empty())
		{
			throw new IllegalStateException("Closed one bracket too little");
		}

		while(node.getParent() != null)
		{
			node = node.getParent();
		}

		node.flatten();

		return node;
	}
}
