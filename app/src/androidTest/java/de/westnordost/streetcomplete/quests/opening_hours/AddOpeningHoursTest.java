package de.westnordost.streetcomplete.quests.opening_hours;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.quests.AOsmElementQuestTypeTest;

public class AddOpeningHoursTest extends AOsmElementQuestTypeTest
{
	@Override protected OsmElementQuestType createQuestType()
	{
		return new AddOpeningHours(null);
	}

	public void testOpeningHours()
	{
		bundle.putString(AddOpeningHoursForm.OPENING_HOURS, "my cool opening hours");
		verify(new StringMapEntryAdd("opening_hours", "my cool opening hours"));
	}
}
