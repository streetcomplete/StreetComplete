package de.westnordost.streetcomplete.data.osm.download;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class OverpassStatusParserTest extends TestCase
{
	public void testParseRateLimit()
	{
		assertEquals(2, parse("Rate limit: 2").maxAvailableSlots);
	}

	public void testParseAvailableSlots()
	{
		assertEquals(33, parse("33 slots available now.").availableSlots);
	}

	public void testParseNoAvailableSlots()
	{
		assertEquals(25, (int) parse("Slot available after: 2016-11-20T18:08:05Z, in 25 seconds.").nextAvailableSlotIn);
	}

	public void testParseNoAvailableSlotsMultiple()
	{
		assertEquals(25, (int) parse(
				"Slot available after: 2016-11-20T18:08:05Z, in 25 seconds.\n" +
				"Slot available after: 2016-11-20T20:08:05Z, in 564 seconds.\n").nextAvailableSlotIn);
	}

	private OverpassStatus parse(String xml)
	{
		try
		{
			InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
			return new OverpassStatusParser().parse(in);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}