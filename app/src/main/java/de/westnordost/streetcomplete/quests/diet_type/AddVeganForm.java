package de.westnordost.streetcomplete.quests.diet_type;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class AddVeganForm extends YesNoQuestAnswerFragment
{

	public static final String OTHER_ANSWER = "OTHER_ANSWER";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setTitle();
		addOtherAnswer(R.string.quest_dietType_only_vegan, new Runnable()
		{
			@Override public void run()
			{
				applyAnswer("only");
			}
		});
		return view;
	}

	private void setTitle()
	{
		String name = getElementName();
		if(name != null)
		{
			setTitle(R.string.quest_dietType_vegan_name_title, name);
		}
		else
		{
			setTitle(R.string.quest_dietType_vegan_title);
		}
	}

	private void applyAnswer(String value)
	{
		Bundle answer = new Bundle();
		answer.putString(OTHER_ANSWER, value);
		applyImmediateAnswer(answer);
	}
}
