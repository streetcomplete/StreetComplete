package de.westnordost.streetcomplete.quests;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify;
import de.westnordost.streetcomplete.quests.road_name.AddRoadName;
import de.westnordost.streetcomplete.quests.road_name.AddRoadNameForm;

public class AddRoadNameTest extends AOsmElementQuestTypeTest
{
	@Override public void setUp()
	{
		super.setUp();
		tags.put("highway","residential");
	}

	public void testNoName()
	{
		bundle.putBoolean(AddRoadNameForm.NO_NAME, true);
		verify(new StringMapEntryAdd("noname","yes"));
	}

	public void testName()
	{
		bundle.putString(AddRoadNameForm.NAME, "my name");
		verify(new StringMapEntryAdd("name","my name"));
	}

	public void testIsService()
	{
		bundle.putInt(AddRoadNameForm.NO_PROPER_ROAD, AddRoadNameForm.IS_SERVICE);
		verify(new StringMapEntryModify("highway",tags.get("highway"),"service"));
	}

	public void testIsTrack()
	{
		bundle.putInt(AddRoadNameForm.NO_PROPER_ROAD, AddRoadNameForm.IS_TRACK);
		verify(new StringMapEntryModify("highway",tags.get("highway"),"track"));
	}

	public void testIsLink()
	{
		bundle.putInt(AddRoadNameForm.NO_PROPER_ROAD, AddRoadNameForm.IS_LINK);

		tags.put("highway","primary");
		verify(new StringMapEntryModify("highway",tags.get("highway"),"primary_link"));

		tags.put("highway","secondary");
		verify(new StringMapEntryModify("highway",tags.get("highway"),"secondary_link"));

		tags.put("highway","tertiary");
		verify(new StringMapEntryModify("highway",tags.get("highway"),"tertiary_link"));
	}

	@Override protected OsmElementQuestType createQuestType()
	{
		return new AddRoadName(null);
	}
}
