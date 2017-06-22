package de.westnordost.streetcomplete.quests;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.quests.max_speed.AddMaxSpeed;
import de.westnordost.streetcomplete.quests.max_speed.AddMaxSpeedForm;

public class AddMaxSpeedTest extends AOsmElementQuestTypeTest
{
	public void testNoSign()
	{
		bundle.putString(AddMaxSpeedForm.MAX_SPEED_IMPLICIT_ROADTYPE, "flubberway");
		bundle.putString(AddMaxSpeedForm.MAX_SPEED_IMPLICIT_COUNTRY, "XX");
		verify(new StringMapEntryAdd("source:maxspeed","XX:flubberway"));
	}

	public void testElectronicSign()
	{
		bundle.putString(AddMaxSpeedForm.MAX_SPEED, "signals");
		verify(
				new StringMapEntryAdd("maxspeed","signals"),
				new StringMapEntryAdd("source:maxspeed","sign"));
	}

	public void testNormalSign()
	{
		bundle.putString(AddMaxSpeedForm.MAX_SPEED, "123");
		verify(
				new StringMapEntryAdd("maxspeed","123"),
				new StringMapEntryAdd("source:maxspeed","sign"));
	}

	@Override protected OsmElementQuestType createQuestType()
	{
		return new AddMaxSpeed(null);
	}
}
