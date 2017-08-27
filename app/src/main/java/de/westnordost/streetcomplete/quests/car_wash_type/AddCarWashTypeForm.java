package de.westnordost.streetcomplete.quests.car_wash_type;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;

public class AddCarWashTypeForm extends ImageListQuestAnswerFragment
{
	public static final String AUTOMATED = "AUTOMATED";
	public static final String SELF_SERVICE = "SELF_SERVICE";
	public static final String OTHER_ANSWER = "OTHER_ANSWER";
	public static final String SERVICE = "SERVICE";

	private final ImageListQuestAnswerFragment.OsmItem[] TYPES = new ImageListQuestAnswerFragment.OsmItem[] {
			new ImageListQuestAnswerFragment.OsmItem(AUTOMATED, R.drawable.car_wash_automated, R.string.quest_carWashType_automated),
			new ImageListQuestAnswerFragment.OsmItem(SELF_SERVICE, R.drawable.car_wash_self_service, R.string.quest_carWashType_selfService)
	};

	@Override protected ImageListQuestAnswerFragment.OsmItem[] getItems()
	{
		return TYPES;
	}

	@Override protected int getItemsPerRow()
	{
		return 2;
	}

	@Override protected int getMaxSelectableItems()
	{
		return -1;
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setTitle(R.string.quest_carWashType_title);
		addOtherAnswers();
		return view;
	}

	private void addOtherAnswers()
	{
		addOtherAnswer(R.string.quest_carWashType_service, new Runnable()
		{
			@Override public void run()
			{
				Bundle answer = new Bundle();
				answer.putString(OTHER_ANSWER, SERVICE);
				applyImmediateAnswer(answer);
			}
		});
	}
}
