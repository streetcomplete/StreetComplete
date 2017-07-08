package de.westnordost.streetcomplete.quests;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingType;
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingTypeForm;

public class AddRecyclingTypeTest extends AOsmElementQuestTypeTest
{
	@Override public void setUp()
	{
		super.setUp();
		tags.put("amenity","recycling");
	}

	public void testRecyclingCentre()
	{
		bundle.putString(AddRecyclingTypeForm.OSM_VALUES, "centre");
		verify(new StringMapEntryAdd("recycling_type","centre"));
	}

	public void testRecyclingUndergroundContainer()
	{
		bundle.putString(AddRecyclingTypeForm.OSM_VALUES, "underground");
		verify(new StringMapEntryAdd("recycling_type","container"));
		verify(new StringMapEntryAdd("location","underground"));
	}

	public void testRecyclingOvergroundContainer()
	{
		bundle.putString(AddRecyclingTypeForm.OSM_VALUES, "overground");
		verify(new StringMapEntryAdd("recycling_type","container"));
	}

	@Override protected OsmElementQuestType createQuestType()
	{
		return new AddRecyclingType(null);
	}
}
