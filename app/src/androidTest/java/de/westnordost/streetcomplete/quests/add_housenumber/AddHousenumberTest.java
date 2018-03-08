package de.westnordost.streetcomplete.quests.add_housenumber;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.quests.AOsmElementQuestTypeTest;
import de.westnordost.streetcomplete.quests.housenumber.AddHousenumber;
import de.westnordost.streetcomplete.quests.housenumber.AddHousenumberForm;


public class AddHousenumberTest extends AOsmElementQuestTypeTest
{
	@Override public void setUp() throws Exception
	{
		super.setUp();
		tags.put("building","house");
	}

	public void testRegex()
	{
		String r = AddHousenumberForm.VALID_HOUSENUMBER_REGEX;
		assertTrue("1".matches(r));
		assertTrue("1234".matches(r));

		assertTrue("1234a".matches(r));
		assertTrue("1234/a".matches(r));
		assertTrue("1234 / a".matches(r));
		assertTrue("1234 / A".matches(r));
		assertTrue("1234A".matches(r));
		assertTrue("1234/9".matches(r));
		assertTrue("1234 / 9".matches(r));

		assertFalse("12345".matches(r));
		assertFalse("1234 5".matches(r));
		assertFalse("1234/55".matches(r));
		assertFalse("1234AB".matches(r));
	}

	public void testNumber()
	{
		bundle.putString(AddHousenumberForm.HOUSENUMBER, "99b");
		verify(new StringMapEntryAdd("addr:housenumber","99b"));
	}

	public void testName()
	{
		bundle.putString(AddHousenumberForm.HOUSENAME, "La Escalera");
		verify(new StringMapEntryAdd("addr:housename","La Escalera"));
	}

	public void testConscriptionNumber()
	{
		bundle.putString(AddHousenumberForm.CONSCRIPTIONNUMBER, "I.123");
		verify(
				new StringMapEntryAdd("addr:conscriptionnumber","I.123"),
				new StringMapEntryAdd("addr:housenumber","I.123"));
	}

	public void testConscriptionNumberAndStreetNumber()
	{
		bundle.putString(AddHousenumberForm.CONSCRIPTIONNUMBER, "I.123");
		bundle.putString(AddHousenumberForm.STREETNUMBER, "12b");
		verify(
				new StringMapEntryAdd("addr:conscriptionnumber","I.123"),
				new StringMapEntryAdd("addr:streetnumber","12b"),
				new StringMapEntryAdd("addr:housenumber","12b"));
	}

	@Override protected OsmElementQuestType createQuestType()
	{
		return new AddHousenumber(null);
	}
}
