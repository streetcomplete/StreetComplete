package de.westnordost.streetcomplete.quests.bikeway;

import de.westnordost.streetcomplete.R;

public enum Cycleway
{
	// some kind of cycle lane, not specified if with continuous or dashed lane markings
	LANE_UNSPECIFIED   ( R.drawable.ic_cycleway_lane,       R.drawable.ic_cycleway_lane_l,        R.string.quest_cycleway_value_lane),
	// a.k.a. exclusive lane, dedicated lane or simply (proper) lane
	EXCLUSIVE_LANE     ( R.drawable.ic_cycleway_lane,       R.drawable.ic_cycleway_lane_l,       R.string.quest_cycleway_value_lane ),
	// a.k.a. protective lane, multipurpose lane, soft lane or recommended lane
	ADVISORY_LANE      ( R.drawable.ic_cycleway_shared_lane, R.drawable.ic_cycleway_shared_lane_l, R.string.quest_cycleway_value_lane_soft),
	// slight difference to dashed lane only made in NL, BE
	SUGGESTION_LANE    ( R.drawable.ic_cycleway_suggestion_lane, R.drawable.ic_cycleway_suggestion_lane, R.string.quest_cycleway_value_suggestion_lane),
	TRACK              ( R.drawable.ic_cycleway_track,      R.drawable.ic_cycleway_track_l,      R.string.quest_cycleway_value_track ),
	NONE               ( R.drawable.ic_cycleway_none,       R.drawable.ic_cycleway_none,         R.string.quest_cycleway_value_none ),
	NONE_NO_ONEWAY     ( R.drawable.ic_cycleway_pictograms, R.drawable.ic_cycleway_pictograms_l, R.string.quest_cycleway_value_none_but_no_oneway ),
	PICTOGRAMS         ( R.drawable.ic_cycleway_pictograms, R.drawable.ic_cycleway_pictograms_l, R.string.quest_cycleway_value_shared ),
	SIDEWALK_EXPLICIT  ( R.drawable.ic_cycleway_sidewalk_explicit, R.drawable.ic_cycleway_sidewalk_explicit_l,    R.string.quest_cycleway_value_sidewalk ),
	SIDEWALK_OK        ( R.drawable.ic_cycleway_sidewalk,   R.drawable.ic_cycleway_sidewalk,     R.string.quest_cycleway_value_sidewalk_allowed),
	DUAL_LANE          ( R.drawable.ic_cycleway_lane_dual,  R.drawable.ic_cycleway_lane_dual_l,  R.string.quest_cycleway_value_lane_dual ),
	DUAL_TRACK         ( R.drawable.ic_cycleway_track_dual, R.drawable.ic_cycleway_track_dual_l, R.string.quest_cycleway_value_track_dual ),
	BUSWAY             ( R.drawable.ic_cycleway_bus_lane,   R.drawable.ic_cycleway_bus_lane_l,   R.string.quest_cycleway_value_bus_lane );

	public final int iconResId;
	public final int iconResIdLeft;
	public final int nameResId;

	// some of the values defined above are special values that should not be visible by default
	public static Cycleway[] getDisplayValues() { return new Cycleway[] {
		EXCLUSIVE_LANE, ADVISORY_LANE,
		TRACK, NONE,
		PICTOGRAMS, BUSWAY,
		SIDEWALK_EXPLICIT, SIDEWALK_OK,
		DUAL_LANE, DUAL_TRACK
	};}

	Cycleway(int iconResId, int iconResIdLeft, int nameResId)
	{
		this.iconResId = iconResId;
		this.iconResIdLeft = iconResIdLeft;
		this.nameResId = nameResId;
	}

	public int getIconResId(boolean isLeftHandTraffic)
	{
		return isLeftHandTraffic ? iconResIdLeft : iconResId;
	}

	public boolean isOnSidewalk()
	{
		return this == SIDEWALK_EXPLICIT || this == SIDEWALK_OK;
	}
}
