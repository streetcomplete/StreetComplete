package de.westnordost.streetcomplete.quests.internet_access;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddInternetAccessForm extends AbstractQuestAnswerFragment
{
	private static final String
			YES = "yes",
			NO = "no",
			WIFI = "wifi",
			WIRED = "wired",
			TERMINAL = "terminal";

	public static final String OSM_VALUE = "answer";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		View buttonPanel = setButtonsView(R.layout.quest_buttonpanel_internet_access);

		buttonPanel.findViewById(R.id.buttonWired).setOnClickListener(v -> applyAnswer(WIRED));
		buttonPanel.findViewById(R.id.buttonNo).setOnClickListener(v -> applyAnswer(NO));
		buttonPanel.findViewById(R.id.buttonWifi).setOnClickListener(v -> applyAnswer(WIFI));

		addOtherAnswers();

		return view;
	}

	@Override public boolean hasChanges()
	{
		return false;
	}

	private void addOtherAnswers()
	{
		addOtherAnswer(R.string.quest_internet_access_terminal, () -> applyAnswer(TERMINAL));
		addOtherAnswer(R.string.quest_internet_access_yes, () -> applyAnswer(YES));
	}

	private void applyAnswer(String answer)
	{
		Bundle bundle = new Bundle();
		bundle.putString(OSM_VALUE, answer);
		applyImmediateAnswer(bundle);
	}
}
