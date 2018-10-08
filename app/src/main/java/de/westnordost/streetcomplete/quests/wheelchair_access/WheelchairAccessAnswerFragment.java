package de.westnordost.streetcomplete.quests.wheelchair_access;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class WheelchairAccessAnswerFragment extends AbstractQuestAnswerFragment
{
	public static final String ANSWER = "answer";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		View buttonPanel = setButtonsView(R.layout.quest_buttonpanel_yes_limited_no);

		buttonPanel.findViewById(R.id.buttonYes).setOnClickListener(v -> onClickAnswer("yes"));
		buttonPanel.findViewById(R.id.buttonLimited).setOnClickListener(v -> onClickAnswer("limited"));
		buttonPanel.findViewById(R.id.buttonNo).setOnClickListener(v -> onClickAnswer("no"));
		return view;
	}

	protected void onClickAnswer(String answer)
	{
		Bundle bundle = new Bundle();
		bundle.putString(ANSWER, answer);
		applyAnswer(bundle);
	}
}
