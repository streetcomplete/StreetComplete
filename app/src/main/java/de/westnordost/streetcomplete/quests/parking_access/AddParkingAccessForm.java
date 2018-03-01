package de.westnordost.streetcomplete.quests.parking_access;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;

public class AddParkingAccessForm extends AbstractQuestFormAnswerFragment
{

	RadioGroup radioGroup;

	public static final String ACCESS = "access";

	private static final String
		YES = "yes",
		CUSTOMERS = "customers",
		PRIVATE = "private";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		View contentView = setContentView(R.layout.quest_parking_access);
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
				case R.id.yes:
					answer.putString(ACCESS, YES);
					break;
				case R.id.customers:
					answer.putString(ACCESS, CUSTOMERS);
					break;
				case R.id.private_access:
					answer.putString(ACCESS, PRIVATE);
					break;
			}

			applyFormAnswer(answer);
		}
	}

	@Override public boolean hasChanges() { return radioGroup.getCheckedRadioButtonId() != -1; }
}
