package de.westnordost.streetcomplete.quests.bikeway;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.util.SphericalEarthMath;
import de.westnordost.streetcomplete.view.ListAdapter;
import de.westnordost.streetcomplete.view.StreetSideSelectPuzzle;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

public class AddCyclewayForm extends AbstractQuestFormAnswerFragment
{
	public static final String
			CYCLEWAY_LEFT = "cycleway_left",
			CYCLEWAY_RIGHT = "cycleway_right",
			CYCLEWAY_LEFT_DIR = "cycleway_left_opposite",
			CYCLEWAY_RIGHT_DIR = "cycleway_right_opposite";

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

	private StreetSideSelectPuzzle puzzle;
	private float wayOrientationAtCenter;

	private Cycleway leftSide;
	private Cycleway rightSide;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle inState)
	{
		View view = super.onCreateView(inflater, container, inState);
		setContentView(R.layout.quest_cycleway);

		puzzle = view.findViewById(R.id.puzzle);
		int defaultResId = getCountryInfo().isLeftHandTraffic() ?
				R.drawable.ic_cycleway_unknown_l : R.drawable.ic_cycleway_unknown;

		puzzle.setDefaultStreetSideImageResource(defaultResId);
		puzzle.setListener(new StreetSideSelectPuzzle.OnClickSideListener()
		{
			@Override public void onClick(boolean isRight) { showCyclewaySelectionDialog(isRight); }
		});

		wayOrientationAtCenter = getWayOrientationAtCenterLineInDegrees(getElementGeometry());

		if(inState != null)
		{
			String rightSideString = inState.getString(CYCLEWAY_RIGHT);
			if(rightSideString != null)
			{
				rightSide = Cycleway.valueOf(rightSideString);
				puzzle.setRightSideImageResource(rightSide.getIconResId(isLeftHandTraffic()));
			}
			String leftSideString = inState.getString(CYCLEWAY_LEFT);
			if(leftSideString != null)
			{
				leftSide = Cycleway.valueOf(leftSideString);
				puzzle.setLeftSideImageResource(leftSide.getIconResId(isLeftHandTraffic()));
			}
		}

		return view;
	}

	@Override public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if(rightSide != null) outState.putString(CYCLEWAY_RIGHT, rightSide.name());
		if(leftSide != null)  outState.putString(CYCLEWAY_LEFT, leftSide.name());
	}

	public void onMapOrientation(float rotation, float tilt)
	{
		if(puzzle == null) return;
		float rotationInDegrees = (float) (rotation * 180 / Math.PI);
		puzzle.setStreetRotation(wayOrientationAtCenter + rotationInDegrees);
	}

	private static float getWayOrientationAtCenterLineInDegrees(ElementGeometry e)
	{
		List<LatLon> points = e.polylines.get(0);
		if(points != null && points.size() > 1)
		{
			List<LatLon> centerLine = findCenterLineOfPolyLine(points);
			if(centerLine != null)
			{
				return (float) SphericalEarthMath.bearing(centerLine.get(0), centerLine.get(1));
			}
		}
		return 0;
	}

	private static List<LatLon> findCenterLineOfPolyLine(List<LatLon> positions)
	{
		double halfDistance = getLengthInMeters(positions) / 2;
		for(int i = 0; i < positions.size() -1; i++)
		{
			LatLon pos0 = positions.get(i);
			LatLon pos1 = positions.get(i+1);
			halfDistance -= SphericalEarthMath.distance(pos0, pos1);
			if(halfDistance > 0) continue;

			List<LatLon> result = new ArrayList<>(2);
			result.add(pos0);
			result.add(pos1);
			return result;
		}
		return null;
	}

	private static double getLengthInMeters(List<LatLon> positions)
	{
		double length = 0;
		for(int i = 0; i < positions.size() -1; i++)
		{
			LatLon p0 = positions.get(i);
			LatLon p1 = positions.get(i+1);
			length += SphericalEarthMath.distance(p0, p1);
		}
		return length;
	}

	@Override protected void onClickOk()
	{
		if(leftSide == null || rightSide == null)
		{
			Toast.makeText(getActivity(), R.string.no_changes, Toast.LENGTH_SHORT).show();
			return;
		}

		// a cycleway that goes into opposite direction of a oneway street needs special tagging
		Bundle bundle = new Bundle();
		if(isOneway())
		{
			// if the road is oneway=-1, a cycleway that goes opposite to it would be cycleway:oneway=yes
			int reverseDir = isReversedOneway() ? 1 : -1;

			if(isReverseSideRight())
			{
				if(rightSide == Cycleway.TRACK || rightSide == Cycleway.LANE)
				{
					bundle.putInt(CYCLEWAY_RIGHT_DIR, reverseDir);
				}
			}
			else
			{
				if(leftSide == Cycleway.TRACK || leftSide == Cycleway.LANE)
				{
					bundle.putInt(CYCLEWAY_LEFT_DIR, reverseDir);
				}
			}
		}

		bundle.putString(CYCLEWAY_LEFT, leftSide.name());
		bundle.putString(CYCLEWAY_RIGHT, rightSide.name());
		applyFormAnswer(bundle);
	}

	@Override public boolean hasChanges()
	{
		return leftSide != null || rightSide != null;
	}

	private void showCyclewaySelectionDialog(final boolean isRight)
	{
		RecyclerView recyclerView = new RecyclerView(getActivity());
		recyclerView.setLayoutParams(new RecyclerView.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

		final AlertDialog alertDialog = new AlertDialogBuilder(getActivity())
				.setTitle(R.string.quest_select_hint)
				.setView(recyclerView)
				.create();

		recyclerView.setAdapter(createAdapter(Arrays.asList(Cycleway.values()), new OnCyclewaySelected()
		{
			@Override public void onCyclewaySelected(Cycleway cycleway)
			{
				alertDialog.dismiss();

				int iconResId = cycleway.getIconResId(isLeftHandTraffic());

				if (isRight)
				{
					puzzle.replaceRightSideImageResource(iconResId);
					rightSide = cycleway;
				}
				else
				{
					puzzle.replaceLeftSideImageResource(iconResId);
					leftSide = cycleway;
				}
			}
		}));
		alertDialog.show();
	}

	private interface OnCyclewaySelected { void onCyclewaySelected(Cycleway cycleway); }
	private ListAdapter<Cycleway> createAdapter(List<Cycleway> items, final OnCyclewaySelected callback)
	{
		return new ListAdapter<Cycleway>(items)
		{
			@Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
			{
				return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
						R.layout.labeled_icon_button_cell, parent, false))
				{
					@Override protected void update(final Cycleway item)
					{
						ImageView iconView = itemView.findViewById(R.id.imageView);
						TextView textView = itemView.findViewById(R.id.textView);
						iconView.setImageResource(item.getIconResId(isLeftHandTraffic()));
						textView.setText(item.nameResId);
						itemView.setOnClickListener(new View.OnClickListener()
						{
							@Override public void onClick(View view)
							{
								callback.onCyclewaySelected(item);
							}
						});
					}
				};
			}
		};
	}

	private boolean isOneway()
	{
		Map<String, String> tags = getOsmElement().getTags();
		String oneway = tags.get("oneway");
		return oneway != null && (oneway.equals("yes") || oneway.equals("-1"));
	}

	/** @return whether the side that goes into the opposite direction of the driving direction of a
	 *          one-way is the right side */
	private boolean isReverseSideRight()
	{
		return isReversedOneway() ^ isLeftHandTraffic();
	}

	private boolean isReversedOneway()
	{
		return "-1".equals(getOsmElement().getTags().get("oneway"));
	}

	// just a shortcut
	private boolean isLeftHandTraffic()
	{
		return getCountryInfo().isLeftHandTraffic();
	}
}
