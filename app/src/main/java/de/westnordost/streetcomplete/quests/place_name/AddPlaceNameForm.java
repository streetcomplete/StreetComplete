package de.westnordost.streetcomplete.quests.place_name;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.util.TextChangedWatcher;


public class AddPlaceNameForm extends AbstractQuestFormAnswerFragment
{
	public static final String NO_NAME = "no_name";
	public static final String NAME = "name";

	private EditText nameInput;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		View contentView = setContentView(R.layout.quest_placename);
		nameInput = contentView.findViewById(R.id.nameInput);
		nameInput.addTextChangedListener(new TextChangedWatcher(this::checkIsFormComplete));

		addOtherAnswer(R.string.quest_name_answer_noName, this::confirmNoName);

		return view;
	}

	@Override protected void onClickOk()
	{
		Bundle data = new Bundle();
		data.putString(NAME, getPlaceName());
		applyAnswer(data);
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

	@Override public boolean isFormComplete() { return !getPlaceName().isEmpty(); }

	private String getPlaceName() { return nameInput.getText().toString().trim(); }
}
