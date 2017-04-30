package de.westnordost.streetcomplete.quests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.westnordost.streetcomplete.R;

/** Abstract base class for dialogs in which the user answers a yes/no quest */
public abstract class AbstractYesNoQuestAnswerFragment extends AbstractQuestAnswerFragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		View buttonPanel = setButtonsView(R.layout.quest_buttonpanel_yesno);

		Button buttonYes = (Button) buttonPanel.findViewById(R.id.buttonYes);
		buttonYes.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickYesNo(true);
			}
		});
		Button buttonNo = (Button) buttonPanel.findViewById(R.id.buttonNo);
		buttonNo.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				onClickYesNo(false);
			}
		});
		return view;
	}

	@Override public boolean hasChanges()
	{
		return false;
	}

	protected abstract void onClickYesNo(boolean yes);
}
