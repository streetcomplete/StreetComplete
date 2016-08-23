package de.westnordost.osmagent.quests;

import java.io.Serializable;
import java.util.Map;

/** Represents a single change (of one key value pair) on a map of strings. It is either
 *  adding a key+value, expecting the given key does not exist in the map or removing a key+value
 *  pair, expecting it to have exactly the given value. */
public class StringMapEntryChange implements Serializable
{
	static final long serialVersionUID = 1L;

	public enum Type
	{
		ADD, DELETE
	}

	public String key;
	public String value;
	public Type type;

	StringMapEntryChange(String key, String value, Type type)
	{
		this.key = key;
		this.value = value;
		this.type = type;
	}

	public String toString()
	{
		return type + " " + key + " = " + value;
	}

	public void applyTo(Map<String,String> map)
	{
		if(type == Type.ADD)
		{
			map.put(key, value);
		}
		if(type == Type.DELETE)
		{
			map.remove(key);
		}
	}

	public boolean conflictsWith(Map<String,String> map)
	{
		if(type == Type.ADD)
		{
			return map.containsKey(key);
		}
		if(type == Type.DELETE)
		{
			return !map.containsKey(key) || !map.get(key).equals(value);
		}

		return false;
	}
}
