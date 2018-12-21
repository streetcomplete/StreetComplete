package de.westnordost.streetcomplete.quests;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.quests.postbox_collection_times.AddCollectionTimesForm;
import de.westnordost.streetcomplete.quests.postbox_collection_times.AddPostboxCollectionTimes;

public class AddPostboxCollectionTimesTest extends AOsmElementQuestTypeTest
{
	public void testNoTimes()
	{
		bundle.putBoolean(AddCollectionTimesForm.Companion.getNO_TIMES_SPECIFIED(), true);
		verify(
			new StringMapEntryAdd("collection_times:signed","no"));
	}

	public void testTimes()
	{
		bundle.putString(AddCollectionTimesForm.Companion.getTIMES(), "my times");
		verify(
			new StringMapEntryAdd("collection_times","my times"));
	}

	@Override protected OsmElementQuestType createQuestType()
	{
		return new AddPostboxCollectionTimes(null);
	}
}
