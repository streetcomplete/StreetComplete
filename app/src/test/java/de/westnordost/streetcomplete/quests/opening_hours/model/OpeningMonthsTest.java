package de.westnordost.streetcomplete.quests.opening_hours.model;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

public class OpeningMonthsTest extends TestCase
{
	private static final boolean l = true;
	private static final boolean o = false;

	private static final Weekdays MONDAY = new Weekdays(new boolean[]{l});
	private static final Weekdays MONDAY_FRIDAY = new Weekdays(new boolean[]{l,l,l,l,l});
	private static final Weekdays SATURDAY_SUNDAY = new Weekdays(new boolean[]{o,o,o,o,o,l,l});

	private static final CircularSection WHOLE_YEAR = new CircularSection(0,11);
	private static final CircularSection JUNE_SEPTEMBER = new CircularSection(5,8);

	public void testOmitMonthsIfWholeYear()
	{
		assertEquals("Mo 09:00-17:00",
			months(WHOLE_YEAR, cluster(days(MONDAY, hours(9,17)))).toString());

		assertEquals("Mo-Fr 09:00-17:00; Sa,Su 09:00-12:00",
			months(
				WHOLE_YEAR,
				cluster(days(MONDAY_FRIDAY, hours(9,17))),
				cluster(days(SATURDAY_SUNDAY, hours(9,12)))
			).toString());
	}

	public void testPrependMonthsBeforeEveryCluster()
	{
		assertEquals("Jun-Sep: Mo 09:00-17:00",
			months(JUNE_SEPTEMBER, cluster(days(MONDAY, hours(9,17)))).toString());

		assertEquals("Jun-Sep: Mo-Fr 09:00-17:00; Jun-Sep: Sa,Su 09:00-12:00",
			months(
				JUNE_SEPTEMBER,
				cluster(days(MONDAY_FRIDAY, hours(9,17))),
				cluster(days(SATURDAY_SUNDAY, hours(9,12)))
			).toString());
	}

	public void testPrependMonthsBeforeEveryWeekdays()
	{
		assertEquals("Jun-Sep: Mo-Fr 09:00-17:00, Jun-Sep: Sa,Su 09:00-12:00",
			months(
				JUNE_SEPTEMBER,
				cluster(
					days(MONDAY_FRIDAY, hours(9,17)),
					days(SATURDAY_SUNDAY, hours(9,12))
				)
			).toString());
	}

	private static OpeningMonths months(CircularSection months, List<OpeningWeekdays> ... clusters)
	{
		return new OpeningMonths(months, Arrays.asList(clusters));
	}

	private static List<OpeningWeekdays> cluster(OpeningWeekdays ... weekdays)
	{
		return Arrays.asList(weekdays);
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
