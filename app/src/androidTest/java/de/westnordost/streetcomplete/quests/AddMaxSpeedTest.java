package de.westnordost.streetcomplete.quests;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify;
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

	public void testNormalSign()
	{
		bundle.putString(AddMaxSpeedForm.MAX_SPEED, "123");
		verify(
				new StringMapEntryAdd("maxspeed","123"),
				new StringMapEntryAdd("source:maxspeed","sign"));
	}

	public void testZoneSign()
	{
		bundle.putString(AddMaxSpeedForm.MAX_SPEED, "123");
		bundle.putString(AddMaxSpeedForm.MAX_SPEED_IMPLICIT_ROADTYPE, "zoneXYZ");
		bundle.putString(AddMaxSpeedForm.MAX_SPEED_IMPLICIT_COUNTRY, "AA");
		verify(
				new StringMapEntryAdd("maxspeed","123"),
				new StringMapEntryAdd("source:maxspeed","AA:zoneXYZ"));
	}

	public void testLivingStreet()
	{
		tags.put("highway","residential");
		bundle.putBoolean(AddMaxSpeedForm.LIVING_STREET, true);
		verify(
				new StringMapEntryModify("highway","residential","living_street"));
	}

	@Override protected OsmElementQuestType createQuestType()
	{
		return new AddMaxSpeed(null);
	}
}
