package de.westnordost.streetcomplete.quests.internet_access;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;

public class AddInternetAccessForm extends AbstractQuestFormAnswerFragment
{
	private RadioGroup radioGroup;

	public static final String INTERNET_ACCESS = "internet_access";

	private static final String
			WLAN = "wlan",
			NO = "no",
			TERMINAL = "terminal",
			WIRED = "wired";

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		View contentView = setContentView(R.layout.quest_internet_access);
		radioGroup = contentView.findViewById(R.id.radioButtonGroup);
		radioGroup.setOnCheckedChangeListener((group, checkedId) -> checkIsFormComplete());
		return view;
	}

	@Override protected void onClickOk()
	{
		Bundle answer = new Bundle();
		answer.putString(INTERNET_ACCESS, getInternetAccessValueByCheckedRadioButtonId());
		applyAnswer(answer);
	}

	private String getInternetAccessValueByCheckedRadioButtonId()
	{
		switch (radioGroup.getCheckedRadioButtonId())
		{
			case R.id.wlan:      return WLAN;
			case R.id.no:        return NO;
			case R.id.terminal:  return TERMINAL;
			case R.id.wired:     return WIRED;
		}
		return null;
	}

	@Override public boolean isFormComplete() { return radioGroup.getCheckedRadioButtonId() != -1; }
}
