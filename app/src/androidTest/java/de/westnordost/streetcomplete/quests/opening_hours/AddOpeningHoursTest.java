package de.westnordost.streetcomplete.quests.opening_hours;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.quests.AOsmElementQuestTypeTest;
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHours;
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHoursForm;

public class AddOpeningHoursTest extends AOsmElementQuestTypeTest
{
	@Override protected OsmElementQuestType createQuestType()
	{
		return new AddOpeningHours();
	}

	public void testOpeningHours()
	{
		bundle.putString(AddOpeningHoursForm.OPENING_HOURS, "my cool opening hours");
		verify(
				R.string.quest_openingHours_commitMessage,
				new StringMapEntryAdd("opening_hours", "my cool opening hours"));
	}
}
