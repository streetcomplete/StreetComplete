package de.westnordost.streetcomplete.quests.surface;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.quests.AOsmElementQuestTypeTest;
import de.westnordost.streetcomplete.quests.GroupedImageListQuestAnswerFragment;

public class AddPathSurfaceTest extends AOsmElementQuestTypeTest
{
	public void testSurface()
	{
		bundle.putString(GroupedImageListQuestAnswerFragment.Companion.getOSM_VALUE(), "cobblestone");
		verify(new StringMapEntryAdd("surface","cobblestone"));
	}

	@Override protected OsmElementQuestType createQuestType()
	{
		return new AddPathSurface(null);
	}
}
