package de.westnordost.osmagent.quests.osm.changes;

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
}
