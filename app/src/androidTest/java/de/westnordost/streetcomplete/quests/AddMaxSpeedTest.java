package de.westnordost.streetcomplete.quests;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify;
import de.westnordost.streetcomplete.quests.max_speed.AddMaxSpeed;
import de.westnordost.streetcomplete.quests.max_speed.AddMaxSpeedForm;

public class AddMaxSpeedTest extends AOsmElementQuestTypeTest
{
	private static final String MAXSPEED_TYPE = "maxspeed:type";

	public void testNoSign()
	{
		bundle.putString(AddMaxSpeedForm.MAX_SPEED_IMPLICIT_ROADTYPE, "flubberway");
		bundle.putString(AddMaxSpeedForm.MAX_SPEED_IMPLICIT_COUNTRY, "XX");
		verify(new StringMapEntryAdd(MAXSPEED_TYPE,"XX:flubberway"));
	}

	public void testNormalSign()
	{
		bundle.putString(AddMaxSpeedForm.MAX_SPEED, "123");
		verify(
				new StringMapEntryAdd("maxspeed","123"),
				new StringMapEntryAdd(MAXSPEED_TYPE,"sign"));
	}

	public void testAdvisoryNormalSign()
	{
		bundle.putString(AddMaxSpeedForm.ADVISORY_SPEED, "123");
		verify(
				new StringMapEntryAdd("maxspeed:advisory","123"),
				new StringMapEntryAdd(MAXSPEED_TYPE+":advisory","sign"));
	}

	public void testZoneSign()
	{
		bundle.putString(AddMaxSpeedForm.MAX_SPEED, "123");
		bundle.putString(AddMaxSpeedForm.MAX_SPEED_IMPLICIT_ROADTYPE, "zoneXYZ");
		bundle.putString(AddMaxSpeedForm.MAX_SPEED_IMPLICIT_COUNTRY, "AA");
		verify(
				new StringMapEntryAdd("maxspeed","123"),
				new StringMapEntryAdd(MAXSPEED_TYPE,"AA:zoneXYZ"));
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
