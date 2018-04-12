package de.westnordost.streetcomplete.quests.place_name;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

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

		addOtherAnswer(R.string.quest_name_answer_noName, this::confirmNoName);

		return view;
	}

	@Override protected void onClickOk()
	{
		Bundle data = new Bundle();
		String name = nameInput.getText().toString().trim();
		data.putString(NAME, name);
		applyFormAnswer(data);
	}

	private void confirmNoName()
	{
		new AlertDialogBuilder(getActivity())
			.setTitle(R.string.quest_name_answer_noName_confirmation_title)
			.setPositiveButton(R.string.quest_name_noName_confirmation_positive, (dialog, which) ->
			{
				Bundle data = new Bundle();
				data.putBoolean(NO_NAME, true);
				applyImmediateAnswer(data);
			})
			.setNegativeButton(R.string.quest_generic_confirmation_no, null)
			.show();
	}

	@Override public boolean hasChanges()
	{
		return !nameInput.getText().toString().trim().isEmpty();
	}
}
