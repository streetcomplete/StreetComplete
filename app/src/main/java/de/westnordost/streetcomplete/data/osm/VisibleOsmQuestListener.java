package de.westnordost.streetcomplete.data.osm;

import de.westnordost.osmapi.map.data.Element;

public interface VisibleOsmQuestListener
{
	void onQuestCreated(OsmQuest quest, Element element);
	void onOsmQuestRemoved(long questId);
}
