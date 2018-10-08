package de.westnordost.streetcomplete.data.osm.tql;

import de.westnordost.osmapi.map.data.Element;

/** A value within a BooleanExpression that filters by a tag (key-value) combination. I.e.
 *  highway=residential
 *  If value is a regex, the format of it is to be stored to work with String.matches(), meaning
 *  that
 *    - must match the whole string (in Overpass, it may match anywhere)
 *    - "." means "any 1 character" (in Overpass, it means any string if alone)
 *  */
public class TagFilterValue implements OQLExpressionValue
{
	public TagFilterValue(String key, String op, String value)
	{
		this.key = key;
		this.op = op;
		this.value = value;
	}

	private final String key;
	private final String op;
	private final String value;

	public boolean matches(Object obj)
	{
		if(!(obj instanceof Element)) return false;
		Element element = (Element) obj;

		if(value == null)
		{
			return element.getTags() != null && element.getTags().containsKey(key);
		}

		String tagValue = element.getTags() != null ? element.getTags().get(key) : null;

		if(tagValue == null) return op.startsWith("!");
		if(isValueRegex())
		{
			if(op.equals("!~")) return !tagValue.matches(value);
			if(op.equals("~")) return tagValue.matches(value);
		}
		else
		{
			if(op.equals("!=")) return !tagValue.equals(value);
			if(op.equals("=")) return tagValue.equals(value);
		}
		return false;
	}

	private boolean isValueRegex()
	{
		return op != null && op.indexOf('~') != -1;
	}

	public String toString()
	{
		return toString(key, op, value);
	}

	private static String toString(String key, String op, String value)
	{
		StringBuilder builder = new StringBuilder();

		builder.append(ensureQuotes(key));
		if(op != null) builder.append(op);
		if(value != null) builder.append(ensureQuotes(value));
		return builder.toString();
	}

	private static String ensureQuotes(String x)
	{
		String quot = x.charAt(0) != '"' && x.charAt(0) != '\'' ? "\"" : "";
		return quot + x + quot;
	}

	@Override
	public String toOverpassQLString()
	{
		String overpassValue = value;
		if(isValueRegex())
		{
			if(value.equals(".*"))
			{
				overpassValue = ".";
			}
			else
			{
				overpassValue = "^(" + value + ")$";
			}
		}
		return "["+toString(key, op, overpassValue)+"]";
	}

}
