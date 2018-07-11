package de.westnordost.streetcomplete.quests.opening_hours.model;

import junit.framework.TestCase;

import java.util.Arrays;

public class OpeningWeekdaysTest extends TestCase
{
	private static final Weekdays MONDAY = new Weekdays(new boolean[]{true});
	private static final Weekdays TUESDAY = new Weekdays(new boolean[]{false, true});

	public void testIntersectionOnSameWeekday()
	{
		assertFalse(
			days(MONDAY,hours(2,6)).intersects(
			days(TUESDAY, hours(3,5)))
		);
	}

	public void testNoIntersectionOnSameWeekdays()
	{
		assertFalse(
			days(MONDAY, hours(2,6)).intersects(
			days(MONDAY, hours(8,10))
		));
	}

	public void testOneIntersection()
	{
		assertTrue(
			days(MONDAY, hours(8,15)).intersects(
			days(MONDAY, hours(14,18))
		));
	}

	public void testOneOfManyIntersection()
	{
		assertTrue(
			days(MONDAY, hours(2,8), hours(8,10)).intersects(
			days(MONDAY, hours(14,18), hours(9,12))
		));
	}

	public void testSelfIntersecting()
	{
		assertTrue(days(MONDAY,hours(2,8),hours(6,9)).isSelfIntersecting());
	}

	public void testNotSelfIntersecting()
	{
		assertFalse(days(MONDAY,hours(2,8),hours(8,10)).isSelfIntersecting());
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
