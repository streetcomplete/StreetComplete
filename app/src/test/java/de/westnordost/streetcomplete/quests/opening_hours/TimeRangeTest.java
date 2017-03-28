package de.westnordost.streetcomplete.quests.opening_hours;

import junit.framework.TestCase;

public class TimeRangeTest extends TestCase
{
	public void testOpenEndIntersection()
	{
		TimeRange openEnd = new TimeRange(10,50, true);
		TimeRange before = new TimeRange(0,5, false);
		TimeRange after = new TimeRange(60,70, false);
		assertTrue(openEnd.intersects(after));
		assertFalse(openEnd.intersects(before));

		assertFalse(before.intersects(openEnd));
		assertTrue(after.intersects(openEnd));
	}

	public void testToString()
	{
		TimeRange openEnd = new TimeRange(10,80, true);
		assertEquals("00:10-01:20+", openEnd.toString());
		assertEquals("00:10 till 01:20+", openEnd.toStringUsing(" till "));
		assertEquals("00:00+", new TimeRange(0,0,true).toString());
	}
}
