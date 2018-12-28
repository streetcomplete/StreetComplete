package de.westnordost.streetcomplete.quests.opening_hours.adapter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.westnordost.streetcomplete.quests.opening_hours.model.CircularSection;
import de.westnordost.streetcomplete.quests.opening_hours.model.OpeningMonths;
import de.westnordost.streetcomplete.quests.opening_hours.model.OpeningWeekdays;
import de.westnordost.streetcomplete.quests.opening_hours.model.TimeRange;
import de.westnordost.streetcomplete.quests.opening_hours.model.Weekdays;

import static org.junit.Assert.*;

@SuppressWarnings("unchecked")
public class OpeningHoursModelCreatorTest
{
	private static final Weekdays MONDAY = new Weekdays(new boolean[]{true});
	private static final Weekdays MONDAY_TUESDAY = new Weekdays(new boolean[]{true, true});
	private static final Weekdays MONDAY_FRIDAY = new Weekdays(new boolean[]{true, true, true, true, true});
	private static final Weekdays TUESDAY = new Weekdays(new boolean[]{false, true});

	private static final TimeRange MORNING = new TimeRange(8*60,12*60);
	private static final TimeRange MIDDAY = new TimeRange(10*60,16*60);
	private static  TimeRange AFTERNOON = new TimeRange(14*60,18*60);
	private static  TimeRange LONG_AFTERNOON = new TimeRange(13*60,20*60);
	private static  TimeRange DUSK_TILL_DAWN = new TimeRange(18*60,6*60);
	private static  TimeRange EARLY_MORNING = new TimeRange(4*60,8*60);

	private static final OpeningWeekdaysRow MONDAY_MORNING = new OpeningWeekdaysRow(MONDAY, MORNING);

	private static final CircularSection ALL_YEAR = new CircularSection(0,11);
	private static final CircularSection JAN_JUN = new CircularSection(0,6);
	private static final CircularSection JUL_DEC = new CircularSection(6,11);

	@Test public void copiesOpeningMonths()
	{
		List<OpeningMonthsRow> viewData = new ArrayList<>();
		viewData.add(new OpeningMonthsRow(JAN_JUN, MONDAY_MORNING));
		viewData.add(new OpeningMonthsRow(JUL_DEC, MONDAY_MORNING));
		List<OpeningMonths> actual = OpeningHoursModelCreator.create(viewData);

		List<OpeningMonths> expected = months(
			new OpeningMonths(JAN_JUN, clusters(weekdays(new OpeningWeekdays(MONDAY, times(MORNING))))),
			new OpeningMonths(JUL_DEC, clusters(weekdays(new OpeningWeekdays(MONDAY, times(MORNING)))))
		);

		assertEquals(expected, actual);
	}

	@Test public void mergesOpeningWeekdaysRowsOfSameDay()
	{
		List<OpeningMonths> actual = create(
			new OpeningWeekdaysRow(MONDAY, MORNING),
			new OpeningWeekdaysRow(MONDAY, AFTERNOON)
		);

		List<OpeningMonths> expected = months(new OpeningMonths(ALL_YEAR, clusters(
			weekdays(new OpeningWeekdays(MONDAY, times(MORNING, AFTERNOON)))
		)));

		assertEquals(expected, actual);
	}

	@Test public void doesNotClusterDifferentWeekdays()
	{
		List<OpeningMonths> actual = create(
			new OpeningWeekdaysRow(MONDAY, MORNING),
			new OpeningWeekdaysRow(TUESDAY, AFTERNOON)
		);

		List<OpeningMonths> expected = months(new OpeningMonths(ALL_YEAR, clusters(
			weekdays(new OpeningWeekdays(MONDAY, times(MORNING))),
			weekdays(new OpeningWeekdays(TUESDAY, times(AFTERNOON)))
		)));

		assertEquals(expected, actual);
	}

	@Test public void clustersOverlappingWeekdays()
	{
		List<OpeningMonths> actual = create(
			new OpeningWeekdaysRow(MONDAY, MORNING),
			new OpeningWeekdaysRow(MONDAY_TUESDAY, AFTERNOON)
		);

		List<OpeningMonths> expected = months(new OpeningMonths(ALL_YEAR, clusters(
			weekdays(
				new OpeningWeekdays(MONDAY, times(MORNING)),
				new OpeningWeekdays(MONDAY_TUESDAY, times(AFTERNOON))
			)
		)));

		assertEquals(expected, actual);
	}

	@Test public void doesNotClusterIntersectingTimes()
	{
		List<OpeningMonths> actual = create(
			new OpeningWeekdaysRow(MONDAY, MORNING),
			new OpeningWeekdaysRow(MONDAY_TUESDAY, MIDDAY)
		);

		List<OpeningMonths> expected = months(new OpeningMonths(ALL_YEAR, clusters(
			weekdays(new OpeningWeekdays(MONDAY, times(MORNING))),
			weekdays(new OpeningWeekdays(MONDAY_TUESDAY, times(MIDDAY)))
		)));

		assertEquals(expected, actual);
	}

	@Test public void doesNotClusterIntersectingTimesInCluster()
	{
		List<OpeningMonths> actual = create(
			new OpeningWeekdaysRow(MONDAY, MORNING),
			new OpeningWeekdaysRow(MONDAY_TUESDAY, AFTERNOON),
			new OpeningWeekdaysRow(TUESDAY, MIDDAY)
		);

		List<OpeningMonths> expected = months(new OpeningMonths(ALL_YEAR, clusters(
			weekdays(
				new OpeningWeekdays(MONDAY, times(MORNING)),
				new OpeningWeekdays(MONDAY_TUESDAY, times(AFTERNOON))),
			weekdays(
				new OpeningWeekdays(TUESDAY, times(MIDDAY)))
		)));

		assertEquals(expected, actual);
	}

	@Test public void doesClusterMultipleOverlappingWeekdaysWithNonIntersectingTimes()
	{
		List<OpeningMonths> actual = create(
			new OpeningWeekdaysRow(MONDAY_FRIDAY, MORNING),
			new OpeningWeekdaysRow(MONDAY, AFTERNOON),
			new OpeningWeekdaysRow(TUESDAY, LONG_AFTERNOON)
		);

		List<OpeningMonths> expected = months(new OpeningMonths(ALL_YEAR, clusters(
			weekdays(
				new OpeningWeekdays(MONDAY_FRIDAY, times(MORNING)),
				new OpeningWeekdays(MONDAY, times(AFTERNOON)),
				new OpeningWeekdays(TUESDAY, times(LONG_AFTERNOON)))
		)));

		assertEquals(expected, actual);
	}

	@Test public void doesClusterWeekdaysThatOverlapBecauseTimeRangeExtendsToNextDay()
	{
		List<OpeningMonths> actual = create(
			new OpeningWeekdaysRow(MONDAY, DUSK_TILL_DAWN),
			new OpeningWeekdaysRow(TUESDAY, AFTERNOON)
		);

		List<OpeningMonths> expected = months(new OpeningMonths(ALL_YEAR, clusters(
			weekdays(
				new OpeningWeekdays(MONDAY, times(DUSK_TILL_DAWN)),
				new OpeningWeekdays(TUESDAY, times(AFTERNOON)))
		)));

		assertEquals(expected, actual);
	}

	@Test public void doesNotClusterWeekdaysThatOverlapBecauseTimeRangeExtendsToNextDayWithOverlappingTimes()
	// nnnnewww function name record!!! 🎉
	{
		List<OpeningMonths> actual = create(
			new OpeningWeekdaysRow(MONDAY, DUSK_TILL_DAWN),
			new OpeningWeekdaysRow(TUESDAY, EARLY_MORNING)
		);

		List<OpeningMonths> expected = months(new OpeningMonths(ALL_YEAR, clusters(
			weekdays(new OpeningWeekdays(MONDAY, times(DUSK_TILL_DAWN))),
			weekdays(new OpeningWeekdays(TUESDAY, times(EARLY_MORNING)))
		)));

		assertEquals(expected, actual);
	}

	private static List<OpeningMonths> create(OpeningWeekdaysRow ... rows)
	{
		List<OpeningMonthsRow> viewData = new ArrayList<>();
		OpeningMonthsRow omr = new OpeningMonthsRow();
		List<OpeningWeekdaysRow> owrs = omr.weekdaysList;
		owrs.addAll(Arrays.asList(rows));
		viewData.add(omr);
		return OpeningHoursModelCreator.create(viewData);
	}

	private static List<OpeningMonths> months(OpeningMonths ... ranges)
	{
		return Arrays.asList(ranges);
	}

	private static List<TimeRange> times(TimeRange ... ranges)
	{
		return Arrays.asList(ranges);
	}

	private static List<OpeningWeekdays> weekdays(OpeningWeekdays ... ranges)
	{
		return Arrays.asList(ranges);
	}

	private static List<List<OpeningWeekdays>> clusters(List<OpeningWeekdays> ... ranges)
	{
		return Arrays.asList(ranges);
	}
}
