package de.westnordost.streetcomplete.quests.parking_access;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;

public class AddParkingAccessForm extends AbstractQuestFormAnswerFragment
{
	private RadioGroup radioGroup;

	public static final String ACCESS = "access";

	private static final String
		YES = "yes",
		CUSTOMERS = "customers",
		PRIVATE = "private";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		View contentView = setContentView(R.layout.quest_parking_access);
		radioGroup = contentView.findViewById(R.id.radioButtonGroup);
		radioGroup.setOnCheckedChangeListener((group, checkedId) -> checkIsFormComplete());
		return view;
	}

	@Override protected void onClickOk()
	{
		Bundle answer = new Bundle();
		answer.putString(ACCESS, getAccessValueByCheckedRadioButtonId());
		applyAnswer(answer);
	}

	private String getAccessValueByCheckedRadioButtonId()
	{
		switch (radioGroup.getCheckedRadioButtonId())
		{
			case R.id.yes:            return YES;
			case R.id.customers:      return CUSTOMERS;
			case R.id.private_access: return PRIVATE;
		}
		return null;
	}

	@Override public boolean isFormComplete() { return radioGroup.getCheckedRadioButtonId() != -1; }
}
