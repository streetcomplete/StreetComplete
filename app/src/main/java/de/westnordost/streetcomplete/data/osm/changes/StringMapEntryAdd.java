package de.westnordost.streetcomplete.data.osm.changes;

import java.util.Map;

public class StringMapEntryAdd implements StringMapEntryChange
{
	public String key;
	public String value;

	public StringMapEntryAdd(String key, String value)
	{
		this.key = key;
		this.value = value;
	}

	@Override public String toString()
	{
		return "ADD \"" + key + "\"=\"" + value + "\"";
	}

	@Override public void applyTo(Map<String, String> map)
	{
		map.put(key, value);
	}

	@Override public boolean conflictsWith(Map<String, String> map)
	{
		return map.containsKey(key);
	}

	@Override public boolean equals(Object other)
	{
		if(other == null || !(other instanceof StringMapEntryAdd)) return false;
		StringMapEntryAdd o = (StringMapEntryAdd) other;
		return key.equals(o.key) && value.equals(o.value);
	}

	@Override public int hashCode()
	{
		int result = key.hashCode();
		result = 31 * result + value.hashCode();
		return result;
	}

	@Override public StringMapEntryChange reversed()
	{
		return new StringMapEntryDelete(key, value);
	}
}
