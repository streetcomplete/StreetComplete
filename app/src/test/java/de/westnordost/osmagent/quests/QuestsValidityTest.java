package de.westnordost.osmagent.quests;

import junit.framework.TestCase;

import java.util.List;

import de.westnordost.osmagent.OsmagentConstants;
import de.westnordost.osmagent.data.osm.OverpassQuestType;
import de.westnordost.osmagent.data.osm.download.ReflectionQuestTypeListCreator;

public class QuestsValidityTest extends TestCase
{
	public void testQueryValid()
	{
		List<OverpassQuestType> questTypes = ReflectionQuestTypeListCreator
				.create(OverpassQuestType.class, OsmagentConstants.OSM_QUESTS_PACKAGE);
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
