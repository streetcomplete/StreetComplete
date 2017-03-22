package de.westnordost.streetcomplete.quests;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.quests.place_name.AddPlaceName;

public class AddPlaceNameTest extends AOsmElementQuestTypeTest
{
	@Override public void setUp()
	{
		super.setUp();
		tags.put("shop","kiosk");
	}

	public void testNoName()
	{
		bundle.putBoolean(AddPlaceNameForm.NO_NAME, true);
		verify(
				R.string.quest_placeName_commitMessage_noname,
				new StringMapEntryAdd("noname","yes"));
	}

	public void testName()
	{
		bundle.putString(AddPlaceNameForm.NAME, "my name");
		verify(
				R.string.quest_placeName_commitMessage,
				new StringMapEntryAdd("name","my name"));
	}

	@Override protected OsmElementQuestType createQuestType()
	{
		return new AddPlaceName();
	}
}
