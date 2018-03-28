package de.westnordost.streetcomplete.quests;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.quests.bikeway.AddCycleway;
import de.westnordost.streetcomplete.quests.bikeway.AddCyclewayForm;
import de.westnordost.streetcomplete.quests.bikeway.Cycleway;

public class AddCyclewayTest extends AOsmElementQuestTypeTest
{
	public void testCyclewayLeftAndRightDontHaveToBeSpecified1()
	{
		bundle.putString(AddCyclewayForm.CYCLEWAY_LEFT, Cycleway.LANE.name());
		StringMapChangesBuilder cb = new StringMapChangesBuilder(tags);
		createQuestType().applyAnswerTo(bundle, cb);
		// success if no exception thrown
	}

	public void testCyclewayLeftAndRightDontHaveToBeSpecified2()
	{
		bundle.putString(AddCyclewayForm.CYCLEWAY_RIGHT, Cycleway.LANE.name());
		StringMapChangesBuilder cb = new StringMapChangesBuilder(tags);
		createQuestType().applyAnswerTo(bundle, cb);
		// success if no exception thrown
	}

	public void testCyclewayLane()
	{
		putBothSides(Cycleway.LANE);
		verify(new StringMapEntryAdd("cycleway:both", "lane"));
	}

	public void testCyclewayTrack()
	{
		putBothSides(Cycleway.TRACK);
		verify(new StringMapEntryAdd("cycleway:both", "track"));
	}

	public void testCyclewayBusLane()
	{
		putBothSides(Cycleway.BUSWAY);
		verify(new StringMapEntryAdd("cycleway:both", "share_busway"));
	}

	public void testCyclewaySharedLane()
	{
		putBothSides(Cycleway.SHARED);
		verify(new StringMapEntryAdd("cycleway:both", "shared_lane"));
	}

	public void testCyclewayNone()
	{
		putBothSides(Cycleway.NONE);
		verify(new StringMapEntryAdd("cycleway:both", "no"));
	}

	public void testCyclewayOnSidewalk()
	{
		putBothSides(Cycleway.SIDEWALK);
		verify(
				new StringMapEntryAdd("cycleway:both", "track"),
				new StringMapEntryAdd("sidewalk", "both"),
				new StringMapEntryAdd("cycleway:both:segregated", "no")
		);
	}

	public void testCyclewaySidewalkOkay()
	{
		putBothSides(Cycleway.SIDEWALK_OK);
		verify(
				new StringMapEntryAdd("cycleway:both", "no"),
				new StringMapEntryAdd("sidewalk", "both"),
				new StringMapEntryAdd("sidewalk:both:bicycle", "yes")
		);
	}

	public void testCyclewaySidewalkAny()
	{
		bundle.putString(AddCyclewayForm.CYCLEWAY_RIGHT, Cycleway.SIDEWALK.name());
		bundle.putString(AddCyclewayForm.CYCLEWAY_LEFT, Cycleway.SIDEWALK_OK.name());
		verify(
				new StringMapEntryAdd("sidewalk", "both")
		);
	}

	public void testCyclewayDualTrack()
	{
		putBothSides(Cycleway.TRACK_DUAL);
		verify(
				new StringMapEntryAdd("cycleway:both", "track"),
				new StringMapEntryAdd("cycleway:both:oneway", "no")
		);
	}

	public void testCyclewayDualLane()
	{
		putBothSides(Cycleway.LANE_DUAL);
		verify(
				new StringMapEntryAdd("cycleway:both", "lane"),
				new StringMapEntryAdd("cycleway:both:oneway", "no")
		);
	}

	public void testLeftAndRightAreDifferent()
	{
		bundle.putString(AddCyclewayForm.CYCLEWAY_RIGHT, Cycleway.LANE.name());
		bundle.putString(AddCyclewayForm.CYCLEWAY_LEFT, Cycleway.TRACK.name());
		verify(
				new StringMapEntryAdd("cycleway:right", "lane"),
				new StringMapEntryAdd("cycleway:left", "track")
		);
	}

	public void testCyclewayMakesStreetNotOnewayForBicycles()
	{
		putBothSides(Cycleway.LANE);
		bundle.putBoolean(AddCyclewayForm.IS_ONEWAY_NOT_FOR_CYCLISTS, true);
		verify(
				new StringMapEntryAdd("cycleway:both", "lane"),
				new StringMapEntryAdd("oneway:bicycle", "no")
		);
	}

	public void testCyclewayLaneWithExplicitDirection()
	{
		// this would be a street that has lanes on both sides but is oneway=yes (in countries with
		// right hand traffic)
		putBothSides(Cycleway.LANE);
		bundle.putInt(AddCyclewayForm.CYCLEWAY_LEFT_DIR, -1);
		verify(
				new StringMapEntryAdd("cycleway:left", "lane"),
				new StringMapEntryAdd("cycleway:left:oneway", "-1"),
				new StringMapEntryAdd("cycleway:right", "lane")
		);
	}

	public void testCyclewayLaneWithExplicitOtherDirection()
	{
		// this would be a street that has lanes on both sides but is oneway=-1 (in countries with
		// right hand traffic)
		putBothSides(Cycleway.LANE);
		bundle.putInt(AddCyclewayForm.CYCLEWAY_LEFT_DIR, +1);
		verify(
				new StringMapEntryAdd("cycleway:left", "lane"),
				new StringMapEntryAdd("cycleway:left:oneway", "yes"),
				new StringMapEntryAdd("cycleway:right", "lane")
		);
	}

	private void putBothSides(Cycleway cycleway)
	{
		bundle.putString(AddCyclewayForm.CYCLEWAY_RIGHT, cycleway.name());
		bundle.putString(AddCyclewayForm.CYCLEWAY_LEFT, cycleway.name());
	}

	@Override protected OsmElementQuestType createQuestType()
	{
		return new AddCycleway(null);
	}
}
