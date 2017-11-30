package de.westnordost.streetcomplete.quests;

import java.util.ArrayList;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingType;
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingTypeForm;

public class AddRecyclingTypeTest extends AOsmElementQuestTypeTest
{
	@Override public void setUp() throws Exception
	{
		super.setUp();
		tags.put("amenity","recycling");
	}

	public void testRecyclingCentre()
	{
		bundle.putStringArrayList(AddRecyclingTypeForm.OSM_VALUES, getAsStringArray("centre"));
		verify(new StringMapEntryAdd("recycling_type","centre"));
	}

	public void testRecyclingUndergroundContainer()
	{
		bundle.putStringArrayList(AddRecyclingTypeForm.OSM_VALUES, getAsStringArray("underground"));
		verify(new StringMapEntryAdd("recycling_type","container"));
		verify(new StringMapEntryAdd("location","underground"));
	}

	public void testRecyclingOvergroundContainer()
	{
		bundle.putStringArrayList(AddRecyclingTypeForm.OSM_VALUES, getAsStringArray("overground"));
		verify(new StringMapEntryAdd("recycling_type","container"));
	}

	private ArrayList<String> getAsStringArray(String value)
	{
		ArrayList<String> values = new ArrayList<>();
		values.add(value);
		return values;
	}

	@Override protected OsmElementQuestType createQuestType()
	{
		return new AddRecyclingType(null);
	}
}
