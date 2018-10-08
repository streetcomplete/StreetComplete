package de.westnordost.streetcomplete.quests.oneway;

import android.os.Bundle;
import android.support.annotation.AnyThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.StreetSideRotater;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;
import de.westnordost.streetcomplete.view.StreetSideSelectPuzzle;

public class AddOnewayForm extends YesNoQuestAnswerFragment
{
	public static final String WAY_ID = "way_id";

	@Inject WayTrafficFlowDao db;
	private StreetSideRotater streetSideRotater;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle inState)
	{
		Injector.instance.getApplicationComponent().inject(this);

		View view = super.onCreateView(inflater, container, inState);
		setContentView(R.layout.quest_street_side_puzzle);
		setNoContentPadding();

		View compassNeedle = view.findViewById(R.id.compassNeedle);
		StreetSideSelectPuzzle puzzle = view.findViewById(R.id.puzzle);
		puzzle.showOnlyRightSide();

		boolean isForward = db.isForward(getOsmElement().getId());
		puzzle.setRightSideImageResource(isForward ? R.drawable.ic_oneway_lane : R.drawable.ic_oneway_lane_reverse);

		streetSideRotater = new StreetSideRotater(puzzle, compassNeedle, getElementGeometry());

		return view;
	}

	@Override protected void onClickYesNo(boolean answer)
	{
		Bundle bundle = new Bundle();
		bundle.putBoolean(ANSWER, answer);
		// the quest needs the way ID of the element to find out the direction of the oneway
		bundle.putLong(WAY_ID, getOsmElement().getId());
		applyAnswer(bundle);
	}

	@AnyThread public void onMapOrientation(float rotation, float tilt)
	{
		if(streetSideRotater != null) {
			streetSideRotater.onMapOrientation(rotation, tilt);
		}
	}
}
