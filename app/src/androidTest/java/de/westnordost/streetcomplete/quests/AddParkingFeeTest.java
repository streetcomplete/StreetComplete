package de.westnordost.streetcomplete.quests;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.quests.parking_fee.AddParkingFee;
import de.westnordost.streetcomplete.quests.parking_fee.AddParkingFeeForm;

public class AddParkingFeeTest extends AOsmElementQuestTypeTest
{

	@Override protected OsmElementQuestType createQuestType()
	{
		return new AddParkingFee(null);
	}

	public void testYes()
	{
		bundle.putBoolean(AddParkingFeeForm.FEE, true);
		verify(new StringMapEntryAdd("fee", "yes"));
	}

	public void testNo()
	{
		bundle.putBoolean(AddParkingFeeForm.FEE, false);
		verify(new StringMapEntryAdd("fee", "no"));
	}

	public void testYesButOnlyAt()
	{
		bundle.putBoolean(AddParkingFeeForm.FEE, false);
		bundle.putString(AddParkingFeeForm.FEE_CONDITONAL_HOURS, "xyz");
		verify(
			new StringMapEntryAdd("fee", "no"),
		    new StringMapEntryAdd("fee:conditional", "yes @ (xyz)"));
	}

	public void testYesButNotAt()
	{
		bundle.putBoolean(AddParkingFeeForm.FEE, true);
		bundle.putString(AddParkingFeeForm.FEE_CONDITONAL_HOURS, "xyz");
		verify(
			new StringMapEntryAdd("fee", "yes"),
			new StringMapEntryAdd("fee:conditional", "no @ (xyz)"));
	}
}
