package de.westnordost.streetcomplete.quests.complete;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class CompleteQuestYesNoAnswerFragment extends YesNoQuestAnswerFragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		buttonOtherAnswers.setVisibility(View.GONE);
		return view;
	}
}
