package de.westnordost.osmagent.quests.osm.types;

import junit.framework.TestCase;

import java.util.List;

import de.westnordost.osmagent.quests.osm.download.OsmQuestDownload;
import de.westnordost.osmagent.quests.osm.download.ReflectionQuestTypeListCreator;

public class QuestsValidityTest extends TestCase
{
	public void testQueryValid()
	{
		List<OverpassQuestType> questTypes = ReflectionQuestTypeListCreator
				.create(OverpassQuestType.class, OsmQuestDownload.OSM_QUEST_PACKAGE);
		for(OverpassQuestType questType : questTypes)
		{
			// if this fails and the returned exception is not informative, catch here and record
			// the name of the QuestType
			questType.getOverpassQuery(null);
		}
		// parsing the query threw no errors -> valid
		assertTrue(true);
	}
}
