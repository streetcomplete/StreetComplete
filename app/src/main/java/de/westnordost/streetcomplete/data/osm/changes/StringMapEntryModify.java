package de.westnordost.streetcomplete.data.osm.changes;

import java.util.Map;

public class StringMapEntryModify implements StringMapEntryChange
{
	public String key;
	public String valueBefore;
	public String value;

	public StringMapEntryModify(String key, String valueBefore, String value)
	{
		this.key = key;
		this.valueBefore = valueBefore;
		this.value = value;
	}

	@Override public String toString()
	{
		return "MODIFY \"" + key + "\"=\"" + valueBefore + "\" -> \"" + key + "\"=\"" + value + "\"";
	}

	@Override public void applyTo(Map<String, String> map)
	{
		map.put(key, value);
	}

	@Override public boolean conflictsWith(Map<String, String> map)
	{
		return !map.containsKey(key) || !map.get(key).equals(valueBefore);
	}

	@Override public boolean equals(Object other)
	{
		if(other == null || !(other instanceof StringMapEntryModify)) return false;
		StringMapEntryModify o = (StringMapEntryModify) other;
		return key.equals(o.key) && valueBefore.equals(o.valueBefore) && value.equals(o.value);
	}

	@Override public int hashCode()
	{
		int result = key.hashCode();
		result = 31 * result + valueBefore.hashCode();
		result = 31 * result + value.hashCode();
		return result;
	}

	@Override public StringMapEntryChange reversed()
	{
		return new StringMapEntryModify(key, value, valueBefore);
	}
}
