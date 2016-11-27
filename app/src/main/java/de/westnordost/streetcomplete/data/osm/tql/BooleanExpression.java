package de.westnordost.streetcomplete.data.osm.tql;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;

/** A boolean expression of values that are connected by ANDs and ORs */
public class BooleanExpression<T extends BooleanExpressionValue>
{
	public enum Type
	{
		AND, OR, ROOT, LEAF
	}

	// once set, these are final
	private Type type;
	private T value;

	private BooleanExpression<T> parent;
	private LinkedList<BooleanExpression<T>> children = new LinkedList<>();

	public BooleanExpression(boolean asRoot)
	{
		if(asRoot) setType(Type.ROOT);
	}

	public BooleanExpression()
	{

	}

	/** --------------------- Methods for extending the boolean expression ---------------------- */

	public BooleanExpression<T> addAnd()
	{
		if(!isAnd())
		{
			BooleanExpression<T> newChild = createIntermediateChild();
			newChild.setType(Type.AND);
			return newChild;
		}
		return this;
	}

	public BooleanExpression<T> addOr()
	{
		BooleanExpression<T> node = this;

		if(isAnd())
		{
			if (getParent().isRoot())
			{
				node = createIntermediateParent();
				node.setType(Type.OR);
			}
			else
			{
				node = getParent();
			}
		}

		if (!node.isOr())
		{
			BooleanExpression<T> newChild = node.createIntermediateChild();
			newChild.setType(Type.OR);
			return newChild;
		}
		return node;
	}

	public void addValue(T t)
	{
		BooleanExpression<T> child = createChild();
		child.setType(Type.LEAF);
		child.value = t;
	}

	public BooleanExpression<T> addOpenBracket()
	{
		return createChild();
	}

	private BooleanExpression<T> createChild()
	{
		BooleanExpression<T> child = new BooleanExpression<>();
		addChild(child);
		return child;
	}

	private BooleanExpression<T> createIntermediateParent()
	{
		BooleanExpression<T> newParent = new BooleanExpression<>();
		BooleanExpression<T> parent = getParent();
		parent.removeChild(this);
		newParent.addChild(this);
		parent.addChild(newParent);
		return newParent;
	}

	private BooleanExpression<T> createIntermediateChild()
	{
		BooleanExpression<T> lastChild = removeLastChild();
		BooleanExpression<T> newNode = createChild();
		if(lastChild != null) newNode.addChild(lastChild);
		return newNode;
	}

	private BooleanExpression<T> removeLastChild()
	{
		if(children.isEmpty()) return null;
		return children.removeLast();
	}

	private void addChild(BooleanExpression<T> child)
	{
		child.setParent(this);
		children.add(child);
	}

	private void removeChild(BooleanExpression<T> child)
	{
		children.remove(child);
		child.setParent(null);
	}

	/** --------------------- Methods for accessing the boolean expression ---------------------- */

	public boolean matches(Object element)
	{
		switch(type)
		{
			case LEAF:
				return value.matches(element);
			case OR:
				for(BooleanExpression<T> child : children)
				{
					if(child.matches(element)) return true;
				}
				return false;
			case AND:
				for(BooleanExpression<T> child : children)
				{
					if(!child.matches(element)) return false;
				}
				return true;
			case ROOT:
				return  children.getFirst().matches(element);
		}
		return false;
	}

	public BooleanExpression<T> getFirstChild()
	{
		return children.isEmpty() ? null : children.getFirst();
	}

	public Iterable<BooleanExpression<T>> getChildren()
	{
		return Collections.unmodifiableList(children);
	}

	public T getValue() { return value; }
	public BooleanExpression<T> getParent() { return parent; }
	private void setParent(BooleanExpression<T> newParent) { parent = newParent; }

	public boolean isOr() { return type == Type.OR; }
	public boolean isAnd() { return type == Type.AND; }
	public boolean isValue() { return type == Type.LEAF; }
	public boolean isRoot() { return type == Type.ROOT; }

	private void setType(Type op)
	{
		if(type != null) throw new IllegalStateException();
		type = op;
	}

	/** Removes unnecessary depth in the expression tree */
	public void flatten()
	{
		removeEmptyNodes();
		mergeNodesWithSameOperator();
	}

	/** remove nodes from superfluous brackets */
	private void removeEmptyNodes()
	{
		ListIterator<BooleanExpression<T>> it = children.listIterator();
		while ( it.hasNext() )
		{
			BooleanExpression<T> child = it.next();
			if(child.type == null && child.children.size() == 1)
			{
				replaceChildAt(it, child.children.getFirst());
				it.previous(); // = the just replaced node will be checked again
			}
			else
			{
				child.removeEmptyNodes();
			}
		}
	}

	/** merge children recursively which do have the same operator set (and, or) */
	private void mergeNodesWithSameOperator()
	{
		if(isValue()) return;

		ListIterator<BooleanExpression<T>> it = children.listIterator();
		while (it.hasNext())
		{
			BooleanExpression<T> child = it.next();
			child.mergeNodesWithSameOperator();

			// merge two successive nodes of same type
			if (child.type == type)
			{
				replaceChildAt(it, child.children);
			}
		}
	}

	private void replaceChildAt(ListIterator<BooleanExpression<T>> at,
								BooleanExpression<T> with)
	{
		replaceChildAt(at, Collections.singletonList(with));
	}

	private void replaceChildAt(ListIterator<BooleanExpression<T>> at,
								Collection<BooleanExpression<T>> with)
	{
		at.remove();
		for(BooleanExpression<T> withEle : with)
		{
			at.add(withEle);
			withEle.setParent(this);
		}
	}

	/** Expand the expression so that all ANDs have only leaves */
	public void expand()
	{
		moveDownAnds();
		mergeNodesWithSameOperator();
	}

	private void moveDownAnds()
	{
		if(isValue()) return;

		if(isAnd())
		{
			BooleanExpression<T> rest = removeFirstOr();
			if(rest != null)
			{
				// the first OR moves into our place, we are now an orphan
				getParent().replaceChild(this, rest);
				addCopiesOfMyselfInBetweenChildrenOf(rest);
				rest.mergeNodesWithSameOperator();
				rest.moveDownAnds();
				return;
			}
		}
		for(BooleanExpression<T> child : new LinkedList<>(children)) child.moveDownAnds();
	}

	private void replaceChild(BooleanExpression<T> replace, BooleanExpression<T> with)
	{
		ListIterator<BooleanExpression<T>> it = children.listIterator();
		while ( it.hasNext() )
		{
			BooleanExpression<T> child = it.next();
			if(child == replace)
			{
				replaceChildAt(it, with);
				return;
			}
		}
	}

	public BooleanExpression<T> copy()
	{
		BooleanExpression<T> result = new BooleanExpression<>();
		result.type = type;
		result.value = value; // <- this is a a reference! value should be immutable
		result.parent = null; // parent is set on parent.addChild, see for loop
		result.children = new LinkedList<>();
		for(BooleanExpression<T> child : children)
		{
			result.addChild(child.copy());
		}

		return result;
	}

	/** Adds deep copies of this as children to other, each taking one original child as its own */
	private void addCopiesOfMyselfInBetweenChildrenOf(BooleanExpression<T> other)
	{
		ListIterator<BooleanExpression<T>> it = other.children.listIterator();
		while (it.hasNext())
		{
			BooleanExpression<T> child = it.next();
			BooleanExpression<T> clone = copy();
			clone.replacePlaceholder(child);
			other.replaceChildAt(it, clone);
		}
	}

	private void replacePlaceholder(BooleanExpression<T> with)
	{
		ListIterator<BooleanExpression<T>> it = children.listIterator();
		while (it.hasNext())
		{
			BooleanExpression<T> child = it.next();
			if(child.type == null)
			{
				replaceChildAt(it, with);
				return;
			}
		}
	}

	/** Find first OR child and remove it from my children */
	private BooleanExpression<T> removeFirstOr()
	{
		ListIterator<BooleanExpression<T>> it = children.listIterator();
		while ( it.hasNext() )
		{
			BooleanExpression<T> child = it.next();
			if (child.isOr())
			{
				it.remove();
				BooleanExpression<T> placeholder = new BooleanExpression<>();
				it.add(placeholder);
				return child;
			}
		}
		return null;
	}

	@Override
	public String toString()
	{
		if(type == Type.LEAF) return value.toString();

		StringBuilder builder = new StringBuilder();
		if(isOr() && !getParent().isRoot()) builder.append('(');

		boolean first = true;
		for(BooleanExpression<T> child : children)
		{
			if(first) first = false;
			else
			{
				builder.append(' ');
				builder.append(type.toString().toLowerCase());
				builder.append(' ');
			}

			builder.append(child.toString());
		}
		if(isOr() && !getParent().isRoot()) builder.append(')');

		return builder.toString();
	}
}
