package de.westnordost.streetcomplete.data.osm.tql;

public interface OQLExpressionValue extends BooleanExpressionValue
{
	String toOverpassQLString();
}
