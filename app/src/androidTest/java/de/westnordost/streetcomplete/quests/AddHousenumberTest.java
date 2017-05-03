package de.westnordost.streetcomplete.quests;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.quests.housenumber.AddHousenumber;
import de.westnordost.streetcomplete.quests.housenumber.AddHousenumberForm;

public class AddHousenumberTest extends AOsmElementQuestTypeTest
{
	@Override public void setUp()
	{
		super.setUp();
		tags.put("building","house");
	}

	public void testNoName()
	{
		bundle.putString(AddHousenumberForm.HOUSENUMBER, "99b");
		verify(
				new StringMapEntryAdd("addr:housenumber","99b"));
	}

	@Override protected OsmElementQuestType createQuestType()
	{
		return new AddHousenumber(null);
	}
}
