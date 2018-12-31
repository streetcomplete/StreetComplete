package de.westnordost.streetcomplete.quests.opening_hours.model;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

public class TimeRangeTest
{
	@Test public void intersect()
	{
		TimeRange tr = new TimeRange(10,14,false);
		TimeRange directlyAfter = new TimeRange(14,16,false);
		TimeRange clearlyAfter = new TimeRange(17,18,false);
		TimeRange directlyBefore = new TimeRange(8,10,false);
		TimeRange clearlyBefore = new TimeRange(4,8,false);
		TimeRange within = new TimeRange(11,12,false);
		TimeRange intersectsLowerSection = new TimeRange(6,12,false);
		TimeRange intersectUpperSection = new TimeRange(12,20,false);
		TimeRange loopsOutside = new TimeRange(20,4,false);
		TimeRange loopsInside = new TimeRange(20,12,false);

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

	@Test public void openEndIntersection()
	{
		TimeRange openEnd = new TimeRange(10,50, true);
		TimeRange before = new TimeRange(0,5, false);
		TimeRange after = new TimeRange(60,70, false);
		assertTrue(openEnd.intersects(after));
		assertFalse(openEnd.intersects(before));

		assertFalse(before.intersects(openEnd));
		assertTrue(after.intersects(openEnd));
	}

	@Test public void toStringWorks()
	{
		TimeRange openEnd = new TimeRange(10,80, true);
		assertEquals("00:10-01:20+", openEnd.toString());
		assertEquals("00:10 till 01:20+", openEnd.toStringUsing(Locale.US, " till "));
		assertEquals("00:00+", new TimeRange(0,0,true).toString());
	}
}
