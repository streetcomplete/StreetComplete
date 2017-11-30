package de.westnordost.streetcomplete.quests;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessBusiness;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessBusinessForm;

public class AddWheelchairBusinessTest extends AOsmElementQuestTypeTest
{
	@Override public void setUp() throws Exception
	{
		super.setUp();
		tags.put("shop","yes");
	}

	public void testBusinessYes()
	{
		bundle.putString(AddWheelchairAccessBusinessForm.ANSWER, "yes");
		verify(new StringMapEntryAdd("wheelchair","yes"));
	}

	public void testBusinessNo()
	{
		bundle.putString(AddWheelchairAccessBusinessForm.ANSWER, "no");
		verify(new StringMapEntryAdd("wheelchair","no"));
	}

	public void testBusinessLimited()
	{
		bundle.putString(AddWheelchairAccessBusinessForm.ANSWER, "limited");
		verify(new StringMapEntryAdd("wheelchair","limited"));
	}

	@Override protected OsmElementQuestType createQuestType()
	{
		return new AddWheelchairAccessBusiness(null);
	}
}