package de.westnordost.streetcomplete.quests.bikeway;

import android.os.Bundle;
import android.support.annotation.AnyThread;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.ElementGeometry;
import de.westnordost.streetcomplete.data.osm.tql.FiltersParser;
import de.westnordost.streetcomplete.data.osm.tql.TagFilterExpression;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.util.SphericalEarthMath;
import de.westnordost.streetcomplete.view.CompassView;
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

	private static final String
			DEFINE_BOTH_SIDES = "define_both_sides";

	private static final TagFilterExpression LIKELY_NO_BICYCLE_CONTRAFLOW = new FiltersParser().parse(
			"ways with oneway:bicycle != no and " +
			" (oneway ~ yes|-1 and highway ~ primary|secondary|tertiary or junction=roundabout)");


	private StreetSideSelectPuzzle puzzle;
	private CompassView compassView;
	private float wayOrientationAtCenter;

	private boolean isDefiningBothSides;

	private Cycleway leftSide;
	private Cycleway rightSide;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle inState)
	{
		View view = super.onCreateView(inflater, container, inState);
		setContentView(R.layout.quest_cycleway);

		puzzle = view.findViewById(R.id.puzzle);
		puzzle.setListener(this::showCyclewaySelectionDialog);

		compassView = view.findViewById(R.id.compassNeedle);

		wayOrientationAtCenter = getWayOrientationAtCenterLineInDegrees(getElementGeometry());

		initPuzzleDisplay(inState);
		initPuzzleImages(inState);

		return view;
	}

	private void initPuzzleDisplay(Bundle inState)
	{
		if(inState != null)
		{
			isDefiningBothSides = inState.getBoolean(DEFINE_BOTH_SIDES);
		}
		else
		{
			isDefiningBothSides = !LIKELY_NO_BICYCLE_CONTRAFLOW.matches(getOsmElement());
		}

		if(!isDefiningBothSides)
		{
			if(isLeftHandTraffic()) puzzle.showOnlyLeftSide();
			else                    puzzle.showOnlyRightSide();

			addOtherAnswer(R.string.quest_cycleway_answer_contraflow_cycleway, this::showBothSides);
		}
	}

	private void initPuzzleImages(Bundle inState)
	{
		int defaultResId = isLeftHandTraffic() ?
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
		outState.putBoolean(DEFINE_BOTH_SIDES, isDefiningBothSides);
	}

	@AnyThread public void onMapOrientation(final float rotation, final float tilt)
	{
		final float rotationInDegrees = (float) (rotation * 180 / Math.PI);
		getActivity().runOnUiThread(() ->
		{
			if(puzzle != null)
			{
				puzzle.setStreetRotation(wayOrientationAtCenter + rotationInDegrees);
			}
			if(compassView != null)
			{
				compassView.setOrientation(rotation, tilt);
			}
		});
	}

	private static float getWayOrientationAtCenterLineInDegrees(ElementGeometry e)
	{
		if(e.polylines == null) return 0;

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
		if(leftSide == null && rightSide == null)
		{
			Toast.makeText(getActivity(), R.string.no_changes, Toast.LENGTH_SHORT).show();
			return;
		}
		else if(isDefiningBothSides && (leftSide == null || rightSide == null))
		{
			Toast.makeText(getActivity(), R.string.need_specify_both_sides, Toast.LENGTH_SHORT).show();
			return;
		}

		boolean isOnewayNotForCyclists = false;

		// a cycleway that goes into opposite direction of a oneway street needs special tagging
		Bundle bundle = new Bundle();
		if(isOneway() && leftSide != null && rightSide != null)
		{
			// if the road is oneway=-1, a cycleway that goes opposite to it would be cycleway:oneway=yes
			int reverseDir = isReversedOneway() ? 1 : -1;

			if(isReverseSideRight())
			{
				if(isSingleTrackOrLane(rightSide))
				{
					bundle.putInt(CYCLEWAY_RIGHT_DIR, reverseDir);
				}
				isOnewayNotForCyclists = rightSide != Cycleway.NONE;
			}
			else
			{
				if(isSingleTrackOrLane(leftSide))
				{
					bundle.putInt(CYCLEWAY_LEFT_DIR, reverseDir);
				}
				isOnewayNotForCyclists = leftSide != Cycleway.NONE;
			}

			isOnewayNotForCyclists |= isDualTrackOrLane(leftSide);
			isOnewayNotForCyclists |= isDualTrackOrLane(rightSide);
		}

		if(leftSide != null)  bundle.putString(CYCLEWAY_LEFT, leftSide.name());
		if(rightSide != null) bundle.putString(CYCLEWAY_RIGHT, rightSide.name());
		bundle.putBoolean(IS_ONEWAY_NOT_FOR_CYCLISTS, isOnewayNotForCyclists);
		applyFormAnswer(bundle);
	}

	private static boolean isSingleTrackOrLane(Cycleway cycleway)
	{
		return cycleway == Cycleway.TRACK || cycleway == Cycleway.EXCLUSIVE_LANE;
	}

	private static boolean isDualTrackOrLane(Cycleway cycleway)
	{
		return cycleway == Cycleway.DUAL_TRACK || cycleway == Cycleway.DUAL_LANE;
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

		recyclerView.setAdapter(createAdapter(getCyclewayItems(isRight), cycleway ->
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
		}));
		alertDialog.show();
	}

	private List<Cycleway> getCyclewayItems(boolean isRight)
	{
		List<Cycleway> values = new ArrayList<>(Arrays.asList(Cycleway.getDisplayValues()));
		// different wording for a contraflow lane that is marked like a "shared" lane (just bicycle pictogram)
		if(isOneway() && isReverseSideRight() == isRight)
		{
			Collections.replaceAll(values, Cycleway.PICTOGRAMS, Cycleway.NONE_NO_ONEWAY);
		}
		String country = getCountryInfo().getCountryCode();
		if("BE".equals(country))
		{
			// Belgium does not make a difference between continuous and dashed lanes -> so don't tag that difference
			// also, in Belgium there is a differentiation between the normal lanes and suggestion lanes
			values.remove(Cycleway.EXCLUSIVE_LANE);
			values.remove(Cycleway.ADVISORY_LANE);
			values.add(0, Cycleway.LANE_UNSPECIFIED);
			values.add(1, Cycleway.SUGGESTION_LANE);
		}
		else if("NL".equals(country))
		{
			// a differentiation between dashed lanes and suggestion lanes only exist in NL and BE
			values.add(values.indexOf(Cycleway.ADVISORY_LANE)+1, Cycleway.SUGGESTION_LANE);
		}

		return values;
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
						int resId = item.getIconResId(isLeftHandTraffic());
						iconView.setImageDrawable(getCurrentCountryResources().getDrawable(resId));
						textView.setText(item.nameResId);
						itemView.setOnClickListener(view -> callback.onCyclewaySelected(item));
					}
				};
			}
		};
	}

	private void showBothSides()
	{
		isDefiningBothSides = true;
		puzzle.showBothSides();
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
