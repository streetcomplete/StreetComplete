package de.westnordost.streetcomplete.data.osm.download;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.*;

public class OverpassStatusParserTest
{
	@Test public void parseRateLimit()
	{
		assertEquals(2, parse("Rate limit: 2").maxAvailableSlots);
	}

	@Test public void parseAvailableSlots()
	{
		assertEquals(33, parse("33 slots available now.").availableSlots);
	}

	@Test public void parseNoAvailableSlots()
	{
		assertEquals(25, (int) parse("Slot available after: 2016-11-20T18:08:05Z, in 25 seconds.").nextAvailableSlotIn);
	}

	@Test public void parseNoAvailableSlotsMultiple()
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
