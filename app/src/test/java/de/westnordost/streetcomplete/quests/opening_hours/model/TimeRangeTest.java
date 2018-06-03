package de.westnordost.streetcomplete.quests.opening_hours.model;

import junit.framework.TestCase;

public class TimeRangeTest extends TestCase
{
	public void testIntersect()
	{
		TimeRange tr = new TimeRange(10,14);
		TimeRange directlyAfter = new TimeRange(14,16);
		TimeRange clearlyAfter = new TimeRange(17,18);
		TimeRange directlyBefore = new TimeRange(8,10);
		TimeRange clearlyBefore = new TimeRange(4,8);
		TimeRange within = new TimeRange(11,12);
		TimeRange intersectsLowerSection = new TimeRange(6,12);
		TimeRange intersectUpperSection = new TimeRange(12,20);
		TimeRange loopsOutside = new TimeRange(20,4);
		TimeRange loopsInside = new TimeRange(20,12);

		assertTrue(tr.intersects(tr));
		assertFalse(tr.intersects(directlyAfter));
		assertFalse(tr.intersects(clearlyAfter));
		assertFalse(tr.intersects(directlyBefore));
		assertFalse(tr.intersects(clearlyBefore));
		assertTrue(tr.intersects(within));
		assertTrue(tr.intersects(intersectsLowerSection));
		assertTrue(tr.intersects(intersectUpperSection));
		assertFalse(tr.intersects(loopsOutside));
		assertTrue(tr.intersects(loopsInside));
	}

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
