package de.westnordost.streetcomplete.quests.opening_hours.adapter;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.westnordost.streetcomplete.quests.opening_hours.model.CircularSection;
import de.westnordost.streetcomplete.quests.opening_hours.model.OpeningMonths;
import de.westnordost.streetcomplete.quests.opening_hours.model.OpeningWeekdays;
import de.westnordost.streetcomplete.quests.opening_hours.model.TimeRange;
import de.westnordost.streetcomplete.quests.opening_hours.model.Weekdays;

@SuppressWarnings("unchecked")
public class OpeningHoursModelCreatorTest extends TestCase
{
	private static final Weekdays MONDAY = new Weekdays(new boolean[]{true});
	private static final Weekdays MONDAY_TUESDAY = new Weekdays(new boolean[]{true, true});
	private static final Weekdays TUESDAY = new Weekdays(new boolean[]{false, true});

	private static final TimeRange MORNING = new TimeRange(8*60,12*60);
	private static final TimeRange MIDDAY = new TimeRange(10*60,16*60);
	private static  TimeRange AFTERNOON = new TimeRange(14*60,18*60);

	private static final OpeningWeekdaysRow MONDAY_MORNING = new OpeningWeekdaysRow(MONDAY, MORNING);

	private static final CircularSection ALL_YEAR = new CircularSection(0,11);
	private static final CircularSection JAN_JUN = new CircularSection(0,6);
	private static final CircularSection JUL_DEC = new CircularSection(6,11);

	public void testCopiesOpeningMonths()
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

	public void testMergesOpeningWeekdaysRowsOfSameDay()
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

	public void testDoesNotClusterDifferentWeekdays()
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

	public void testClustersOverlappingWeekdays()
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

	public void testDoesNotClusterIntersectingTimes()
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

	public void testDoesNotClusterIntersectingTimesInCluster()
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
