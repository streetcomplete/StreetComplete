package de.westnordost.streetcomplete.quests;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.quests.localized_name.AddNameForm;
import de.westnordost.streetcomplete.quests.place_name.AddPlaceName;

public class AddPlaceNameTest extends AOsmElementQuestTypeTest
{
	@Override public void setUp() throws Exception
	{
		super.setUp();
		tags.put("shop","kiosk");
	}

	public void testNoName()
	{
		bundle.putBoolean(AddNameForm.NO_NAME, true);
		verify(
				new StringMapEntryAdd("noname","yes"));
	}

	public void testName()
	{
		bundle.putString(AddNameForm.NAME, "my name");
		verify(
				new StringMapEntryAdd("name","my name"));
	}

	@Override protected OsmElementQuestType createQuestType()
	{
		return new AddPlaceName(null);
	}
}
