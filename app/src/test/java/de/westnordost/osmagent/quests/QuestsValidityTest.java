package de.westnordost.osmagent.quests;

import junit.framework.TestCase;

import java.util.List;

import de.westnordost.osmagent.data.QuestType;
import de.westnordost.osmagent.data.osm.OverpassQuestType;
import de.westnordost.osmagent.data.QuestTypes;

public class QuestsValidityTest extends TestCase
{
	public void testQueryValid()
	{
		List<QuestType> questTypes = new QuestTypes(QuestTypes.TYPES).getQuestTypesSortedByImportance();
		for(QuestType questType : questTypes)
		{
			if(questType instanceof OverpassQuestType)
			{
				// if this fails and the returned exception is not informative, catch here and record
				// the name of the OverpassQuestType
				((OverpassQuestType) questType).getOverpassQuery(null);
			}
		}
		// parsing the query threw no errors -> valid
		assertTrue(true);
	}
}
