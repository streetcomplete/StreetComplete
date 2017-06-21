package de.westnordost.streetcomplete.quests;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessBusiness;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessBusinessForm;

public class AddWheelchairBusinessTest extends AOsmElementQuestTypeTest
{
	@Override public void setUp()
	{
		super.setUp();
		tags.put("shop","yes");
	}

	public void testBusiness()
	{
		bundle.putString(AddWheelchairAccessBusinessForm.ANSWER, "yes");
		verify(new StringMapEntryAdd("wheelchair","yes"));
	}

	@Override protected OsmElementQuestType createQuestType()
	{
		return new AddWheelchairAccessBusiness(null);
	}
}