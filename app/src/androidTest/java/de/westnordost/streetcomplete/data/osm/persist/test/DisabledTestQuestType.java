package de.westnordost.streetcomplete.data.osm.persist.test;

public class DisabledTestQuestType extends TestQuestType
{
	@Override public boolean isDefaultEnabled() { return false; }
}