package de.westnordost.streetcomplete.quests.opening_hours.model;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class OpeningWeekdaysTest
{
	private static final Weekdays MONDAY = new Weekdays(new boolean[]{true});
	private static final Weekdays TUESDAY = new Weekdays(new boolean[]{false, true});

	@Test public void intersectionOnSameWeekday()
	{
		assertFalse(
			days(MONDAY,hours(2,6)).intersects(
			days(TUESDAY, hours(3,5)))
		);
	}

	@Test public void noIntersectionOnSameWeekdays()
	{
		assertFalse(
			days(MONDAY, hours(2,6)).intersects(
			days(MONDAY, hours(8,10))
		));
	}

	@Test public void oneIntersection()
	{
		assertTrue(
			days(MONDAY, hours(8,15)).intersects(
			days(MONDAY, hours(14,18))
		));
	}

	@Test public void oneOfManyIntersection()
	{
		assertTrue(
			days(MONDAY, hours(2,8), hours(8,10)).intersects(
			days(MONDAY, hours(14,18), hours(9,12))
		));
	}

	@Test public void selfIntersecting()
	{
		assertTrue(days(MONDAY,hours(2,8),hours(6,9)).isSelfIntersecting());
	}

	@Test public void notSelfIntersecting()
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
