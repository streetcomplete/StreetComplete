package de.westnordost.osmagent.quests.types;

import junit.framework.TestCase;

import java.util.List;

import de.westnordost.osmagent.quests.create.ReflectionQuestTypeListCreator;

public class QuestsValidityTest extends TestCase
{
	public void testQueryValid()
	{
		List<QuestType> questTypes = new ReflectionQuestTypeListCreator().create();
		for(QuestType questType : questTypes)
		{
			// if this fails and the returned exception is not informative, catch here and record
			// the name of the QuestType
			questType.getOverpassQuery(null);
		}
		// parsing the query threw no errors -> valid
		assertTrue(true);
	}
}
