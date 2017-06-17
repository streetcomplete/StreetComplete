package de.westnordost.streetcomplete.quests.wheelchair_access;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
		View buttonPanel = setButtonsView(R.layout.quest_wheelchair_access);

		Button buttonYes = (Button) buttonPanel.findViewById(R.id.buttonYes);
		buttonYes.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickAnswer("yes");
			}
		});
		Button buttonLimited = (Button) buttonPanel.findViewById(R.id.buttonLimited);
		buttonLimited.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickAnswer("limited");
			}
		});
		Button buttonNo = (Button) buttonPanel.findViewById(R.id.buttonNo);
		buttonNo.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickAnswer("no");
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
