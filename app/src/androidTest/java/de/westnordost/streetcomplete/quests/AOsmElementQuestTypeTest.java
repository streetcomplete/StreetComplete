package de.westnordost.streetcomplete.quests;

import android.os.Bundle;
import android.support.annotation.NonNull;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChanges;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryChange;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryDelete;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify;
import de.westnordost.streetcomplete.quests.road_name.AddRoadName;
import de.westnordost.streetcomplete.quests.road_name.AddRoadNameForm;

public abstract class AOsmElementQuestTypeTest extends TestCase
{
	protected Bundle bundle;
	protected Map<String,String> tags;

	public void setUp()
	{
		bundle = new Bundle();
		tags = new HashMap<>();
	}

	protected final void verify(int expectedStringId, StringMapEntryChange... expectedChanges)
	{
		StringMapChangesBuilder cb = new StringMapChangesBuilder(tags);
		int stringId = createQuestType().applyAnswerTo(bundle, cb);
		assertEquals(expectedStringId, stringId);
		List<StringMapEntryChange> changes = cb.create().getChanges();
		for(StringMapEntryChange expectedChange : expectedChanges)
		{
			assertTrue(changes.contains(expectedChange));
		}
	}

	protected abstract OsmElementQuestType createQuestType();
}
