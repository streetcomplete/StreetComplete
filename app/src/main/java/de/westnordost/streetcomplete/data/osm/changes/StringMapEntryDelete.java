package de.westnordost.streetcomplete.data.osm.changes;

import java.util.Map;

public class StringMapEntryDelete implements StringMapEntryChange
{
	public String key;
	public String valueBefore;

	public StringMapEntryDelete(String key, String valueBefore)
	{
		this.key = key;
		this.valueBefore = valueBefore;
	}

	@Override public String toString()
	{
		return "DELETE \"" + key + "\"=\"" + valueBefore + "\"";
	}

	@Override public void applyTo(Map<String, String> map)
	{
		map.remove(key);
	}

	@Override public boolean conflictsWith(Map<String, String> map)
	{
		return !map.containsKey(key) || !map.get(key).equals(valueBefore);
	}

	@Override public boolean equals(Object other)
	{
		if(other == null || !(other instanceof StringMapEntryDelete)) return false;
		StringMapEntryDelete o = (StringMapEntryDelete) other;
		return key.equals(o.key) && valueBefore.equals(o.valueBefore);
	}

	@Override public int hashCode()
	{
		int result = key.hashCode();
		result = 31 * result + valueBefore.hashCode();
		return result;
	}

	@Override public StringMapEntryChange reversed()
	{
		return new StringMapEntryAdd(key, valueBefore);
	}
}
