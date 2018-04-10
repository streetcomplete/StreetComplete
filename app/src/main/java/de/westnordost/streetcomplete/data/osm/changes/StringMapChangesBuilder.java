package de.westnordost.streetcomplete.data.osm.changes;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringMapChangesBuilder
{
	private static final String TAG = "ChangesBuilder";
	private final Map<String,String> source;
	private final Map<String, StringMapEntryChange> changes;

	public StringMapChangesBuilder(@NonNull Map<String,String> source)
	{
		this.source = source;
		changes = new HashMap<>();
	}

	public void delete(@NonNull String key)
	{
		String valueBefore = source.get(key);
		if(valueBefore == null)
		{
			throw new IllegalArgumentException("The key '" + key + "' does not exist in the map.");
		}
		checkDuplicate(key);
		changes.put(key, new StringMapEntryDelete(key, valueBefore));
		Log.i(TAG, "key '" + key + "' is deleted");
	}

	public void deleteIfExists(@NonNull String key)
	{
		if(source.get(key) == null)
		{
			return;
		}
		delete(key);
	}

	public void add(@NonNull String key, @NonNull String value)
	{
		if(source.containsKey(key))
		{
			throw new IllegalArgumentException("The key '" + key + "' already exists in the map.");
		}
		checkDuplicate(key);
		changes.put(key, new StringMapEntryAdd(key, value));
		Log.i(TAG, "key '" + key + "' with value '" + value + "' is added");
	}

	public void modify(@NonNull String key, @NonNull String value)
	{
		String valueBefore = source.get(key);
		if(valueBefore == null)
		{
			throw new IllegalArgumentException("The key '" + key + "' does not exist in the map.");
		}
		checkDuplicate(key);
		changes.put(key, new StringMapEntryModify(key, valueBefore, value));
		Log.i(TAG, "key '" + key + "' with value '" + valueBefore + "' is changed to value '" + value + "'");
	}

	public void addOrModify(@NonNull String key, @NonNull String value)
	{
		String valueBefore = source.get(key);
		if(valueBefore == null)
		{
			add(key, value);
		}
		else
		{
			modify(key, value);
		}
	}

	public String getPreviousValue(@NonNull String key)
	{
		return source.get(key);
	}

	private void checkDuplicate(String key)
	{
		if(changes.containsKey(key))
		{
			throw new IllegalStateException("The key '" + key + "' is already being modified.");
		}
	}

	public StringMapChanges create()
	{
		List<StringMapEntryChange> list = new ArrayList<>();
		list.addAll(changes.values());
		return new StringMapChanges(list);
	}
}
