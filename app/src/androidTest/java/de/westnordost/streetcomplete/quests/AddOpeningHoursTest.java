package de.westnordost.streetcomplete.quests;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AOsmElementQuestTypeTest;
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHours;
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHoursForm;

import static org.mockito.Mockito.mock;

public class AddOpeningHoursTest extends AOsmElementQuestTypeTest
{
	@Override protected OsmElementQuestType createQuestType()
	{
		return new AddOpeningHours(mock(OverpassMapDataDao.class));
	}

	public void testOpeningHours()
	{
		bundle.putString(AddOpeningHoursForm.OPENING_HOURS, "my cool opening hours");
		verify(new StringMapEntryAdd("opening_hours", "my cool opening hours"));
	}
}
