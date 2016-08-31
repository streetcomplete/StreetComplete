package de.westnordost.osmagent.quests.osm.types;

import junit.framework.TestCase;

import java.util.List;

import de.westnordost.osmagent.quests.osm.download.ReflectionOverpassQuestTypeListCreator;

public class QuestsValidityTest extends TestCase
{
	public void testQueryValid()
	{
		List<OverpassQuestType> questTypes = new ReflectionOverpassQuestTypeListCreator().create();
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
