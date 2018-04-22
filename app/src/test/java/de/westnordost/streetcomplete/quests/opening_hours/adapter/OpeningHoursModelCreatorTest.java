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

public class OpeningHoursModelCreatorTest extends TestCase
{
	private static final Weekdays MONDAY = new Weekdays(new boolean[]{true});
	private static final Weekdays MONDAY_TUESDAY = new Weekdays(new boolean[]{true, true});
	private static final Weekdays TUESDAY = new Weekdays(new boolean[]{false, true});

	private static final TimeRange MORNING = new TimeRange(8*60,12*60);
	private static  TimeRange AFTERNOON = new TimeRange(14*60,18*60);

	private static final OpeningWeekdaysRow MONDAY_MORNING = new OpeningWeekdaysRow(MONDAY, MORNING);

	public void testCopiesOpeningMonths()
	{
		List<OpeningMonthsRow> viewData = new ArrayList<>();
		viewData.add(new OpeningMonthsRow(new CircularSection(0,6), MONDAY_MORNING));
		viewData.add(new OpeningMonthsRow(new CircularSection(6,11), MONDAY_MORNING));
		List<OpeningMonths> data = OpeningHoursModelCreator.create(viewData);
		assertEquals(2, data.size());
		assertEquals(viewData.get(0).months, data.get(0).months);
		assertEquals(viewData.get(1).months, data.get(1).months);
	}

	public void testCopiesWeekdays()
	{
		List<OpeningMonthsRow> viewData = create(
			new OpeningWeekdaysRow(MONDAY, MORNING),
			new OpeningWeekdaysRow(MONDAY_TUESDAY, AFTERNOON)
		);

		List<OpeningMonths> data = OpeningHoursModelCreator.create(viewData);
		List<OpeningWeekdaysRow> owrs = viewData.get(0).weekdaysList;
		assertEquals(1, data.size());
		OpeningMonths om = data.get(0);
		assertEquals(1, om.weekdaysClusters.size());
		List<OpeningWeekdays> ows = om.weekdaysClusters.get(0);
		assertEquals(owrs.get(0).weekdays, ows.get(0).weekdays);
		assertEquals(owrs.get(1).weekdays, ows.get(1).weekdays);

		assertEquals(1, ows.get(0).timeRanges.size());
		assertEquals(owrs.get(0).timeRange, ows.get(0).timeRanges.get(0));
		assertEquals(1, ows.get(1).timeRanges.size());
		assertEquals(owrs.get(1).timeRange, ows.get(1).timeRanges.get(0));
	}

	public void testMergesOpeningWeekdaysRowsOfSameDays()
	{
		List<OpeningMonthsRow> viewData = create(
			new OpeningWeekdaysRow(MONDAY, MORNING),
			new OpeningWeekdaysRow(MONDAY, AFTERNOON)
		);

		List<OpeningMonths> oms = OpeningHoursModelCreator.create(viewData);
		assertEquals(1, oms.size());
		OpeningMonths om = oms.get(0);
		assertEquals(1, om.weekdaysClusters.size());
		List<OpeningWeekdays> weekdays = om.weekdaysClusters.get(0);
		assertEquals(1, weekdays.size());
		OpeningWeekdays ow = weekdays.get(0);
		List<OpeningWeekdaysRow> owrs = viewData.get(0).weekdaysList;
		assertEquals(owrs.get(0).timeRange, ow.timeRanges.get(0));
		assertEquals(owrs.get(1).timeRange, ow.timeRanges.get(1));
	}

	public void testDoesNotClusterDifferentWeekdays()
	{
		List<OpeningMonthsRow> viewData = create(
			new OpeningWeekdaysRow(MONDAY, MORNING),
			new OpeningWeekdaysRow(TUESDAY, AFTERNOON)
		);

		List<OpeningMonths> oms = OpeningHoursModelCreator.create(viewData);
		assertEquals(1, oms.size());
		OpeningMonths om = oms.get(0);
		assertEquals(2, om.weekdaysClusters.size());
	}

	public void testClustersOverlappingWeekdays()
	{
		List<OpeningMonthsRow> viewData = create(
			new OpeningWeekdaysRow(MONDAY, MORNING),
			new OpeningWeekdaysRow(MONDAY_TUESDAY, AFTERNOON)
		);

		List<OpeningMonths> oms = OpeningHoursModelCreator.create(viewData);
		assertEquals(1, oms.size());
		OpeningMonths om = oms.get(0);
		assertEquals(1, om.weekdaysClusters.size());
	}

	public void testDoesNotClusterIntersectingTimes()
	{
		List<OpeningMonthsRow> viewData = create(
			new OpeningWeekdaysRow(MONDAY, MORNING),
			new OpeningWeekdaysRow(MONDAY_TUESDAY, MORNING)
		);

		List<OpeningMonths> oms = OpeningHoursModelCreator.create(viewData);
		assertEquals(1, oms.size());
		OpeningMonths om = oms.get(0);
		assertEquals(2, om.weekdaysClusters.size());
	}

	public void testDoesNotClusterIntersectingTimesInCluster()
	{
		List<OpeningMonthsRow> viewData = create(
			new OpeningWeekdaysRow(MONDAY, MORNING),
			new OpeningWeekdaysRow(MONDAY_TUESDAY, AFTERNOON),
			new OpeningWeekdaysRow(TUESDAY, AFTERNOON)
		);

		List<OpeningMonths> oms = OpeningHoursModelCreator.create(viewData);
		assertEquals(1, oms.size());
		OpeningMonths om = oms.get(0);
		List<List<OpeningWeekdays>> clusters = om.weekdaysClusters;
		assertEquals(2, clusters.size());
		assertEquals(2, clusters.get(0).size());
		assertEquals(MONDAY, clusters.get(0).get(0).weekdays);
		assertEquals(MONDAY_TUESDAY, clusters.get(0).get(1).weekdays);
		assertEquals(1, clusters.get(1).size());
		assertEquals(TUESDAY, clusters.get(1).get(0).weekdays);
	}

	private static List<OpeningMonthsRow> create(OpeningWeekdaysRow ... rows)
	{
		List<OpeningMonthsRow> viewData = new ArrayList<>();
		OpeningMonthsRow omr = new OpeningMonthsRow();
		List<OpeningWeekdaysRow> owrs = omr.weekdaysList;
		owrs.addAll(Arrays.asList(rows));
		viewData.add(omr);
		return viewData;
	}
}
