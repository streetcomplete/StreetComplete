package de.westnordost.osmagent.quests.osm.changes;

import java.util.Map;

public interface StringMapEntryChange
{
	void applyTo(Map<String,String> map);
	String toString();
	boolean conflictsWith(Map<String,String> map);
}
