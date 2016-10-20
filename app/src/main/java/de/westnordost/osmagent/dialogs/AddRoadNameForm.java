package de.westnordost.osmagent.dialogs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.westnordost.osmagent.R;

public class AddRoadNameForm extends AbstractQuestAnswerFragment
{
	public static final String NO_NAME = "no_name";
	public static final String NAME = "name";

	private AutoCorrectAbbreviationsEditText nameInput;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		setTitle(R.string.quest_streetName_title);
		View contentView = setContentView(R.layout.quest_roadname);

		nameInput = (AutoCorrectAbbreviationsEditText) contentView.findViewById(R.id.nameInput);

		return view;
	}

	@Override protected void onClickOk()
	{
		if(!validate()) return;

		applyNameAnswer();
	}

	@Override protected List<Integer> getOtherAnswerResourceIds()
	{
		List<Integer> answers = super.getOtherAnswerResourceIds();
		answers.add(R.string.quest_streetName_answer_noName);
		return answers;
	}

	@Override protected boolean onClickOtherAnswer(int itemResourceId)
	{
		if(super.onClickOtherAnswer(itemResourceId)) return true;

		if(itemResourceId == R.string.quest_streetName_answer_noName)
		{
			confirmNoStreetName();
			return true;
		}
		return false;
	}

	private void applyNameAnswer()
	{
		Bundle data = new Bundle();
		String name = nameInput.getText().toString().trim();
		data.putString(NAME, name);
		applyAnswer(data);
	}

	private boolean validate()
	{
		String name = nameInput.getText().toString().trim();
		if(name.isEmpty())
		{
			nameInput.setError(getResources().getString(R.string.quest_generic_error_field_empty));
			return false;
		}

		if(name.contains(".") || nameInput.containsAbbreviations())
		{
			confirmPossibleAbbreviation();
			return false;
		}
		return true;
	}

	private void confirmPossibleAbbreviation()
	{
		DialogInterface.OnClickListener onYes = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				applyNameAnswer();
			}
		};
		DialogInterface.OnClickListener onNo = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// nothing, just go back
			}
		};

		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.quest_streetName_nameWithAbbreviations_confirmation_title)
				.setMessage(R.string.quest_streetName_nameWithAbbreviations_confirmation_description)
				.setPositiveButton(R.string.quest_streetName_nameWithAbbreviations_confirmation_positive, onYes)
				.setNegativeButton(R.string.quest_generic_confirmation_no, onNo)
				.show();
	}

	private void confirmNoStreetName()
	{
		DialogInterface.OnClickListener onYes = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				Bundle data = new Bundle();
				data.putBoolean(NO_NAME, true);
				applyAnswer(data);
			}
		};
		DialogInterface.OnClickListener onNo = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// nothing, just go back
			}
		};

		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.quest_streetName_answer_noName_confirmation_title)
				.setMessage(R.string.quest_streetName_answer_noName_confirmation_description)
				.setPositiveButton(R.string.quest_streetName_noName_confirmation_positive, onYes)
				.setNegativeButton(R.string.quest_generic_confirmation_no, onNo)
				.show();
	}

	@Override public boolean hasChanges()
	{
		return !nameInput.getText().toString().trim().isEmpty();
	}
}
