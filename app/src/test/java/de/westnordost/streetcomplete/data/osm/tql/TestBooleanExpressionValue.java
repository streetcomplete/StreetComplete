package de.westnordost.streetcomplete.data.osm.tql;

public class TestBooleanExpressionValue implements BooleanExpressionValue
{
	private String value;

	public TestBooleanExpressionValue(String v)
	{
		value = v;
	}

	@Override
	public boolean matches(Object ele)
	{
		return ele.equals(value);
	}

	@Override
	public String toString()
	{
		return value;
	}

	public Object getValue()
	{
		return value;
	}
}
