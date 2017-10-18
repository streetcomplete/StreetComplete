package de.westnordost.streetcomplete.quests.bikeway;

import de.westnordost.streetcomplete.R;

public enum Cycleway
{
	LANE	   ( R.drawable.ic_cycleway_lane,        R.drawable.ic_cycleway_lane_l,        R.string.quest_cycleway_value_lane ),
	TRACK	   ( R.drawable.ic_cycleway_track,       R.drawable.ic_cycleway_track_l,       R.string.quest_cycleway_value_track ),
	NONE	   ( R.drawable.ic_cycleway_none,        R.drawable.ic_cycleway_none,          R.string.quest_cycleway_value_none ),
	SHARED	   ( R.drawable.ic_cycleway_shared_lane, R.drawable.ic_cycleway_shared_lane_l, R.string.quest_cycleway_value_shared ),
	SIDEWALK   ( R.drawable.ic_cycleway_sidewalk,    R.drawable.ic_cycleway_sidewalk_l,    R.string.quest_cycleway_value_sidewalk ),
	SIDEWALK_OK( R.drawable.ic_cycleway_sidewalk_ok, R.drawable.ic_cycleway_sidewalk_ok,   R.string.quest_cycleway_value_sidewalk_allowed),
	LANE_DUAL  ( R.drawable.ic_cycleway_lane_dual,   R.drawable.ic_cycleway_lane_dual_l,   R.string.quest_cycleway_value_lane_dual ),
	TRACK_DUAL ( R.drawable.ic_cycleway_track_dual,  R.drawable.ic_cycleway_track_dual_l,  R.string.quest_cycleway_value_track_dual ),
	BUSWAY	   ( R.drawable.ic_cycleway_bus_lane,    R.drawable.ic_cycleway_bus_lane_l,    R.string.quest_cycleway_value_bus_lane );

	public final int iconResId;
	public final int iconResIdLeft;
	public final int nameResId;

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
}