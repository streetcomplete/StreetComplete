package de.westnordost.streetcomplete.quests.internet_access;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;

public class AddInternetAccessForm extends AbstractQuestFormAnswerFragment
{

	RadioGroup radioGroup;

	public static final String INTERNET_ACCESS = "internet_access";

	private static final String
			WLAN = "wlan",
			NO = "no",
			TERMINAL = "terminal",
			WIRED = "wired";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		View contentView = setContentView(R.layout.quest_internet_access);
		radioGroup = contentView.findViewById(R.id.radioButtonGroup);

		return view;
	}


	@Override protected void onClickOk()
	{
		Bundle answer = new Bundle();

		int checkedButton = radioGroup.getCheckedRadioButtonId();
		if (checkedButton == -1)
		{
			Toast.makeText(getActivity(), R.string.no_changes, Toast.LENGTH_SHORT).show();
		}
		else
		{
			switch (checkedButton)
			{
				case R.id.wlan:
					answer.putString(INTERNET_ACCESS, WLAN);
					break;
				case R.id.no:
					answer.putString(INTERNET_ACCESS, NO);
					break;
				case R.id.terminal:
					answer.putString(INTERNET_ACCESS, TERMINAL);
					break;
				case R.id.wired:
					answer.putString(INTERNET_ACCESS, WIRED);
					break;
			}

			applyFormAnswer(answer);
		}
	}

	@Override public boolean hasChanges()
	{
		return !(radioGroup.getCheckedRadioButtonId() == -1);
	}
}
