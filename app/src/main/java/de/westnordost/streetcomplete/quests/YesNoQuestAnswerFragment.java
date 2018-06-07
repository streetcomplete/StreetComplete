package de.westnordost.streetcomplete.quests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.streetcomplete.R;

/** Abstract base class for dialogs in which the user answers a yes/no quest */
public class YesNoQuestAnswerFragment extends AbstractQuestAnswerFragment
{
	public static final String ANSWER = "answer";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		View buttonPanel = setButtonsView(R.layout.quest_buttonpanel_yesno);

		buttonPanel.findViewById(R.id.buttonYes).setOnClickListener(v -> onClickYesNo(true));
		buttonPanel.findViewById(R.id.buttonNo).setOnClickListener(v -> onClickYesNo(false));
		return view;
	}

	@Override public boolean hasChanges() {	return false; }

	protected void onClickYesNo(boolean answer)
	{
		Bundle bundle = new Bundle();
		bundle.putBoolean(ANSWER, answer);
		applyImmediateAnswer(bundle);
	}
}
