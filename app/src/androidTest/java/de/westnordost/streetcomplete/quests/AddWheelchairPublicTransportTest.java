package de.westnordost.streetcomplete.quests;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelChairAccessPublicTransport;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessPublicTransportForm;

public class AddWheelchairPublicTransportTest extends AOsmElementQuestTypeTest
{
	@Override public void setUp()
	{
		super.setUp();
		tags.put("highway","bus_stop");
	}

	public void testPublicTransport()
	{
		bundle.putString(AddWheelchairAccessPublicTransportForm.ANSWER, "yes");
		verify(new StringMapEntryAdd("wheelchair","yes"));
	}

	@Override protected OsmElementQuestType createQuestType()
	{
		return new AddWheelChairAccessPublicTransport(null);
	}
}
