package de.westnordost.osmagent.tql;

public interface OQLExpressionValue extends BooleanExpressionValue
{
	String toOverpassQLString();
}
