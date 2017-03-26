package de.westnordost.streetcomplete.quests;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.quests.building_levels.AddBuildingLevels;
import de.westnordost.streetcomplete.quests.building_levels.AddBuildingLevelsForm;
import de.westnordost.streetcomplete.quests.place_name.AddPlaceName;

public class AddBuildingLevelsTest extends AOsmElementQuestTypeTest
{
	@Override public void setUp()
	{
		super.setUp();
		tags.put("building","residential");
	}

	public void testBuildingLevelsOnly()
	{
		bundle.putInt(AddBuildingLevelsForm.BUILDING_LEVELS, 5);
		verify(
				R.string.quest_buildingLevels_commitMessage,
				new StringMapEntryAdd("building:levels","5"));
	}

	public void testBuildingLevelsAndZeroRoofLevels()
	{
		bundle.putInt(AddBuildingLevelsForm.BUILDING_LEVELS, 5);
		bundle.putInt(AddBuildingLevelsForm.ROOF_LEVELS, 0);
		verify(
				R.string.quest_buildingLevels_commitMessage,
				new StringMapEntryAdd("building:levels","5"),
				new StringMapEntryAdd("roof:levels","0"));
	}

	public void testBuildingLevelsAndRoofLevels()
	{
		bundle.putInt(AddBuildingLevelsForm.BUILDING_LEVELS, 5);
		bundle.putInt(AddBuildingLevelsForm.ROOF_LEVELS, 3);
		verify(
				R.string.quest_buildingLevels_commitMessage,
				new StringMapEntryAdd("building:levels","5"),
				new StringMapEntryAdd("roof:levels","3"));
	}

	@Override protected OsmElementQuestType createQuestType()
	{
		return new AddBuildingLevels();
	}
}
