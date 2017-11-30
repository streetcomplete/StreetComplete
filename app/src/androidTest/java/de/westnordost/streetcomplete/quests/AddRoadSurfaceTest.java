package de.westnordost.streetcomplete.quests;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.quests.road_surface.AddRoadSurface;
import de.westnordost.streetcomplete.quests.road_surface.AddRoadSurfaceForm;

public class AddRoadSurfaceTest extends AOsmElementQuestTypeTest
{
	@Override public void setUp() throws Exception
	{
		super.setUp();
		tags.put("highway","residential");
	}

	public void testSurface()
	{
		bundle.putString(AddRoadSurfaceForm.SURFACE, "cobblestone");
		verify(new StringMapEntryAdd("surface","cobblestone"));
	}

	@Override protected OsmElementQuestType createQuestType()
	{
		return new AddRoadSurface(null);
	}
}
