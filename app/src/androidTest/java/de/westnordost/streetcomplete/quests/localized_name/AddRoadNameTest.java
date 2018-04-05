package de.westnordost.streetcomplete.quests.localized_name;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify;
import de.westnordost.streetcomplete.quests.AOsmElementQuestTypeTest;
import de.westnordost.streetcomplete.quests.localized_name.data.PutRoadNameSuggestionsHandler;
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao;

import static org.mockito.Mockito.mock;

public class AddRoadNameTest extends AOsmElementQuestTypeTest
{
	@Override public void setUp() throws Exception
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
		bundle.putStringArray(AddLocalizedNameForm.NAMES, new String[]{"my name"});
		verify(new StringMapEntryAdd("name","my name"));
	}

	public void testMultipleNames()
	{
		bundle.putStringArray(AddLocalizedNameForm.NAMES, new String[]{"my name","kröötz"});
		bundle.putStringArray(AddLocalizedNameForm.LANGUAGE_CODES, new String[]{"en","de"});
		verify(
				new StringMapEntryAdd("name","my name"),
				new StringMapEntryAdd("name:en","my name"),
				new StringMapEntryAdd("name:de","kröötz")
		);
	}

	public void testMultipleNamesDefaultNameIsOfNoSpecificLanguage()
	{
		bundle.putStringArray(AddLocalizedNameForm.NAMES, new String[]{"my name / kröötz", "my name","kröötz"});
		bundle.putStringArray(AddLocalizedNameForm.LANGUAGE_CODES, new String[]{"", "en","de"});
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
		return new AddRoadName(null,
				mock(RoadNameSuggestionsDao.class),
				mock(PutRoadNameSuggestionsHandler.class));
	}
}
