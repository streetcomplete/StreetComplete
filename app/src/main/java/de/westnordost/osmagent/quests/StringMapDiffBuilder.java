package de.westnordost.osmagent.quests;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StringMapDiffBuilder
{
	private final Map<String,String> source;
	private final List<StringMapEntryChange> changes;

	public StringMapDiffBuilder(@NonNull Map<String,String> source)
	{
		this.source = source;
		changes = new ArrayList<>();
	}

	public void delete(@NonNull String key)
	{
		String value = source.get(key);
		if(value == null)
		{
			throw new IllegalArgumentException("The key '" + key + "' does not exist in the map.");
		}
		changes.add(new StringMapEntryChange(key, value, StringMapEntryChange.Type.DELETE));
	}

	public void add(@NonNull String key, @NonNull String value)
	{
		if(source.containsKey(key))
		{
			throw new IllegalArgumentException("The key '" + key + "' already exists in the map.");
		}
		changes.add(new StringMapEntryChange(key, value, StringMapEntryChange.Type.ADD));
	}

	public void modify(@NonNull String key, @NonNull String value)
	{
		delete(key);
		changes.add(new StringMapEntryChange(key, value, StringMapEntryChange.Type.ADD));
	}

	public StringMapChanges create()
	{
		return new StringMapChanges(changes);
	}
}
