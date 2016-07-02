package de.westnordost.osmagent.quests.dialogs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import de.westnordost.osmagent.R;

public class StreetNameDialog extends AbstractQuestAnswerDialog
{
	public static final String NO_NAME = "no_name";
	public static final String NAME = "name";

	private CheckBox hasNoName;
	private AutoCorrectAbbreviationsEditText nameInput;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);

		setTitle(R.string.quest_streetName_title);
		View contentView = setContentView(R.layout.quest_streetname);

		nameInput = (AutoCorrectAbbreviationsEditText) contentView.findViewById(R.id.nameInput);

		nameInput.addTextChangedListener(new DefaultTextWatcher()
		{
			@Override
			public void afterTextChanged(Editable s)
			{
				hasNoName.setEnabled(s.length() == 0);
			}
		});

		hasNoName = (CheckBox) contentView.findViewById(R.id.noNameCheckbox);
		hasNoName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked)
			{
				if(isChecked)
				{
					confirmNoStreetName();
				}
				else
				{
					nameInput.setEnabled(true);
				}
			}
		});

		return view;
	}


	@Override
	protected boolean validate()
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


	@Override
	protected void addAnswer(Bundle data)
	{
		String name = nameInput.getText().toString().trim();
		data.putBoolean(NO_NAME, hasNoName.isChecked());
		data.putString(NAME, name);
	}

	private void confirmPossibleAbbreviation()
	{
		DialogInterface.OnClickListener onYes = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				applyAnswerAndDismiss();
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
				onNoStreetNameConfirmed();
			}
		};

		DialogInterface.OnClickListener onNo = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				onNoStreetNameCancelled();
			}
		};

		DialogInterface.OnCancelListener onCancel = new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialog)
			{
				onNoStreetNameCancelled();
			}
		};

		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.quest_generic_confirmation_title)
				.setMessage(R.string.quest_streetName_noName_confirmation_description)
				.setPositiveButton(R.string.quest_generic_confirmation_yes, onYes)
				.setNegativeButton(R.string.quest_generic_confirmation_no, onNo)
				.setOnCancelListener(onCancel)
				.show();
	}

	private void onNoStreetNameConfirmed()
	{
		nameInput.setEnabled(false);
		nameInput.setError(null);
	}

	private void onNoStreetNameCancelled()
	{
		hasNoName.setChecked(false);
	}

}
