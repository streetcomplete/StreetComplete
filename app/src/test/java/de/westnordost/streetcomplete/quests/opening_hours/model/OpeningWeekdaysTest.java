package de.westnordost.streetcomplete.quests.opening_hours.model;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class OpeningWeekdaysTest
{
	private static final Weekdays MONDAY = new Weekdays(new boolean[]{true});
	private static final Weekdays TUESDAY = new Weekdays(new boolean[]{false, true});

	@Test public void noIntersectionOnDifferentWeekday()
	{
		assertFalse(intersects(
			days(MONDAY,  hours(2,6)),
			days(TUESDAY, hours(3,5))
		));
	}

	@Test public void noIntersectionOnSameWeekday()
	{
		assertFalse(intersects(
			days(MONDAY, hours(2,6)),
			days(MONDAY, hours(8,10))
		));
	}

	@Test public void oneIntersection()
	{
		assertTrue(intersects(
			days(MONDAY, hours(8,15)),
			days(MONDAY, hours(14,18))
		));
	}

	@Test public void intersectionAtNextDay()
	{
		assertTrue(intersects(
			days(MONDAY,  hours(18,4)),
			days(TUESDAY, hours(2,12))
		));
	}

	@Test public void noIntersectionAtNextDay()
	{
		assertFalse(intersects(
			days(MONDAY,  hours(18,4)),
			days(TUESDAY, hours(12,20))
		));
	}

	@Test public void oneOfManyIntersectionAtNextDay()
	{
		assertTrue(intersects(
			days(MONDAY,  hours(12,16), hours(18,4)),
			days(TUESDAY, hours(20,4),  hours(2,12))
		));
	}

	@Test public void noneOfManyIntersectionAtNextDay()
	{
		assertFalse(intersects(
			days(MONDAY,  hours(12,16), hours(18,4)),
			days(TUESDAY, hours(20,4),  hours(4,12))
		));
	}

	@Test public void oneOfManyIntersectionAtSameDay()
	{
		assertTrue(intersects(
			days(MONDAY, hours(2,8),   hours(8,10)),
			days(MONDAY, hours(14,18), hours(9,12))
		));
	}

	@Test public void noWeekdaysIntersection()
	{
		assertFalse(intersectsWeekdays(
			days(MONDAY,  hours(8,12)),
			days(TUESDAY, hours(8,12))
		));
	}

	@Test public void normalWeekdaysIntersection()
	{
		assertTrue(intersectsWeekdays(
			days(MONDAY,  hours(8,12)),
			days(MONDAY,  hours(16,20))
		));
	}

	@Test public void weekdaysIntersectionAtNextDay()
	{
		assertTrue(intersectsWeekdays(
			days(MONDAY,  hours(20, 4)),
			days(TUESDAY, hours(12,20))
		));
	}

	@Test public void selfIntersecting()
	{
		assertTrue(days(MONDAY, hours(2,8), hours(6,9)).isSelfIntersecting());
	}

	@Test public void notSelfIntersecting()
	{
		assertFalse(days(MONDAY, hours(2,8), hours(8,10)).isSelfIntersecting());
	}

	private static boolean intersects(OpeningWeekdays one, OpeningWeekdays two)
	{
		boolean result1 = one.intersects(two);
		boolean result2 = two.intersects(one);
		if(result1 != result2) fail("Intersection result was not symmetric!");
		return result1;
	}

	private static boolean intersectsWeekdays(OpeningWeekdays one, OpeningWeekdays two)
	{
		boolean result1 = one.intersectsWeekdays(two);
		boolean result2 = two.intersectsWeekdays(one);
		if(result1 != result2) fail("intersectsWeekdays result was not symmetric!");
		return result1;
	}

	private static OpeningWeekdays days(Weekdays weekdays, TimeRange... ranges)
	{
		return new OpeningWeekdays(weekdays, Arrays.asList(ranges));
	}

	private static TimeRange hours(int start, int end)
	{
		return new TimeRange(start*60, end*60);
	}
}
