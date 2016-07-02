package de.westnordost.osmagent.tql;

import de.westnordost.osmapi.map.data.Element;

/** A value within a BooleanExpression that filters by a tag (key-value) combination. I.e.
 *  highway=residential */
public class TagFilterValue implements OQLExpressionValue
{
	public TagFilterValue(String key, String op, String value)
	{
		this.key = key;
		this.op = op;
		this.value = value;
	}

	private String key;
	private String op;
	private String value;

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
		if(op.indexOf('~') != -1)
		{
			// Overpass understands "." to mean "any string". For a Java regex matcher, that would
			// be ".*"
			String regExValue = value;
			if(regExValue.equals(".")) regExValue = ".*";
			if(op.equals("!~")) return !tagValue.matches(regExValue);
			if(op.equals("~")) return tagValue.matches(regExValue);
		}
		else
		{
			if(op.equals("!=")) return !tagValue.equals(value);
			if(op.equals("=")) return tagValue.equals(value);
		}
		return false;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		builder.append(ensureQuotes(key));
		if(op != null) builder.append(op);
		if(value != null) builder.append(ensureQuotes(value));
		return builder.toString();
	}

	private String ensureQuotes(String x)
	{
		String quot = x.charAt(0) != '"' && x.charAt(0) != '\'' ? "\"" : "";
		return quot + x + quot;
	}

	@Override
	public String toOverpassQLString()
	{
		return '['+toString()+']';
	}

}
