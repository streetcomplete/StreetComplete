package de.westnordost.streetcomplete.quests.localized_name;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.streetcomplete.R;


public class AddBusStopNameForm extends AddLocalizedNameForm
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		View contentView = setContentView(R.layout.quest_localizedname);

		addOtherAnswers();

		initLocalizedNameAdapter(contentView, savedInstanceState);

		return view;
	}

	private void addOtherAnswers() {
		addOtherAnswer(R.string.quest_name_answer_noName, this::confirmNoName);
		addOtherAnswer(R.string.quest_streetName_answer_cantType, this::showKeyboardInfo);
	}

	@Override
	protected void onClickOk() {
		applyNameAnswer();
	}

	private void confirmNoName()
	{
		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.quest_name_answer_noName_confirmation_title)
				.setPositiveButton(R.string.quest_name_noName_confirmation_positive, (dialog, which) ->
				{
					Bundle data = new Bundle();
					data.putBoolean(NO_NAME, true);
					applyAnswer(data);
				})
				.setNegativeButton(R.string.quest_generic_confirmation_no, null)
				.show();
	}
}
