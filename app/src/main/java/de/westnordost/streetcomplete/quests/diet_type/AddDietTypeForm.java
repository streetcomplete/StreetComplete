package de.westnordost.streetcomplete.quests.diet_type;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddDietTypeForm extends AbstractQuestAnswerFragment
{

	public static final String YES = "YES";
	public static final String NO = "NO";
	public static final String ONLY = "ONLY";
	public static final String ANSWER = "answer";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		View buttonPanel = setButtonsView(R.layout.quest_buttonpanel_diet_type);

		Button buttonYes = (Button) buttonPanel.findViewById(R.id.buttonYes);
		buttonYes.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickAnswer(YES);
			}
		});

		Button buttonNo = (Button) buttonPanel.findViewById(R.id.buttonNo);
		buttonNo.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickAnswer(NO);
			}
		});

		Button buttonOnly = (Button) buttonPanel.findViewById(R.id.buttonOnly);
		buttonOnly.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickAnswer(ONLY);
			}
		});

		return view;
	}

	@Override public boolean hasChanges()
	{
		return false;
	}

	protected void onClickAnswer(String answer)
	{
		Bundle bundle = new Bundle();
		bundle.putString(ANSWER, answer);
		applyImmediateAnswer(bundle);
	}
}
