package de.westnordost.streetcomplete.quests.recycling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddRecyclingTypeForm extends AbstractQuestAnswerFragment
{
	public static final String ANSWER = "answer";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		View buttonPanel = setContentView(R.layout.quest_recycling_type);
		setTitle(R.string.quest_recycling_type_title);

		Button buttonYes = (Button) buttonPanel.findViewById(R.id.buttonCentre);
		buttonYes.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickAnswer("centre");
			}
		});
		Button buttonLimited = (Button) buttonPanel.findViewById(R.id.buttonOverground);
		buttonLimited.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickAnswer("overground");
			}
		});
		Button buttonNo = (Button) buttonPanel.findViewById(R.id.buttonUnderground);
		buttonNo.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickAnswer("underground");
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
