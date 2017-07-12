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

	public void testOneName()
	{
		bundle.putStringArray(AddRoadNameForm.NAMES, new String[]{"my name"});
		verify(new StringMapEntryAdd("name","my name"));
	}

	public void testMultipleNames()
	{
		bundle.putStringArray(AddRoadNameForm.NAMES, new String[]{"my name","kröötz"});
		bundle.putStringArray(AddRoadNameForm.LANGUAGE_CODES, new String[]{"en","de"});
		verify(
				new StringMapEntryAdd("name","my name"),
				new StringMapEntryAdd("name:en","my name"),
				new StringMapEntryAdd("name:de","kröötz")
		);
	}

	public void testMultipleNamesDefaultNameIsOfNoSpecificLanguage()
	{
		bundle.putStringArray(AddRoadNameForm.NAMES, new String[]{"my name / kröötz", "my name","kröötz"});
		bundle.putStringArray(AddRoadNameForm.LANGUAGE_CODES, new String[]{null, "en","de"});
		verify(
				new StringMapEntryAdd("name","my name / kröötz"),
				new StringMapEntryAdd("name:en","my name"),
				new StringMapEntryAdd("name:de","kröötz")
		);
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
