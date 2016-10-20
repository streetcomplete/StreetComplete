package de.westnordost.osmagent.data.osm.tql;

public interface OQLExpressionValue extends BooleanExpressionValue
{
	String toOverpassQLString();
}
