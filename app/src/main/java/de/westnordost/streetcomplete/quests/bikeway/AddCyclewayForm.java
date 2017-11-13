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
			CYCLEWAY_RIGHT_DIR = "cycleway_right_opposite",
			IS_ONEWAY_NOT_FOR_CYCLISTS = "oneway_not_for_cyclists";

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
		puzzle.setListener(new StreetSideSelectPuzzle.OnClickSideListener()
		{
			@Override public void onClick(boolean isRight) { showCyclewaySelectionDialog(isRight); }
		});

		wayOrientationAtCenter = getWayOrientationAtCenterLineInDegrees(getElementGeometry());

		restoreInstanceState(inState);

		return view;
	}

	private void restoreInstanceState(Bundle inState)
	{
		int defaultResId = getCountryInfo().isLeftHandTraffic() ?
				R.drawable.ic_cycleway_unknown_l : R.drawable.ic_cycleway_unknown;
		if(inState != null)
		{
			String rightSideString = inState.getString(CYCLEWAY_RIGHT);
			if(rightSideString != null)
			{
				rightSide = Cycleway.valueOf(rightSideString);
				puzzle.setRightSideImageResource(rightSide.getIconResId(isLeftHandTraffic()));
			}
			else
			{
				puzzle.setRightSideImageResource(defaultResId);
			}
			String leftSideString = inState.getString(CYCLEWAY_LEFT);
			if(leftSideString != null)
			{
				leftSide = Cycleway.valueOf(leftSideString);
				puzzle.setLeftSideImageResource(leftSide.getIconResId(isLeftHandTraffic()));
			}
			else
			{
				puzzle.setLeftSideImageResource(defaultResId);
			}
		}
		else
		{
			puzzle.setLeftSideImageResource(defaultResId);
			puzzle.setRightSideImageResource(defaultResId);
		}
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
			List<LatLon> centerLine = SphericalEarthMath.centerLineOf(points);
			if(centerLine != null)
			{
				return (float) SphericalEarthMath.bearing(centerLine.get(0), centerLine.get(1));
			}
		}
		return 0;
	}

	@Override protected void onClickOk()
	{
		if(leftSide == null || rightSide == null)
		{
			Toast.makeText(getActivity(), R.string.no_changes, Toast.LENGTH_SHORT).show();
			return;
		}

		boolean isOnewayNotForCyclists = false;

		// a cycleway that goes into opposite direction of a oneway street needs special tagging
		Bundle bundle = new Bundle();
		if(isOneway())
		{
			// if the road is oneway=-1, a cycleway that goes opposite to it would be cycleway:oneway=yes
			int reverseDir = isReversedOneway() ? 1 : -1;

			if(isReverseSideRight())
			{
				if(isSingleTrackOrLane(rightSide))
				{
					bundle.putInt(CYCLEWAY_RIGHT_DIR, reverseDir);
					isOnewayNotForCyclists = true;
				}
			}
			else
			{
				if(isSingleTrackOrLane(leftSide))
				{
					bundle.putInt(CYCLEWAY_LEFT_DIR, reverseDir);
					isOnewayNotForCyclists = true;
				}
			}

			isOnewayNotForCyclists |= isDualTrackOrLane(leftSide);
			isOnewayNotForCyclists |= isDualTrackOrLane(rightSide);
		}

		bundle.putString(CYCLEWAY_LEFT, leftSide.name());
		bundle.putString(CYCLEWAY_RIGHT, rightSide.name());
		bundle.putBoolean(IS_ONEWAY_NOT_FOR_CYCLISTS, isOnewayNotForCyclists);
		applyFormAnswer(bundle);
	}

	private static boolean isSingleTrackOrLane(Cycleway cycleway)
	{
		return cycleway == Cycleway.TRACK || cycleway == Cycleway.LANE;
	}

	private static boolean isDualTrackOrLane(Cycleway cycleway)
	{
		return cycleway == Cycleway.TRACK_DUAL || cycleway == Cycleway.LANE_DUAL;
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
			@Override public ViewHolder<Cycleway> onCreateViewHolder(ViewGroup parent, int viewType)
			{
				return new ViewHolder<Cycleway>(LayoutInflater.from(parent.getContext()).inflate(
						R.layout.labeled_icon_button_cell, parent, false))
				{
					@Override protected void onBind(final Cycleway item)
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
	 *          one-way is on the right side of the way */
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
